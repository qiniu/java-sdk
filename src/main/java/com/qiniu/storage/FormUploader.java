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
    private final ConfigHelper configHelper;
    private StringMap params;
    private String filename;
    private Client client;

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
        this.configHelper = new ConfigHelper(configuration);
    }

    /**
     * 同步上传文件
     */
    public Response upload() throws QiniuException {
        buildParams();
        String host = configHelper.upHost(token);
        try {
            if (data != null) {
                return client.multipartPost(configHelper.upHost(token), params, "file", filename, data,
                        mime, new StringMap());
            } else {
                return client.multipartPost(configHelper.upHost(token), params, "file", filename, file,
                        mime, new StringMap());
            }
        } catch (QiniuException e) {
            if (e.response == null || e.response.needSwitchServer()) {
                changeHost(token, host);
            }
            throw e;
        }
    }

    /**
     * 异步上传文件
     */
    public void asyncUpload(final UpCompletionHandler handler) throws IOException {
        buildParams();
        final String host = configHelper.upHost(token);
        if (data != null) {
            client.asyncMultipartPost(host, params, "file", filename,
                    data, mime, new StringMap(), new AsyncCallback() {
                        @Override
                        public void complete(Response res) {
                            if (res != null && res.needSwitchServer()) {
                                changeHost(token, host);
                            }
                            handler.complete(key, res);
                        }
                    });
            return;
        }
        client.asyncMultipartPost(configHelper.upHost(token), params, "file", filename,
                file, mime, new StringMap(), new AsyncCallback() {
                    @Override
                    public void complete(Response res) {
                        if (res != null && res.needSwitchServer()) {
                            changeHost(token, host);
                        }
                        handler.complete(key, res);
                    }
                });
    }

    private void changeHost(String upToken, String host) {
        try {
            configHelper.tryChangeUpHost(upToken, host);
        } catch (Exception e) {
            // ignore
            // use the old up host //
        }
    }

    private void buildParams() throws QiniuException {
        if (params == null) return;
        params.put("token", token);

        if (key != null) {
            params.put("key", key);
        }

        if (file != null) {
            filename = file.getName();
        } else {
            Object object = params.get("filename");
            if (object != null) {
                filename = (String) object;
                object = null;
            } else if (filename == null || filename.trim().length() == 0) {
                if (key == null) {
                    filename = "defaultFilename";
                } else {
                    filename = key;
                }
            }
        }

        if (checkCrc) {
            long crc32;
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
}
