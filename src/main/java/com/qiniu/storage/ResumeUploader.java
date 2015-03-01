package com.qiniu.storage;

import com.qiniu.common.Config;
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
 * Created by bailong on 15/2/23.
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
    private FileInputStream file;
    private String host;

    ResumeUploader(
            Client client,
            String upToken,
            String key,
            File file,
            StringMap params,
            String mime
    ) {
        this.client = client;
        this.upToken = upToken;
        this.key = key;
        this.f = file;
        this.size = file.length();
        this.params = params;
        this.mime = mime == null ? Client.DefaultMime : mime;
        this.host = Config.UP_HOST;
        long count = (size + Config.BLOCK_SIZE - 1) / Config.BLOCK_SIZE;
        this.contexts = new String[(int) count];
        this.blockBuffer = new byte[Config.BLOCK_SIZE];
    }

    public Response upload() throws QiniuException {
        try {
            this.file = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new QiniuException(e);
        }
        long uploaded = 0;
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

            long crc = Crc32.bytes(blockBuffer, 0, blockSize);
            Response response = null;
            try {
                response = makeBlock(blockBuffer, blockSize);
            } catch (QiniuException e) {
                if (e.code() < 0) {
                    host = Config.UP_HOST_BACKUP;
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
            //todo check return crc32
            // if blockInfo.crc32 != crc{}

            contexts[contextIndex++] = blockInfo.ctx;
            uploaded += blockSize;
        }
        close();
        return makeFile();
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
}
