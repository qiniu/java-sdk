package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.ResumeBlockInfo;
import com.qiniu.util.Crc32;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * 分片上传
 * 参考文档：<a href="http://developer.qiniu.com/docs/v6/api/overview/up/chunked-upload.html">分片上传</a>
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
    private final Configuration configuration;
    private final Client client;
    private final byte[] blockBuffer;
    private final Recorder recorder;
    private final long modifyTime;
    private final RecordHelper helper;
    private FileInputStream file;
    private String host;
    private int retryMax;

    /**
     * 构建分片上传文件的对象
     */
    public ResumeUploader(Client client, String upToken, String key, File file,
                          StringMap params, String mime, Recorder recorder, Configuration configuration) {
        this.configuration = configuration;
        this.client = client;
        this.upToken = upToken;
        this.key = key;
        this.f = file;
        this.size = file.length();
        this.params = params;
        this.mime = mime == null ? Client.DefaultMime : mime;
        long count = (size + Constants.BLOCK_SIZE - 1) / Constants.BLOCK_SIZE;
        this.contexts = new String[(int) count];
        this.blockBuffer = new byte[Constants.BLOCK_SIZE];
        this.recorder = recorder;
        this.modifyTime = f.lastModified();
        helper = new RecordHelper();
        retryMax = configuration.retryMax;
    }

    /**
     * 同步上传文件
     */
    public Response upload() throws QiniuException {
        try {
            return upload0();
        } finally {
            close();
        }
    }

    private Response upload0() throws QiniuException {
        if (host == null) {
            this.host = configuration.upHost(upToken);
        }
        long uploaded = helper.recoveryFromRecord();
        try {
            this.file = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new QiniuException(e);
        }
        boolean retry = false;
        int contextIndex = blockIdx(uploaded);
        try {
            file.skip(uploaded);
        } catch (IOException e) {
            close();
            throw new QiniuException(e);
        }
        while (uploaded < size) {
            int blockSize = nextBlockSize(uploaded);
            try {
                file.read(blockBuffer, 0, blockSize);
            } catch (IOException e) {
                close();
                throw new QiniuException(e);
            }

            long crc = Crc32.bytes(blockBuffer, 0, blockSize);
            Response response = null;
            QiniuException temp = null;
            try {
                response = makeBlock(blockBuffer, blockSize);
            } catch (QiniuException e) {
                if (e.code() < 0) {
                    host = configuration.upHostBackup(upToken);
                }
                if (e.response == null || e.response.needRetry()) {
                    retry = true;
                    temp = e;
                } else {
                    close();
                    throw e;
                }
            }

            if (!retry) {
                ResumeBlockInfo blockInfo0 = response.jsonToObject(ResumeBlockInfo.class);
                if (blockInfo0.crc32 != crc) {
                    retry = true;
                    temp = new QiniuException(new Exception("block's crc32 is not match"));
                }
            }

            if (retry) {
                if (retryMax > 0) {
                    retryMax--;
                    try {
                        response = makeBlock(blockBuffer, blockSize);
                        retry = false;
                    } catch (QiniuException e) {
                        close();
                        throw e;
                    }
                } else {
                    close();
                    throw temp;
                }
            }

            ResumeBlockInfo blockInfo = response.jsonToObject(ResumeBlockInfo.class);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fileUrl() {
        String url = host + "/mkfile/" + size + "/mimeType/" + UrlSafeBase64.encodeToString(mime)
                + "/fname/" + UrlSafeBase64.encodeToString(f.getName());
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
                    b.append(UrlSafeBase64.encodeToString("" + value));
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
        if (size < uploaded + Constants.BLOCK_SIZE) {
            return (int) (size - uploaded);
        }
        return Constants.BLOCK_SIZE;
    }

    private int blockIdx(long offset) {
        return (int) (offset / Constants.BLOCK_SIZE);
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

            String recorderKey = recorder.recorderKeyGenerate(key, f);

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
                    String recorderKey = recorder.recorderKeyGenerate(key, f);
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
                String recorderKey = recorder.recorderKeyGenerate(key, f);
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
