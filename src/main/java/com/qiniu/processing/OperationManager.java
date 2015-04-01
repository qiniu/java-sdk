package com.qiniu.processing;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

/**
 * Created by Simon on 2015/4/1.
 */
public class OperationManager {
    private final Client client;
    private final Auth auth;

    public OperationManager(Auth auth) {
        this.auth = auth;
        this.client = new Client();
    }

    public String pfop(String bucket, String key, String fops) throws QiniuException {
        return pfop(bucket, key, fops, null);
    }

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
