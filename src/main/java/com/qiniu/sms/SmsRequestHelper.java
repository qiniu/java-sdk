package com.qiniu.sms;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

/**
 * @author hugo
 * @date 2023-06-07 21:25
 */
public class SmsRequestHelper {
    private Configuration configuration;
    private Client client;
    private Auth auth;

    public SmsRequestHelper(Configuration configuration, Client client, Auth auth) {
        this.configuration = configuration;
        this.client = client;
        this.auth = auth;
    }

    public Response get(String url) throws QiniuException {
        StringMap headers = composeHeader(url, MethodType.GET.toString(), null, Client.FormMime);
        return client.get(url, headers);
    }

    public Response post(String url, byte[] body) throws QiniuException {
        StringMap headers = composeHeader(url, MethodType.POST.toString(), body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    public Response put(String url, byte[] body) throws QiniuException {
        StringMap headers = composeHeader(url, MethodType.PUT.toString(), body, Client.JsonMime);
        return client.put(url, body, headers, Client.JsonMime);
    }

    public Response delete(String url) throws QiniuException {
        StringMap headers = composeHeader(url, MethodType.DELETE.toString(), null, Client.DefaultMime);
        return client.delete(url, headers);
    }

    private StringMap composeHeader(String url, String method, byte[] body, String contentType) {
        StringMap headers = auth.authorizationV2(url, method, body, contentType);
        headers.put("Content-Type", contentType);
        return headers;
    }
}
