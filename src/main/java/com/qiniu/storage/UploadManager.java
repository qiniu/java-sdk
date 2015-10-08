package com.qiniu.storage;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.IOException;

/**
 * 七牛文件上传管理器
 * <p/>
 * 一般默认可以使用这个类的方法来上传数据和文件。这个类自动检测文件的大小，
 * 只要超过了{@link com.qiniu.common.Config#PUT_THRESHOLD}
 */
public final class UploadManager {
    private final Client client;
    private final Recorder recorder;
    private final RecordKeyGenerator keyGen;

    public UploadManager() {
        this(null, null);
    }

    /**
     * 断点上传记录。只针对 文件分块上传。
     * 分块上传中，将每一块上传的记录保存下来。上传中断后可在上一次断点记录基础上上传剩余部分。
     *
     * @param recorder 断点记录者
     */
    public UploadManager(Recorder recorder) {
        this(recorder, new RecordKeyGenerator() {

            @Override
            public String gen(String key, File file) {
                return key + "_._" + file.getAbsolutePath();
            }
        });
    }

    /**
     * 断点上传记录。只针对 文件分块上传。
     * 分块上传中，将每一块上传的记录保存下来。上传中断后可在上一次断点记录基础上上传剩余部分。
     *
     * @param recorder 断点记录者
     * @param keyGen   生成文件的断点记录标示，根据生成的标示，可找到断点记录的内容
     */
    public UploadManager(Recorder recorder, RecordKeyGenerator keyGen) {
        client = new Client();
        this.recorder = recorder;
        this.keyGen = keyGen;
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
        if (size <= Config.PUT_THRESHOLD) {
            return new FormUploader(client, token, key, file, params, mime, checkCrc).upload();
        }

        String recorderKey = key;
        if (keyGen != null) {
            recorderKey = keyGen.gen(key, file);
        }
        ResumeUploader uploader = new ResumeUploader(client, token, key, file,
                params, mime, recorder, recorderKey);
        return uploader.upload();
    }


    public void asyncPut(final byte[] data, final String key, final String token, StringMap params,
                         String mime, boolean checkCrc, UpCompletionHandler handler) throws IOException {
        checkArgs(key, data, null, token);
        if (mime == null) {
            mime = Client.DefaultMime;
        }
        params = filterParam(params);
        new FormUploader(client, token, key, data, params, mime, checkCrc).asyncUpload(handler);
    }
}
