package com.qiniu.processing.image;

import com.qiniu.processing.GeneralOp;
import com.qiniu.processing.Gravity;

/**
 * Created by bailong on 15/2/25.
 */
public final class AdvanceOp extends GeneralOp {
    public AdvanceOp() {
        super("imageMogr2", null);
    }

    public AdvanceOp format(String format) {
        put("format", format);
        return this;
    }

    public AdvanceOp interlace(int flag) {
        put("interlace", flag);
        return this;
    }

    public AdvanceOp autoOrient() {
        put("auto-orient", null);
        return this;
    }

    public AdvanceOp strip() {
        put("strip", null);
        return this;
    }

    public AdvanceOp thumbnail(String arg) {
        put("thumbnail", arg);
        return this;
    }

    public AdvanceOp crop(String arg) {
        put("crop", arg);
        return this;
    }

    public AdvanceOp rotate(int degree) {
        put("rotate", degree);
        return this;
    }

    public AdvanceOp blur(int radius, int sigma) {
        put("blur", radius + "x" + sigma);
        return this;
    }

    public AdvanceOp gravity(Gravity gravity) {
        put("gravity", gravity);
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
