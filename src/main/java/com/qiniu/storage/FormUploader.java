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
public final class FormUploader extends BaseUploader {

    private final File file;
    private final byte[] data;
    private final String mime;
    private final boolean checkCrc;
    private StringMap params;
    private String filename;

    /**
     * 构建一个表单上传字节数组的对象
     *
     * @param client        上传 Client
     * @param upToken       上传 token
     * @param key           文件上传后存储的 key
     * @param data          上传的数据
     * @param params        自定义参数
     * @param mime          MimeTYpe
     * @param checkCrc      是否开启 Crc 检测
     * @param configuration 上传配置信息
     */
    public FormUploader(Client client, String upToken, String key, byte[] data, StringMap params,
                        String mime, boolean checkCrc, Configuration configuration) {
        this(client, upToken, key, data, null, params, mime, checkCrc, configuration);
    }

    /**
     * 构建一个表单上传文件的对象
     *
     * @param client        上传 Client
     * @param upToken       上传 token
     * @param key           文件上传后存储的 key
     * @param file          上传的文件
     * @param params        自定义参数
     * @param mime          MimeTYpe
     * @param checkCrc      是否开启 Crc 检测
     * @param configuration 上传配置信息
     */
    public FormUploader(Client client, String upToken, String key, File file, StringMap params,
                        String mime, boolean checkCrc, Configuration configuration) {
        this(client, upToken, key, null, file, params, mime, checkCrc, configuration);
    }

    private FormUploader(Client client, String upToken, String key, byte[] data, File file, StringMap params,
                         String mime, boolean checkCrc, Configuration configuration) {
        super(client, upToken, key, configuration);

        this.file = file;
        this.data = data;
        this.params = params;
        this.mime = mime;
        this.checkCrc = checkCrc;
    }

    @Override
    Response uploadFlows() throws QiniuException {
        buildParams();

        Retry.RequestRetryConfig retryConfig = new Retry.RequestRetryConfig.Builder().
                setRetryMax(this.config.retryMax)
                .build();
        return Retry.retryRequestAction(retryConfig, new Retry.RequestRetryAction() {
            @Override
            public String getRequestHost() throws QiniuException {
                return configHelper.upHost(upToken);
            }

            @Override
            public void tryChangeRequestHost(String oldHost) throws QiniuException {
                changeHost(upToken, oldHost);
            }

            @Override
            public Response doRequest(String host) throws QiniuException {
                return uploadWithHost(host);
            }
        });
    }

    Response uploadWithHost(String host) throws QiniuException {
        if (data != null) {
            return client.multipartPost(host, params, "file", filename, data,
                    mime, new StringMap());
        } else {
            return client.multipartPost(host, params, "file", filename, file,
                    mime, new StringMap());
        }
    }

    /**
     * 异步上传文件
     *
     * @param handler 结束回调
     * @throws IOException 异常
     */
    public void asyncUpload(final UpCompletionHandler handler) throws IOException {
        buildParams();
        asyncRetryUploadBetweenRegion(handler);
    }

    /**
     * 支持区域间重试
     */
    private void asyncRetryUploadBetweenRegion(final UpCompletionHandler handler) {
        UploadToken token = null;
        try {
            token = new UploadToken(upToken);
        } catch (QiniuException exception) {
            exception.printStackTrace();
            handler.complete(key, Response.createError(null, "", 0, exception.getMessage()));
            return;
        }

        final UploadToken finalToken = token;
        asyncRetryUploadBetweenHosts(0, new UpCompletionHandler() {
            @Override
            public void complete(String key, Response r) {
                if (!Retry.shouldUploadAgain(r, null)
                        || !couldReloadSource() || !reloadSource()
                        || config.region == null || !config.region.switchRegion(finalToken)) {
                    handler.complete(key, r);
                } else {
                    asyncRetryUploadBetweenRegion(handler);
                }
            }
        });
    }

    /**
     * 支持 hosts 间重试
     */
    private void asyncRetryUploadBetweenHosts(final int retryIndex, final UpCompletionHandler handler) {
        String host = null;
        try {
            host = configHelper.upHost(upToken);
        } catch (QiniuException exception) {
            exception.printStackTrace();
            handler.complete(key, Response.createError(null, "", 0, exception.getMessage()));
            return;
        }

        final String finalHost = host;
        asyncUploadWithHost(finalHost, new UpCompletionHandler() {
            @Override
            public void complete(String key, Response r) {
                if (Retry.requestShouldSwitchHost(r, null)) {
                    changeHost(upToken, finalHost);
                }

                if (Retry.requestShouldRetry(r, null) && retryIndex < config.retryMax) {
                    asyncRetryUploadBetweenHosts((retryIndex + 1), handler);
                } else {
                    handler.complete(key, r);
                }
            }
        });
    }

    private void asyncUploadWithHost(String host, final UpCompletionHandler handler) {
        if (data != null) {
            client.asyncMultipartPost(host, params, "file", filename, data, mime, new StringMap(),
                    new AsyncCallback() {
                        @Override
                        public void complete(Response res) {
                            handler.complete(key, res);
                        }
                    });
        } else {
            try {
                client.asyncMultipartPost(host, params, "file", filename, file, mime, new StringMap(),
                        new AsyncCallback() {
                            @Override
                            public void complete(Response res) {
                                handler.complete(key, res);
                            }
                        });
            } catch (QiniuException exception) {
                // 看代码逻辑此处永远不会执行，仅为象征处理
                handler.complete(key, Response.createError(null, "", 0, exception.getMessage()));
            }
        }
    }

    @Override
    boolean couldReloadSource() {
        return true;
    }

    @Override
    boolean reloadSource() {
        return true;
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
        params.put("token", upToken);

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
