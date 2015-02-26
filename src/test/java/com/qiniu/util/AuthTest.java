package com.qiniu.util;

import com.qiniu.TestConfig;
import com.qiniu.http.Client;
import org.junit.Test;

import static org.junit.Assert.*;


public class AuthTest {
    @Test
    public void testToken() {
        String token = TestConfig.dummyAuth.token("test");
        assertEquals("abcdefghklmnopq:mSNBTR7uS2crJsyFr2Amwv1LaYg=", token);
    }

    @Test
    public void testTokenWithData() {
        String token = TestConfig.dummyAuth.tokenWithData("test");
        assertEquals("abcdefghklmnopq:-jP8eEV9v48MkYiBGs81aDxl60E=:dGVzdA==", token);
    }

    @Test
    public void testTokenOfRequest() {
        String token = TestConfig.dummyAuth.tokenOfRequest("http://www.qiniu.com?go=1", "test".getBytes(), "");
        assertEquals("abcdefghklmnopq:cFyRVoWrE3IugPIMP5YJFTO-O-Y=", token);
        token = TestConfig.dummyAuth.tokenOfRequest("http://www.qiniu.com?go=1", "test".getBytes(), Client.FormMime);
        assertEquals("abcdefghklmnopq:svWRNcacOE-YMsc70nuIYdaa1e4=", token);
    }

    @Test
    public void testPrivateDownloadUrl() {
        String url = TestConfig.dummyAuth.privateDownloadUrlWithDeadline("http://www.qiniu.com?go=1",
                1234567890 + 3600);
        String expect = "http://www.qiniu.com?go=1&e=1234571490&token=abcdefghklmnopq:8vzBeLZ9W3E4kbBLFLW0Xe0u7v4=";
        assertEquals(expect, url);
    }

    @Test
    public void testDeprecatedPolicy() {
        StringMap policy = new StringMap().put("asyncOps", 1);
        try {
            TestConfig.dummyAuth.uploadTokenWithDeadline("1", null, 1234567890L + 3600, policy, false);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testUploadToken() {
        StringMap policy = new StringMap().put("endUser", "y");
        String token = TestConfig.dummyAuth.uploadTokenWithDeadline("1", "2", 1234567890L + 3600, policy, false);
        // CHECKSTYLE:OFF
        String exp = "abcdefghklmnopq:yyeexeUkPOROoTGvwBjJ0F0VLEo=:eyJlbmRVc2VyIjoieSIsInNjb3BlIjoiMToyIiwiZGVhZGxpbmUiOjEyMzQ1NzE0OTB9";
        // CHECKSTYLE:ON
        assertEquals(exp, token);
    }
}
