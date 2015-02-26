package com.qiniu.processing.image;

import com.qiniu.processing.GeneralOp;

/**
 * Created by bailong on 15/2/25.
 */
public final class BasicOp extends GeneralOp {
    public BasicOp(int mode) {
        super("imageView2", mode);
    }

    public BasicOp width(int w) {
        put("w", w);
        return this;
    }

    public BasicOp height(int h) {
        put("h", h);
        return this;
    }

    public BasicOp format(String format) {
        put("format", format);
        return this;
    }

    public BasicOp interlace(int flag) {
        put("interlace", flag);
        return this;
    }

    @Override
    public String build() {
        if (argsCount == 0) {
            throw new IllegalStateException("zip list must have at least one part.");
        }
        return super.build();
    }

}
