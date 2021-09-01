package test.com.qiniu.util;

import com.qiniu.util.UrlSafeBase64;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class Base64Test {
    @Test
    @Tag("UnitTest")
    public void testEncode() throws UnsupportedEncodingException {
        String data = "你好/+=";
        String result = UrlSafeBase64.encodeToString(data);
        assertEquals("5L2g5aW9Lys9", result);
    }

    @Test
    @Tag("UnitTest")
    public void testEmpty() {
        String base64Empty = UrlSafeBase64.encodeToString("");
        assertEquals("", base64Empty);
    }
}
