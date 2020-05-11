package test.com.qiniu.util;

import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrlComposerTest {
    @Test
    public void testComposeUrlWithQueries() {
        String testUrl = "http://sms.qiniuapi.com/v1/signature";
        StringMap queryMap = new StringMap().put("page", 1).put("page_size", 10);
        String url = UrlUtils.composeUrlWithQueries(testUrl, queryMap);
        assertTrue(url.equals("http://sms.qiniuapi.com/v1/signature?page=1&page_size=10")
                || url.equals("http://sms.qiniuapi.com/v1/signature?page_size=10&page=1"));
    }

    @Test
    public void testNonEncoding() {
        String u = "http://asfd.clouddn.com/s共df/_-+./*/~/@/:/!/$/&/&amp;/'/(/)/*/+/,/;\"/=/ /"
                + "sdf/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/,/;\"/=/ /?sdfr=34sdf";
        String f = "http://asfd.clouddn.com/s%E5%85%B1df/_-+./%2A/~/@/:/%21/$/&/&amp;/%27/%28/%29/%2A/+/,/;%22/=/%20/"
                + "sdf/%2A/~/@/:/%21/$/&/&amp;/%27/%28/%29/%2A/+/,/;%22/=/%20/?sdfr=34sdf";
        String c = UrlUtils.urlEncode(u, UrlUtils.PLUS_NON_ENCODING); // ~@:$&+,;=/?     no !'()*
        String cdefault = UrlUtils.urlEncode(u); // use UrlUtils.PLUS_NON_ENCODING
        assertEquals(c, cdefault);
        assertTrue(c.indexOf("%20") > -1 && c.indexOf(" ") == -1);
        assertFalse(java.util.regex.Pattern.compile("[!'()*]").matcher(f).find());
        assertEquals(f, c);

        String f2 = "http://asfd.clouddn.com/s%E5%85%B1df/_-+./*/~/@/:/!/$/&/&amp;/'/(/)/*/+/,/;%22/=/%20/"
                + "sdf/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/,/;%22/=/%20/?sdfr=34sdf";
        String c2 = UrlUtils.urlEncode(u, UrlUtils.PLUS_NON_ENCODING2); // ~@:!$&'()*+,;=/?
        assertEquals(f2, c2);
        assertTrue(java.util.regex.Pattern.compile("[!'()*]").matcher(f2).find());

        assertNotEquals(c, c2);
        String c3 = c.replaceAll("%21", "!")
                .replaceAll("%27", "'")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")")
                .replaceAll("%2A", "*");
        assertEquals(c2, c3);
    }

}
