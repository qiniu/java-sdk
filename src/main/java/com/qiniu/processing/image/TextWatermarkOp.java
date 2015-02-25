package com.qiniu.processing.image;

import com.qiniu.processing.GeneralOp;
import com.qiniu.processing.Gravity;
import com.qiniu.util.UrlSafeBase64;

/**
 * Created by bailong on 15/2/25.
 */
public final class TextWatermarkOp extends GeneralOp {
    public TextWatermarkOp(String text) {
        super("watermark", 2);
        put("text", UrlSafeBase64.encodeToString(text));
    }

    /*
    * watermark/2
         /text/<encodedText>
         /font/<encodedFontName>
         /fontsize/<fontSize>
         /fill/<encodedTextColor>
         /dissolve/<dissolve>
         /gravity/<gravity>
         /dx/<distanceX>
         /dy/<distanceY>*/

    public TextWatermarkOp font(String name) {
        put("font", UrlSafeBase64.encodeToString(name));
        return this;
    }

    public TextWatermarkOp fontSize(int size) {
        put("fontsize", size);
        return this;
    }

    public TextWatermarkOp fill(String color) {
        put("fill", UrlSafeBase64.encodeToString(color));
        return this;
    }

    public TextWatermarkOp dissolve(int val) {
        put("dissolve", val);
        return this;
    }

    public TextWatermarkOp gravity(Gravity gravity) {
        put("gravity", gravity);
        return this;
    }

    public TextWatermarkOp distanceX(int x) {
        put("dx", x);
        return this;
    }

    public TextWatermarkOp distanceY(int y) {
        put("dy", y);
        return this;
    }
}
