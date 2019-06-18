package test.com.qiniu.util;

import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UrlComposerTest {
    @Test
    public void testComposeUrlWithQueries() {
        String testUrl = "http://sms.qiniuapi.com/v1/signature";
        StringMap queryMap = new StringMap().put("page", 1).put("page_size", 10);
        String url = UrlUtils.composeUrlWithQueries(testUrl, queryMap);
        assertTrue(url.equals("http://sms.qiniuapi.com/v1/signature?page=1&page_size=10")
                || url.equals("http://sms.qiniuapi.com/v1/signature?page_size=10&page=1"));
    }

}
