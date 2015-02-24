package com.qiniu.processing;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.processing.model.PfopStatus;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

public final class PersistentOperationManager {
    private Auth auth;
    private String bucket;
    private String pipeline;
    private String notify_url;
    private boolean force;
    private Client client;

    public PersistentOperationManager(Auth auth, String bucket, String pipeline, String notify_url, boolean force) {
        this.auth = auth;
        this.bucket = bucket;
        this.pipeline = pipeline;
        this.notify_url = notify_url;
        this.force = force;
        this.client = new Client();
    }

    public String post(String key, Operation cmd) throws QiniuException {
        return post(key, Pipe.createPersistent().append(cmd));
    }

    public String post(String key, Pipe pipe) throws QiniuException {
        Pipe[] pipes = {pipe};
        return post(key, pipes);
    }

    public String post(String key, Pipe[] pipe) throws QiniuException {
        String fops = StringUtils.join(pipe, ";", null);
        StringMap map = new StringMap().put("bucket", bucket).put("key", key).put("fops", fops)
                .putNoEmpty("pipeline", pipeline).putNoEmpty("notifyURL", notify_url);

        if (force) {
            map.put("force", 1);
        }
        byte[] data = StringUtils.utf8Bytes(map.formString());
        String url = Config.API_HOST + "/pfop/";
        StringMap headers = auth.authorization(url, data, Client.FormMime);
        Response response = client.post(url, data, headers, Client.FormMime);

        PfopStatus status = response.jsonToObject(PfopStatus.class);
        return status.persistentId;
    }

    public String status(String id) throws QiniuException {
        String url = Config.API_HOST + "/status/get/prefop?id=" + id;
        Response response = client.get(url);
        return response.bodyString();
    }
}
