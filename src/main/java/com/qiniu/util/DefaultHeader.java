package com.qiniu.util;

import com.qiniu.http.Headers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DefaultHeader {
    public static final String DISABLE_TIMESTAMP_SIGNATURE_ENV_KEY = "DISABLE_QINIU_TIMESTAMP_SIGNATURE";

    public static void setDefaultHeader(HeadAdder adder) {
        if (adder == null) {
            return;
        }

        if (!isDisableQiniuTimestampSignature()) {
            adder.addHeader("X-Qiniu-Date", xQiniuDate());
        }
    }

    private static boolean isDisableQiniuTimestampSignature() {
        String value = System.getenv(DISABLE_TIMESTAMP_SIGNATURE_ENV_KEY);
        if (value == null) {
            return false;
        }
        value = value.toLowerCase();
        return value.equals("true") || value.equals("yes") || value.equals("y") || value.equals("1");
    }

    private static String xQiniuDate() {
        DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    public interface HeadAdder {
        void addHeader(String key, String value);
    }
}
