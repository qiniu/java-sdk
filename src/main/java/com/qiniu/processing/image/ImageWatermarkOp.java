package com.qiniu.processing.image;

import com.qiniu.processing.GeneralOp;
import com.qiniu.processing.Gravity;
import com.qiniu.util.UrlSafeBase64;

/**
 * Created by bailong on 15/2/25.
 */
public final class ImageWatermarkOp extends GeneralOp {

    public ImageWatermarkOp(String imageUrl) {
        super("watermark", 1);
        put("image", UrlSafeBase64.encodeToString(imageUrl));
    }

    public ImageWatermarkOp dissolve(int val) {
        put("dissolve", val);
        return this;
    }

    public ImageWatermarkOp gravity(Gravity gravity) {
        put("gravity", gravity);
        return this;
    }

    public ImageWatermarkOp distanceX(int x) {
        put("dx", x);
        return this;
    }

    public ImageWatermarkOp distanceY(int y) {
        put("dy", y);
        return this;
    }
}
