package com.qiniu.processing;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

/**
 * 触发持久化处理
 * 针对七牛空间文件，触发异步文件处理。如异步视频转码等
 */
public final class OperationManager {
    private final Client client;
    private final Auth auth;

    public OperationManager(Auth auth) {
        this.auth = auth;
        this.client = new Client();
    }

    /**
     * 触发 空间 文件 的 pfop 操作
     *
     * @param bucket 空间名
     * @param key    文件名
     * @param fops   fop指令
     * @return persistentId
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     * @link http://developer.qiniu.com/docs/v6/api/reference/fop/pfop/pfop.html
     */
    public String pfop(String bucket, String key, String fops) throws QiniuException {
        return pfop(bucket, key, fops, null);
    }

    /**
     * 触发 空间 文件 的 pfop 操作
     *
     * @param bucket 空间名
     * @param key    文件名
     * @param fops   fop指令
     * @param params notifyURL、force、pipeline 等参数
     * @return persistentId
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     * @link http://developer.qiniu.com/docs/v6/api/reference/fop/pfop/pfop.html
     */
    public String pfop(String bucket, String key, String fops, StringMap params) throws QiniuException {
        params = params == null ? new StringMap() : params;
        params.put("bucket", bucket).put("key", key).put("fops", fops);
        byte[] data = StringUtils.utf8Bytes(params.formString());
        String url = Config.API_HOST + "/pfop/";
        StringMap headers = auth.authorization(url, data, Client.FormMime);
        Response response = client.post(url, data, headers, Client.FormMime);
        PfopStatus status = response.jsonToObject(PfopStatus.class);
        return status.persistentId;
    }

    private class PfopStatus {
        public String persistentId;
    }
}
