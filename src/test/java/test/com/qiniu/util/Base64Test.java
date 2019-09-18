package test.com.qiniu.util;

import com.qiniu.util.UrlSafeBase64;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class Base64Test {
    @Test
    public void testEncode() throws UnsupportedEncodingException {
        String data = "你好/+=";
        String result = UrlSafeBase64.encodeToString(data);
        assertEquals("5L2g5aW9Lys9", result);
    }

    @Test
    public void testEmpty() {
        String base64Empty = UrlSafeBase64.encodeToString("");
        Assert.assertEquals("", base64Empty);
    }
}
