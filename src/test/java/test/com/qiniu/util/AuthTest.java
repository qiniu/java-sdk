package test.com.qiniu.util;

import com.qiniu.http.Client;
import com.qiniu.http.Headers;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.nio.charset.Charset;

import static org.junit.Assert.*;


public class AuthTest {
    @Test
    public void testSign() {
        String token = TestConfig.dummyAuth.sign("test");
        assertEquals("abcdefghklmnopq:mSNBTR7uS2crJsyFr2Amwv1LaYg=", token);
    }

    @Test
    public void testSignWithData() {
        String token = TestConfig.dummyAuth.signWithData("test");
        assertEquals("abcdefghklmnopq:-jP8eEV9v48MkYiBGs81aDxl60E=:dGVzdA==", token);
    }

    @Test
    public void testSignRequest() {
        String token = TestConfig.dummyAuth.signRequest("http://www.qiniu.com?go=1", "test".getBytes(), "");
        assertEquals("abcdefghklmnopq:cFyRVoWrE3IugPIMP5YJFTO-O-Y=", token);
        token = TestConfig.dummyAuth.signRequest("http://www.qiniu.com?go=1", "test".getBytes(), Client.FormMime);
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

    @Test
    public void testUploadToken2() {
        Policy p = new Policy();
        p.endUser = "y";
        p.scope = "1:2";
        p.deadline = 1234567890L + 3600;
        String token = TestConfig.dummyAuth.uploadTokenWithPolicy(p);
        // CHECKSTYLE:OFF
        String exp = "abcdefghklmnopq:zx3NdMGffQ0JhUlgGSU5oeTx9Nk=:eyJzY29wZSI6IjE6MiIsImRlYWRsaW5lIjoxMjM0NTcxNDkwLCJlbmRVc2VyIjoieSJ9";
        // CHECKSTYLE:ON
        assertEquals(exp, token);
    }

    @Test
    public void testQiniuAuthorization() {
        String ak = "MY_ACCESS_KEY";
        String sk = "MY_SECRET_KEY";
        String method = "POST";
        String url = "http://rs.qiniu.com/move/bmV3ZG9jczpmaW5kX21hbi50eHQ=/bmV3ZG9jczpmaW5kLm1hbi50eHQ=";
        Auth auth = Auth.create(ak, sk);
        Headers map = auth.qiniuAuthorization(url, method, null, null);
        String a = map.get("Authorization");
        assertEquals("Qiniu MY_ACCESS_KEY:1uLvuZM6l6oCzZFqkJ6oI4oFMVQ=", a);
    }

    private void checkSignQiniu(String sign, Auth auth, String url, String method, Headers headers, byte[] body) {
        String s = auth.signQiniuAuthorization(url, method, body, headers);
//        System.out.println(sign + ": " + s + ": " + sign.equals(s) +  "\n\n");
        assertEquals(sign, s);
    }

    @Test
    public void testSignQiniu() {
        // copy from go sdk https://github.com/qiniu/api.v7/blob/master/auth/credentials_test.go  TestAuthSignRequestV2
        // if Content-Type is nil or "", set it with application/x-www-form-urlencoded in go sdk
        //
        Auth auth = Auth.create("ak", "sk");
        Charset utf8 = Charset.forName("UTF-8");

        String sign = "ak:0i1vKClRDWFyNkcTFzwcE7PzX74=";
        String url = "";
        String method = "";
        Headers headers = new Headers.Builder()
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded")
                .build();
        byte[] body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:K1DI0goT05yhGizDFE5FiPJxAj4=";
        url = "";
        method = "";
        headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:0i1vKClRDWFyNkcTFzwcE7PzX74=";
        url = "";
        method = "GET";
        headers = new Headers.Builder()
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded")
                .build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:0ujEjW_vLRZxebsveBgqa3JyQ-w=";
        url = "";
        method = "POST";
        headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("X-Qiniu", "b")
                .build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:GShw5NitGmd5TLoo38nDkGUofRw=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder()
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded")
                .build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:N1JHCQfjs0zX00yNP59ajZI41W8=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder()
                .set("Content-Type", "application/json")
                .add("X-Qiniu-Bbb", "BBB")
                .add("X-Qiniu-Bbb", "AAA")
                .add("X-Qiniu-Aaa", "DDD")
                .add("X-Qiniu-Aaa", "CCC")
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:s2cp5btoYoHaraDW2CAGBxj0OvU=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder()
                .set("Content-Type", "application/x-www-form-urlencoded")
                .add("X-Qiniu-Bbb", "BBB")
                .add("X-Qiniu-Bbb", "AAA")
                .add("X-Qiniu-Aaa", "DDD")
                .add("X-Qiniu-Aaa", "CCC")
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .build();
        body = "name=test&language=go".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        headers = new Headers.Builder()
                .add("Content-Type", "application/x-www")
                .set("Content-Type", "application/x-www-form-urlencoded")
                .add("X-Qiniu-BBB", "BBB")
                .add("X-Qiniu-bBb", "AAA")
                .add("X-Qiniu-AaA", "DDD")
                .add("X-Qiniu-aAA", "CCC")
                .build();
        checkSignQiniu(sign, auth, url, method, headers, body);

        ////////////  end of copy test   ////

        sign = "ak:K8d62cW_hqjxQ3RElNz8g3BQHa8=";
        url = "http://upload.qiniup.com/mkfile/sdf.jpg";
        method = "";
        headers = new Headers.Builder()
                .set("Content-Type", "application/x-www-form-urlencoded")
                .add("X-Qiniu-Bbb", "BBB")
                .add("X-Qiniu-Bbb", "AAA")
                .add("X-Qiniu-Aaa", "DDD")
                .add("X-Qiniu-Aaa", "CCC")
                .add("X-Qiniu-", "a")
                .add("X-Qiniu", "b")
                .build();
        body = "name=test&language=go".getBytes(utf8);
        checkSignQiniu(sign, auth, url, method, headers, body);

        sign = "ak:CzOiB_NSxrvMLkhK8hhV_1vTqYk=";
        url = "http://upload.qiniup.com/mkfile/sdf.jpg?s=er3&df";
        checkSignQiniu(sign, auth, url, method, headers, body);
    }

    static class Policy {
        String scope;
        long deadline;
        String endUser;
    }
}
