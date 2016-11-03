package com.qiniu.common;

import com.qiniu.http.Error;
import com.qiniu.http.Response;

import java.io.IOException;

/**
 * 异常，封装了http响应数据
 */
public final class QiniuException extends IOException {
    public final Response response;
    private String error;


    public QiniuException(Response response) {
        this.response = response;
    }

    public QiniuException(Exception e) {
        super(e);
        this.response = null;
    }

    public String url() {
        return response.url();
    }

    public int code() {
        return response == null ? -1 : response.statusCode;
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
