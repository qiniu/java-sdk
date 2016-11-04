package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.ResumeBlockInfo;
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
    private final Configuration configuration;
    private final Client client;
    private final byte[] blockBuffer;
    private final InputStream stream;
    private long size;
    private String host;

    StreamUploader(Client client, String upToken, String key, InputStream stream,
                   StringMap params, String mime, Configuration configuration) {
        this.configuration = configuration;
        this.client = client;
        this.upToken = upToken;
        this.key = key;
        this.params = params;
        this.mime = mime == null ? Client.DefaultMime : mime;
        this.contexts = new ArrayList<>();
        this.blockBuffer = new byte[Constants.BLOCK_SIZE];
        this.stream = stream;
    }

    public Response upload() throws QiniuException {
        if (host == null) {
            this.host = configuration.zone.upHost(upToken);
        }

        long uploaded = 0;
        int ret = 0;
        boolean retry = false;
        int contextIndex = 0;

        while (size == 0) {
            int bufferIndex = 0;
            while (ret != -1 && bufferIndex != blockBuffer.length) {
                try {
                    ret = stream.read(blockBuffer, bufferIndex, blockBuffer.length - bufferIndex);
                } catch (IOException e) {
                    close();
                    throw new QiniuException(e);
                }
                if (ret != -1) {
                    bufferIndex += ret;
                    if (ret == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    size = uploaded + bufferIndex;
                }
            }

            Response response = null;
            try {
                response = makeBlock(blockBuffer, bufferIndex);
            } catch (QiniuException e) {
                if (e.code() < 0) {
                    host = configuration.zone.upHostBackup(upToken);
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
                    response = makeBlock(blockBuffer, bufferIndex);
                    retry = false;
                } catch (QiniuException e) {
                    close();
                    throw e;
                }

            }
            ResumeBlockInfo blockInfo = response.jsonToObject(ResumeBlockInfo.class);
            //TODO check return crc32
            // if blockInfo.crc32 != crc{}
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

    private Response makeBlock(byte[] block, int blockSize) throws QiniuException {
        String url = host + "/mkblk/" + blockSize;
        return post(url, block, 0, blockSize);
    }

    private void close() {
        try {
            stream.close();
        } catch (IOException e) {
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
