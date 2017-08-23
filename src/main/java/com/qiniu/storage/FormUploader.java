package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.AsyncCallback;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Crc32;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.IOException;

/**
 * 该类封装了七牛提供的表单上传机制
 * 参考文档：<a href="https://developer.qiniu.com/kodo/manual/form-upload">表单上传</a>
 */
public final class FormUploader {

    private final String token;
    private final String key;
    private final File file;
    private final byte[] data;
    private final String mime;
    private final boolean checkCrc;
    private final Configuration configuration;
    private StringMap params;
    private Client client;
    private String fileName;

    /**
     * 构建一个表单上传字节数组的对象
     */
    public FormUploader(Client client, String upToken, String key, byte[] data, StringMap params,
                        String mime, boolean checkCrc, Configuration configuration) {
        this(client, upToken, key, data, null, params, mime, checkCrc, configuration);
    }

    /**
     * 构建一个表单上传文件的对象
     */
    public FormUploader(Client client, String upToken, String key, File file, StringMap params,
                        String mime, boolean checkCrc, Configuration configuration) {
        this(client, upToken, key, null, file, params, mime, checkCrc, configuration);
    }

    private FormUploader(Client client, String upToken, String key, byte[] data, File file, StringMap params,
                         String mime, boolean checkCrc, Configuration configuration) {
        this.client = client;
        token = upToken;
        this.key = key;
        this.file = file;
        this.data = data;
        this.params = params;
        this.mime = mime;
        this.checkCrc = checkCrc;
        this.configuration = configuration;
    }

    /**
     * 同步上传文件
     */
    public Response upload() throws QiniuException {
        buildParams();
        if (data != null) {
            return client.multipartPost(configuration.upHost(token), params, "file", fileName, data,
                    mime, new StringMap());
        }
        return client.multipartPost(configuration.upHost(token), params, "file", fileName, file,
                mime, new StringMap());
    }

    /**
     * 异步上传文件
     */
    public void asyncUpload(final UpCompletionHandler handler) throws IOException {
        buildParams();
        if (data != null) {
            client.asyncMultipartPost(configuration.upHost(token), params, "file", fileName,
                    data, mime, new StringMap(), new AsyncCallback() {
                        @Override
                        public void complete(Response r) {
                            handler.complete(key, r);
                        }
                    });
            return;
        }
        client.asyncMultipartPost(configuration.upHost(token), params, "file", fileName,
                file, mime, new StringMap(), new AsyncCallback() {
                    @Override
                    public void complete(Response r) {
                        handler.complete(key, r);
                    }
                });
    }

    private void buildParams() throws QiniuException {
        params.put("token", token);
        if (key != null) {
            params.put("key", key);
        }
        if (file != null) {
            fileName = file.getName();
        }
        if (fileName == null || fileName.trim().length() == 0) {
            fileName = "fileName";
        }

        long crc32 = 0;
        if (file != null) {
            try {
                crc32 = Crc32.file(file);
            } catch (IOException e) {
                throw new QiniuException(e);
            }
        } else {
            crc32 = Crc32.bytes(data);
        }
        params.put("crc32", "" + crc32);

    }
}
