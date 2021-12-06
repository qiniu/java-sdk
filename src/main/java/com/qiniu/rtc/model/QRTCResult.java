package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class QRTCResult<T> {
    private int code;
    private String message;
    private T result;

    public QRTCResult(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> QRTCResult<T> fail(int code, String message) {
        return new QRTCResult<>(code, message, null);
    }

    public static <T> QRTCResult<T> success(int code, T result) {
        return new QRTCResult<>(code, "OK", result);
    }


}
