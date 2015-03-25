package com.qiniu.storage;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;

/**
 * 七牛文件上传管理器
 * <p/>
 * 一般默认可以使用这个类的方法来上传数据和文件。这个类自动检测文件的大小，
 * 只要超过了{@link com.qiniu.common.Config#PUT_THRESHOLD}
 */
public final class UploadManager {
    private final Client client;

    public UploadManager() {
        client = new Client();
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
     * @param data 上传的数据
     * @param key 上传数据保存的文件名
     * @param token 上传凭证
     * @param params 自定义参数，如 params.put("x:foo", "foo")
     * @param mime 指定文件mimetype
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
        return new FormUploader(client, token, key, data, params, mime, checkCrc).upload();
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
     * @param params 自定义参数，如 params.put("x:foo", "foo")
     * @param mime 指定文件mimetype
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
     * @param file  上传的文件对象
     * @param key   上传文件保存的文件名
     * @param token 上传凭证
     * @param mime 指定文件mimetype
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
        if (size <= Config.PUT_THRESHOLD) {
            return new FormUploader(client, token, key, file, params, mime, checkCrc).upload();
        }

        ResumeUploader uploader = new ResumeUploader(client, token, key, file, params, mime);
        return uploader.upload();
    }
}
