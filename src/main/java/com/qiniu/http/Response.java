package com.qiniu.http;


import com.qiniu.common.QiniuException;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import okhttp3.MediaType;

import java.io.IOException;
import java.io.InputStream;
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
     * 服务器IP
     */
    public final String address;

    private byte[] body;
    private okhttp3.Response response;

    private Response(okhttp3.Response response, int statusCode, String reqId, String xlog, String xvia,
                     String address, double duration, String error, byte[] body) {
        this.response = response;
        this.statusCode = statusCode;
        this.reqId = reqId;
        this.xlog = xlog;
        this.xvia = xvia;
        this.duration = duration;
        this.error = error;
        this.address = address;
        this.body = body;
    }

    public static Response create(okhttp3.Response response, String address, double duration) {
        String error = null;
        int code = response.code();
        String reqId = null;
        reqId = response.header("X-Reqid");
        reqId = (reqId == null) ? null : reqId.trim();

        byte[] body = null;
        if (ctype(response).equals(Client.JsonMime)) {
            try {
                body = response.body().bytes();
                if (response.code() >= 400 && !StringUtils.isNullOrEmpty(reqId) && body != null) {
                    ErrorBody errorBody = Json.decode(new String(body), ErrorBody.class);
                    error = errorBody.error;
                }
            } catch (Exception e) {
                if (response.code() < 300) {
                    error = e.getMessage();
                }
            }
        }
        return new Response(response, code, reqId, response.header("X-Log"), via(response),
                address, duration, error, body);
    }

    static Response createError(okhttp3.Response response, String address, double duration, String error) {
        if (response == null) {
            return new Response(null, -1, "", "", "", "", duration, error, null);
        }
        int code = response.code();
        String reqId = null;
        reqId = response.header("X-Reqid");
        reqId = (reqId == null) ? null : reqId.trim();

        byte[] body = null;
        if (ctype(response).equals(Client.JsonMime)) {
            try {
                body = response.body().bytes();
                if (response.code() >= 400 && !StringUtils.isNullOrEmpty(reqId) && body != null) {
                    ErrorBody errorBody = Json.decode(new String(body), ErrorBody.class);
                    error = errorBody.error;
                }
            } catch (Exception e) {
                if (response.code() < 300) {
                    error = e.getMessage();
                }
            }
        }
        return new Response(response, code, reqId, response.header("X-Log"), via(response),
                address, duration, error, body);
    }


    private static String via(okhttp3.Response response) {
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

    private static String ctype(okhttp3.Response response) {
        MediaType mediaType = response.body().contentType();
        if (mediaType == null) {
            return "";
        }
        return mediaType.type() + "/" + mediaType.subtype();
    }

    public boolean isOK() {
        return statusCode == 200 && error == null && reqId != null && reqId.length() > 0;
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
        return String.format(Locale.ENGLISH,
                "{ResponseInfo:%s,status:%d, reqId:%s, xlog:%s, xvia:%s, adress:%s, duration:%f s, error:%s}",
                super.toString(), statusCode, reqId, xlog, xvia, address, duration, error);
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

    public synchronized InputStream bodyStream() throws QiniuException {
        if (this.response == null) {
            return null;
        }
        return this.response.body().byteStream();
    }

    public String contentType() {
        return ctype(response);
    }

    public boolean isJson() {
        return contentType().equals(Client.JsonMime);
    }

    public String url() {
        return response.request().url().toString();
    }

    public static class ErrorBody {
        public String error;
    }
}
