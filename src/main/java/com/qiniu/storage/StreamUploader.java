package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.ResumeBlockInfo;
import com.qiniu.util.Crc32;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by long on 2016/11/4.
 */
public final class StreamUploader {
    private final String upToken;
    private final String key;
    private final StringMap params;
    private final String mime;
    private final ArrayList<String> contexts;
    private final ConfigHelper configHelper;
    private final Client client;
    private final byte[] blockBuffer;
    private final InputStream stream;
    private long size;
    private String host = null;
    private int retryMax;

    public StreamUploader(Client client, String upToken, String key, InputStream stream,
                          StringMap params, String mime, Configuration configuration) {
        this.configHelper = new ConfigHelper(configuration);
        this.client = client;
        this.upToken = upToken;
        this.key = key;
        this.params = params;
        this.mime = mime == null ? Client.DefaultMime : mime;
        this.contexts = new ArrayList<>();
        this.blockBuffer = new byte[Constants.BLOCK_SIZE];
        this.stream = stream;
        retryMax = configuration.retryMax;
    }

    public Response upload() throws QiniuException {
        try {
            return upload0();
        } finally {
            close();
        }
    }

    private Response upload0() throws QiniuException {
        if (host == null) {
            this.host = configHelper.upHost(upToken);
        }

        long uploaded = 0;
        int ret = 0;
        boolean retry = false;
        boolean eof = false;

        while (size == 0 && !eof) {
            int bufferIndex = 0;
            int blockSize = 0;

            //try to read the full BLOCK or until the EOF
            while (ret != -1 && bufferIndex != blockBuffer.length) {
                try {
                    blockSize = blockBuffer.length - bufferIndex;
                    ret = stream.read(blockBuffer, bufferIndex, blockSize);
                } catch (IOException e) {
                    close();
                    throw new QiniuException(e);
                }
                if (ret != -1) {
                    //continue to read more
                    //advance bufferIndex
                    bufferIndex += ret;
                    if (ret == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    eof = true;
                    //file EOF here, trigger outer while-loop finish
                    size = uploaded + bufferIndex;
                }
            }

            //mkblk request
            long crc = Crc32.bytes(blockBuffer, 0, bufferIndex);
            Response response = null;
            QiniuException temp = null;
            try {
                response = makeBlock(blockBuffer, bufferIndex);
            } catch (QiniuException e) {
                if (e.code() < 0 || (e.response != null && e.response.needSwitchServer())) {
                    changeHost(upToken, host);
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
                        response = makeBlock(blockBuffer, bufferIndex);
                        retry = false;
                    } catch (QiniuException e) {
                        close();
                        throw e;
                    }
                } else {
                    throw temp;
                }
            }
            ResumeBlockInfo blockInfo = response.jsonToObject(ResumeBlockInfo.class);
            if (blockInfo.crc32 != crc) {
                throw new QiniuException(new Exception("block's crc32 is not match"));
            }
            contexts.add(blockInfo.ctx);
            uploaded += bufferIndex;
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
        }
    }

    private void changeHost(String upToken, String host) {
        try {
            this.host = configHelper.tryChangeUpHost(upToken, host);
        } catch (Exception e) {
            // ignore
            // use the old up host //
        }
    }


    private Response makeBlock(byte[] block, int blockSize) throws QiniuException {
        String url = host + "/mkblk/" + blockSize;
        return post(url, block, 0, blockSize);
    }

    private void close() {
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fileUrl() {
        String url = host + "/mkfile/" + size + "/mimeType/"
                + UrlSafeBase64.encodeToString(mime);
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
}
