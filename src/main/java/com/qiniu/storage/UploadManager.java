package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 七牛文件上传管理器
 * <p/>
 * 一般默认可以使用这个类的方法来上传数据和文件。这个类自动检测文件的大小，
 * 只要超过了{@link Configuration#putThreshold}
 */
public final class UploadManager {
    private final Client client;
    private final Recorder recorder;
    private final Configuration configuration;

    public UploadManager(Configuration c) {
        this(c, null);
    }

    /**
     * 断点上传记录。只针对 文件分块上传。
     * 分块上传中，将每一块上传的记录保存下来。上传中断后可在上一次断点记录基础上上传剩余部分。
     *
     * @param recorder 断点记录者
     */
    public UploadManager(Configuration c, Recorder recorder) {
        configuration = c.clone();
        client = new Client(configuration.dns, configuration.dnsHostFirst, configuration.proxy,
                configuration.connectTimeout, configuration.responseTimeout, configuration.writeTimeout);
        this.recorder = recorder;

    }

    private static void checkArgs(final String key, byte[] data, File f, String token) {
        String message = null;
        if (f == null && data == null) {
            message = "no input data";
        } else if (token == null || token.equals("")) {
            message = "no token";
        }
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 过滤用户自定义参数，只有参数名以<code>x:</code>开头的参数才会被使用
     *
     * @param params 待过滤的用户自定义参数
     * @return 过滤后的用户自定义参数
     */
    private static StringMap filterParam(StringMap params) {
        final StringMap ret = new StringMap();
        if (params == null) {
            return ret;
        }

        params.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                if (value == null) {
                    return;
                }
                String val = value.toString();
                if (key.startsWith("x:") && !val.equals("")) {
                    ret.put(key, val);
                }
            }
        });

        return ret;
    }

    /**
     * 上传数据
     *
     * @param data  上传的数据
     * @param key   上传数据保存的文件名
     * @param token 上传凭证
     */
    public Response put(final byte[] data, final String key, final String token) throws QiniuException {
        return put(data, key, token, null, null, false);
    }

    /**
     * 上传数据
     *
     * @param data     上传的数据
     * @param key      上传数据保存的文件名
     * @param token    上传凭证
     * @param params   自定义参数，如 params.put("x:foo", "foo")
     * @param mime     指定文件mimetype
     * @param checkCrc 是否验证crc32
     * @return
     * @throws QiniuException
     */
    public Response put(final byte[] data, final String key, final String token, StringMap params,
                        String mime, boolean checkCrc) throws QiniuException {
        checkArgs(key, data, null, token);
        if (mime == null) {
            mime = Client.DefaultMime;
        }
        params = filterParam(params);
        return new FormUploader(client, token, key, data, params, mime, checkCrc, configuration).upload();
    }

    /**
     * 上传文件
     *
     * @param filePath 上传的文件路径
     * @param key      上传文件保存的文件名
     * @param token    上传凭证
     */
    public Response put(String filePath, String key, String token) throws QiniuException {
        return put(filePath, key, token, null, null, false);
    }

    /**
     * 上传文件
     *
     * @param filePath 上传的文件路径
     * @param key      上传文件保存的文件名
     * @param token    上传凭证
     * @param params   自定义参数，如 params.put("x:foo", "foo")
     * @param mime     指定文件mimetype
     * @param checkCrc 是否验证crc32
     */
    public Response put(String filePath, String key, String token, StringMap params,
                        String mime, boolean checkCrc) throws QiniuException {
        return put(new File(filePath), key, token, params, mime, checkCrc);
    }

    /**
     * 上传文件
     *
     * @param file  上传的文件对象
     * @param key   上传文件保存的文件名
     * @param token 上传凭证
     */
    public Response put(File file, String key, String token) throws QiniuException {
        return put(file, key, token, null, null, false);
    }

    /**
     * 上传文件
     *
     * @param file     上传的文件对象
     * @param key      上传文件保存的文件名
     * @param token    上传凭证
     * @param mime     指定文件mimetype
     * @param checkCrc 是否验证crc32
     */
    public Response put(File file, String key, String token, StringMap params,
                        String mime, boolean checkCrc) throws QiniuException {
        checkArgs(key, null, file, token);
        if (mime == null) {
            mime = Client.DefaultMime;
        }
        params = filterParam(params);
        long size = file.length();
        if (size <= configuration.putThreshold) {
            return new FormUploader(client, token, key, file, params, mime, checkCrc, configuration).upload();
        }

        ResumeUploader uploader = new ResumeUploader(client, token, key, file,
                params, mime, recorder, configuration);
        return uploader.upload();
    }

    /**
     * 异步上传数据
     *
     * @param data     上传的数据
     * @param key      上传数据保存的文件名
     * @param token    上传凭证
     * @param params   自定义参数，如 params.put("x:foo", "foo")
     * @param mime     指定文件mimetype
     * @param checkCrc 是否验证crc32
     * @param handler  上传完成的回调函数
     */
    public void asyncPut(final byte[] data, final String key, final String token, StringMap params,
                         String mime, boolean checkCrc, UpCompletionHandler handler) throws IOException {
        checkArgs(key, data, null, token);
        if (mime == null) {
            mime = Client.DefaultMime;
        }
        params = filterParam(params);
        new FormUploader(client, token, key, data, params, mime, checkCrc, configuration).asyncUpload(handler);
    }

    /**
     * 流式上传，通常情况建议文件上传，可以使用持久化的断点记录。
     *
     * @param stream sha
     * @param key    上传文件保存的文件名
     * @param token  上传凭证
     * @param params 自定义参数，如 params.put("x:foo", "foo")
     * @param mime   指定文件mimetype
     */
    public Response put(InputStream stream, String key, String token, StringMap params,
                        String mime) throws QiniuException {
        String message = null;
        if (stream == null) {
            message = "no input data";
        } else if (token == null || token.equals("")) {
            message = "no token";
        }
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
        StreamUploader uploader = new StreamUploader(client, token, key, stream,
                params, mime, configuration);
        return uploader.upload();
    }
}
