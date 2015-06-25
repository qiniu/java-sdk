package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.ResumeBlockInfo;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * 分片上传
 * 文档：<a href="http://developer.qiniu.com/docs/v6/api/overview/up/chunked-upload.html">
 * 分片上传</a>
 * <p/>
 * 分片上传通过将一个文件分割为固定大小的块(4M)，每次上传一个块的内容（服务端只分块，没有分片）。
 * 等待所有块都上传完成之后，再将这些块拼接起来，构成一个完整的文件。
 * 另外分片上传还支持纪录上传进度，如果本次上传被暂停，那么下次还可以从上次
 * 上次完成的文件偏移位置，继续开始上传，这样就实现了断点续传功能。
 * <p/>
 * 服务端网络较稳定，较大文件（如500M以上）才需要将块记录保存下来。
 * 小文件没有必要，可以有效地实现大文件的上传。
 */
public final class ResumeUploader {
    private final String upToken;
    private final String key;
    private final File f;
    private final long size;
    private final StringMap params;
    private final String mime;
    private final String[] contexts;
    private final Client client;
    private final byte[] blockBuffer;
    private final Recorder recorder;
    private final String recorderKey;
    private final long modifyTime;
    private final RecordHelper helper;
    private FileInputStream file;
    private String host;

    ResumeUploader(Client client, String upToken, String key, File file,
                   StringMap params, String mime, Recorder recorder, String recorderKey) {
        this.client = client;
        this.upToken = upToken;
        this.key = key;
        this.f = file;
        this.size = file.length();
        this.params = params;
        this.mime = mime == null ? Client.DefaultMime : mime;
        this.host = Config.zone.upHost;
        long count = (size + Config.BLOCK_SIZE - 1) / Config.BLOCK_SIZE;
        this.contexts = new String[(int) count];
        this.blockBuffer = new byte[Config.BLOCK_SIZE];
        this.recorder = recorder;
        this.recorderKey = recorderKey;
        this.modifyTime = f.lastModified();
        helper = new RecordHelper();
    }

    public Response upload() throws QiniuException {
        long uploaded = helper.recoveryFromRecord();
        try {
            this.file = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new QiniuException(e);
        }
        boolean retry = false;
        int contextIndex = 0;
        while (uploaded < size) {
            int blockSize = nextBlockSize(uploaded);
            try {
                file.read(blockBuffer, 0, blockSize);
            } catch (IOException e) {
                close();
                throw new QiniuException(e);
            }

//            long crc = Crc32.bytes(blockBuffer, 0, blockSize);
            Response response = null;
            try {
                response = makeBlock(blockBuffer, blockSize);
            } catch (QiniuException e) {
                if (e.code() < 0) {
                    host = Config.zone.upHostBackup;
                }
                if (e.response == null || e.response.needRetry()) {
                    retry = true;
                } else {
                    close();
                    throw e;
                }
            }
            if (retry) {
                try {
                    response = makeBlock(blockBuffer, blockSize);
                    retry = false;
                } catch (QiniuException e) {
                    close();
                    throw e;
                }

            }
            ResumeBlockInfo blockInfo = response.jsonToObject(ResumeBlockInfo.class);
            //TODO check return crc32
            // if blockInfo.crc32 != crc{}

            contexts[contextIndex++] = blockInfo.ctx;
            uploaded += blockSize;
            helper.record(uploaded);
        }
        close();

        try {
            return makeFile();
        } catch (QiniuException e) {
            try {
                return makeFile();
            } catch (QiniuException e1) {
                throw e1;
            }
        } finally {
            helper.removeRecord();
        }
    }

    private Response makeBlock(byte[] block, int blockSize) throws QiniuException {
        String url = host + "/mkblk/" + blockSize;
        return post(url, block, 0, blockSize);
    }

    private void close() {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fileUrl() {
        String url = host + "/mkfile/" + size + "/mimeType/" + UrlSafeBase64.encodeToString(mime);
        final StringBuilder b = new StringBuilder(url);
        if (key != null) {
            b.append("/key/");
            b.append(UrlSafeBase64.encodeToString(key));
        }
        if (params != null) {
            params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    b.append("/");
                    b.append(key);
                    b.append("/");
                    b.append(value);
                }
            });
        }
        return b.toString();
    }

    private Response makeFile() throws QiniuException {
        String url = fileUrl();
        String s = StringUtils.join(contexts, ",");
        return post(url, StringUtils.utf8Bytes(s));
    }

    private Response post(String url, byte[] data) throws QiniuException {
        return client.post(url, data, new StringMap().put("Authorization", "UpToken " + upToken));
    }

    private Response post(String url, byte[] data, int offset, int size) throws QiniuException {
        return client.post(url, data, offset, size, new StringMap().put("Authorization", "UpToken " + upToken),
                Client.DefaultMime);
    }

    private int nextBlockSize(long uploaded) {
        if (size < uploaded + Config.BLOCK_SIZE) {
            return (int) (size - uploaded);
        }
        return Config.BLOCK_SIZE;
    }

    private class RecordHelper {
        long recoveryFromRecord() {
            try {
                return recoveryFromRecord0();
            } catch (Exception e) {
                e.printStackTrace();
                // ignore

                return 0;
            }
        }

        long recoveryFromRecord0() {
            if (recorder == null) {
                return 0;
            }
            byte[] data = recorder.get(recorderKey);
            if (data == null) {
                return 0;
            }
            String jsonStr = new String(data);
            Record r = new Gson().fromJson(jsonStr, Record.class);
            if (r.offset == 0 || r.modify_time != modifyTime || r.size != size
                    || r.contexts == null || r.contexts.length == 0) {
                return 0;
            }
            for (int i = 0; i < r.contexts.length; i++) {
                contexts[i] = r.contexts[i];
            }

            return r.offset;
        }

        void removeRecord() {
            try {
                if (recorder != null) {
                    recorder.del(recorderKey);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        }

        // save json value
        //{
        //    "size":filesize,
        //    "offset":lastSuccessOffset,
        //    "modify_time": lastFileModifyTime,
        //    "contexts": contexts
        //}
        void record(long offset) {
            try {
                if (recorder == null || offset == 0) {
                    return;
                }
                String data = new Gson().toJson(new Record(size, offset, modifyTime, contexts));
                recorder.set(recorderKey, data.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        }

        private class Record {
            long size;
            long offset;
            // CHECKSTYLE:OFF
            long modify_time;
            // CHECKSTYLE:ON
            String[] contexts;

            Record() {
            }

            Record(long size, long offset, long modify_time, String[] contexts) {
                this.size = size;
                this.offset = offset;
                this.modify_time = modify_time;
                this.contexts = contexts;
            }
        }
    }
}
