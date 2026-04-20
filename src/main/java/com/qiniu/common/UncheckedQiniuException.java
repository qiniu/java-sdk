package com.qiniu.common;

import com.qiniu.http.Error;
import com.qiniu.http.Response;


/**
 * 七牛SDK异常封装类，封装了http响应数据
 */
public final class UncheckedQiniuException extends RuntimeException {

    public final Response response;
    private String error;
    private boolean isUnrecoverable = false;


    public UncheckedQiniuException(Response response) {
        super(response != null ? response.getInfo() : null);
        this.response = response;
        if (response != null) {
            response.close();
        }
    }

    public static UncheckedQiniuException unrecoverable(Exception e) {
        UncheckedQiniuException exception = new UncheckedQiniuException(e);
        exception.isUnrecoverable = true;
        return exception;
    }

    public static UncheckedQiniuException unrecoverable(String msg) {
        UncheckedQiniuException exception = new UncheckedQiniuException(null, msg);
        exception.isUnrecoverable = true;
        return exception;
    }

    public UncheckedQiniuException(Exception e) {
        this(e, null);
    }

    public UncheckedQiniuException(Exception e, String msg) {
        super(msg != null ? msg : (e != null ? e.getMessage() : null), e);
        this.response = null;
        this.error = msg;
    }

    public String url() {
        return response != null ? response.url() : "";
    }

    public int code() {
        return response != null ? response.statusCode : Response.NetworkError;
    }

    public boolean isUnrecoverable() {
        return isUnrecoverable;
    }

    public String error() {
        if (error != null) {
            return error;
        }
        if (response == null || response.statusCode / 100 == 2 || !response.isJson()) {
            return null;
        }
        Error e = null;
        try {
            e = response.jsonToObject(Error.class);
        } catch (QiniuException e1) {
            e1.printStackTrace();
        }
        error = e == null ? "" : e.error;
        return error;
    }
}
