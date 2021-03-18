package com.qiniu.common;

import com.qiniu.http.Error;
import com.qiniu.http.Response;

import java.io.IOException;

/**
 * 七牛SDK异常封装类，封装了http响应数据
 */
public final class QiniuException extends IOException {

    public final Response response;
    private String error;
    private boolean isUnrecoverable = false;


    public QiniuException(Response response) {
        super(response != null ? response.getInfo() : null);
        this.response = response;
        if (response != null) {
            response.close();
        }
    }

    public static QiniuException unrecoverable(Exception e) {
        QiniuException exception = new QiniuException(e);
        exception.isUnrecoverable = true;
        return exception;
    }

    public static QiniuException unrecoverable(String msg) {
        QiniuException exception = new QiniuException(null, msg);
        exception.isUnrecoverable = true;
        return exception;
    }

    public QiniuException(Exception e) {
        this(e, null);
    }

    public QiniuException(Exception e, String msg) {
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
