package com.qiniu.qvs;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

public final class QvsResponse {
    private QvsResponse() {
    }

    public static Response get(String url, Client client, Auth auth) throws QiniuException {
        StringMap headers = auth.authorizationV2(url, "GET", null, null);
        Response res = client.get(url, headers);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    public static Response post(String url, StringMap params, Client client, Auth auth) throws QiniuException {
        byte[] body;
        String contentType = null;
        if (params == null) {
            body = null;
        } else {
            contentType = Client.JsonMime;
            body = Json.encode(params).getBytes(Constants.UTF_8);
        }
        StringMap headers = auth.authorizationV2(url, "POST", body, contentType);
        Response res = client.post(url, body, headers, Client.JsonMime);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    public static Response patch(String url, StringMap params, Client client, Auth auth) throws QiniuException {
        byte[] body;
        String contentType = null;
        if (params == null) {
            body = null;
        } else {
            contentType = Client.JsonMime;
            body = Json.encode(params).getBytes(Constants.UTF_8);
        }
        StringMap headers = auth.authorizationV2(url, "PATCH", body, contentType);
        Response res = client.patch(url, body, headers, Client.JsonMime);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    public static Response delete(String url, Client client, Auth auth) throws QiniuException {
        StringMap headers = auth.authorizationV2(url, "DELETE", null, null);
        Response res = client.delete(url, headers);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    public static Response delete(String url, StringMap params, Client client, Auth auth) throws QiniuException {
        byte[] body;
        String contentType = null;
        if (params == null) {
            body = null;
        } else {
            contentType = Client.JsonMime;
            body = Json.encode(params).getBytes(Constants.UTF_8);
        }
        StringMap headers = auth.authorizationV2(url, "DELETE", body, contentType);
        Response res = client.delete(url, body, headers, Client.JsonMime);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }
}
