package com.qiniu.processing;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;

public final class OperationManager {
    private Auth auth;
    private long tokenExpire;
    private String domain;
    private Client client;

    public OperationManager(String domain, Auth auth, long tokenExpire) {
        this.auth = auth;
        this.domain = domain;
        this.tokenExpire = tokenExpire;
        client = new Client();
    }

    public OperationManager(String domain) {
        this(domain, null, 0);
    }

    private String buildUrl(String key, Pipe pipe) {
        String baseUrl = "http://" + domain + "/" + key + "?" + pipe;
        if (auth == null) {
            return baseUrl;
        }
        return auth.privateDownloadUrl(baseUrl, tokenExpire);
    }

    public Response get(String key, Operation op) throws QiniuException {
        return get(key, Pipe.create().append(op));
    }

    public Response get(String key, Pipe pipe) throws QiniuException {
        String url = buildUrl(key, pipe);
        return client.get(url);
    }
}
