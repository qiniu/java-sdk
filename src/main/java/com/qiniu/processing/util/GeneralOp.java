package com.qiniu.processing.util;

public class GeneralOp implements Operation {
    private StringBuilder content;

    public GeneralOp(String cmd, Object mode) {
        content = new StringBuilder(cmd);
        if (mode != null) {
            content.append("/");
            content.append(mode);
        }
    }

    public GeneralOp(String cmd) {
        this(cmd, null);
    }


    public GeneralOp set(String key, Object value) {
        content.append("/");
        content.append(key);
        if (value != null) {
            content.append("/");
            content.append(value);
        }
        return this;
    }

    @Override
    public String build() {
        return content.toString();
    }
}
