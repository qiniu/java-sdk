package com.qiniu.http;


import com.qiniu.common.QiniuException;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.IOException;
import java.util.Locale;

/**
 * 定义HTTP请求的日志信息和常规方法
 */
public final class Response {
    public static final int InvalidArgument = -4;
    public static final int InvalidFile = -3;
    public static final int Cancelled = -2;
    public static final int NetworkError = -1;
    /**
     * 回复状态码
     */
    public final int statusCode;
    /**
     * 七牛日志扩展头
     */
    public final String reqId;
    /**
     * 七牛日志扩展头
     */
    public final String xlog;
    /**
     * cdn日志扩展头
     */
    public final String xvia;
    /**
     * 错误信息
     */
    public final String error;
    /**
     * 请求消耗时间，单位秒
     */
    public final double duration;
    /**
     * 服务器域名
     */
    public final String host;
    /**
     * 服务器IP
     */
    public final String ip;

    private byte[] body;
    private com.squareup.okhttp.Response response;

    Response(int statusCode, String reqId, String xlog, String xvia, String host, String ip, double duration, String error) {
        this.statusCode = statusCode;
        this.reqId = reqId;
        this.xlog = xlog;
        this.xvia = xvia;
        this.host = host;
        this.duration = duration;
        this.error = error;
        this.ip = ip;
    }

    Response(com.squareup.okhttp.Response response, double duration) {
        this.response = response;
        this.statusCode = response.code();
        this.reqId = response.header("X-Reqid");
        this.xlog = response.header("X-Log");
        this.xvia = via(response);
        this.host = response.request().url().getHost();
        this.duration = duration;
        this.error = null;
        this.ip = null;
    }

    private static String via(com.squareup.okhttp.Response response) {
        String via;
        if (!(via = response.header("X-Via", "")).equals("")) {
            return via;
        }

        if (!(via = response.header("X-Px", "")).equals("")) {
            return via;
        }

        if (!(via = response.header("Fw-Via", "")).equals("")) {
            return via;
        }
        return via;
    }

    public boolean isOK() {
        return statusCode == 200 && error == null;
    }

    public boolean isNetworkBroken() {
        return statusCode == NetworkError;
    }

    public boolean isServerError() {
        return (statusCode >= 500 && statusCode < 600 && statusCode != 579) || statusCode == 996;
    }

    public boolean needSwitchServer() {
        return isNetworkBroken() || (statusCode >= 500 && statusCode < 600 && statusCode != 579);
    }

    public boolean needRetry() {
        return isNetworkBroken() || isServerError() || statusCode == 406 || (statusCode == 200 && error != null);
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "{ResponseInfo:%s,status:%d, reqId:%s, xlog:%s, xvia:%s,  host:%s, ip:%s, duration:%f s, error:%s}",
                super.toString(), statusCode, reqId, xlog, xvia, host, ip, duration, error);
    }

    public <T> T jsonToObject(Class<T> classOfT) throws QiniuException {
        if (!isJson()) {
            return null;
        }
        String b = bodyString();
        return Json.decode(b, classOfT);
    }

    public StringMap jsonToMap() throws QiniuException {
        if (!isJson()) {
            return null;
        }
        String b = bodyString();
        return Json.decode(b);
    }

    public synchronized byte[] body() throws QiniuException {
        if (body != null) {
            return body;
        }
        try {
            this.body = response.body().bytes();
        } catch (IOException e) {
            throw new QiniuException(e);
        }
        return body;
    }

    public String bodyString() throws QiniuException {
        return StringUtils.utf8String(body());
    }

    public String contentType() {
        return response.header(Client.ContentTypeHeader, Client.DefaultMime);
    }

    public boolean isJson() {
        return contentType().equals(Client.JsonMime);
    }

    public String url() {
        return response.request().urlString();
    }
}
