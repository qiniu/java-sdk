package com.qiniu.processing;

/**
 * Created by bailong on 15/2/25.
 */
public class GeneralOp implements Operation {
    protected int argsCount = 0;
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


    public GeneralOp put(String key, Object value) {
        argsCount++;
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
