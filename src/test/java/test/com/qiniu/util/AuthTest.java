package test.com.qiniu.util;

import com.qiniu.http.Client;
import com.qiniu.http.Headers;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    @Test
    @Tag("UnitTest")
    public void testSign() {
        String token = TestConfig.dummyAuth.sign("test");
        assertEquals("abcdefghklmnopq:mSNBTR7uS2crJsyFr2Amwv1LaYg=", token);
    }

    @Test
    @Tag("UnitTest")
    public void testSignWithData() {
        String token = TestConfig.dummyAuth.signWithData("test");
        assertEquals("abcdefghklmnopq:-jP8eEV9v48MkYiBGs81aDxl60E=:dGVzdA==", token);
    }

    @Test
    @Tag("UnitTest")
    public void testSignRequest() {
        String token = TestConfig.dummyAuth.signRequest("http://www.qiniu.com?go=1", "test".getBytes(), "");
        assertEquals("abcdefghklmnopq:cFyRVoWrE3IugPIMP5YJFTO-O-Y=", token);
        token = TestConfig.dummyAuth.signRequest("http://www.qiniu.com?go=1", "test".getBytes(), Client.FormMime);
        assertEquals("abcdefghklmnopq:svWRNcacOE-YMsc70nuIYdaa1e4=", token);
    }

    @Test
    @Tag("UnitTest")
    public void testPrivateDownloadUrl() {
        String url = TestConfig.dummyAuth.privateDownloadUrlWithDeadline("http://www.qiniu.com?go=1",
                1234567890 + 3600);
        String expect = "http://www.qiniu.com?go=1&e=1234571490&token=abcdefghklmnopq:8vzBeLZ9W3E4kbBLFLW0Xe0u7v4=";
        assertEquals(expect, url);
    }

    @Test
    @Tag("UnitTest")
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
    @Tag("UnitTest")
    public void testUploadToken() {
        StringMap policy = new StringMap().put("endUser", "y");
        String token = TestConfig.dummyAuth.uploadTokenWithDeadline("1", "2", 1234567890L + 3600, policy, false);
        // CHECKSTYLE:OFF
        String exp = "abcdefghklmnopq:yyeexeUkPOROoTGvwBjJ0F0VLEo=:eyJlbmRVc2VyIjoieSIsInNjb3BlIjoiMToyIiwiZGVhZGxpbmUiOjEyMzQ1NzE0OTB9";
        // CHECKSTYLE:ON
        assertEquals(exp, token);
    }

    @Test
    @Tag("UnitTest")
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
    @Tag("UnitTest")
    public void testQiniuAuthorization() {
        try {
            Method isDisableQiniuTimestampSignatureMethod = Auth.class.getDeclaredMethod("isDisableQiniuTimestampSignature");
            isDisableQiniuTimestampSignatureMethod.setAccessible(true);
            Boolean isDisableQiniuTimestampSignature = (Boolean) isDisableQiniuTimestampSignatureMethod.invoke(null);
            if (!isDisableQiniuTimestampSignature) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


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
        // System.out.println(sign + ": " + s + ": " + sign.equals(s) + "\n\n");
        assertEquals(sign, s);
        assertTrue(auth.isValidCallback("Qiniu " + sign, new Auth.Request(url, method, headers, body)), "checkSignQiniuValid fail, sign:" + sign);
    }

    private void checkSignQbox(String sign, Auth auth, String url, String method, Headers headers, byte[] body) {
        String contentType = headers.get("Content-Type");
        String s = auth.signRequest(url, body, contentType);
        // System.out.println(sign + ": " + s + ": " + sign.equals(s) + "\n\n");
        assertEquals(sign, s);
        assertTrue(auth.isValidCallback("QBox " + sign, new Auth.Request(url, method, headers, body)), "checkSignQiniuValid fail, sign:" + sign);
    }


    @Test
    @Tag("UnitTest")
    public void testSignQiniu() {
        // copy from go sdk
        // https://github.com/qiniu/api.v7/blob/master/auth/credentials_test.go
        // TestAuthSignRequestV2
        // if Content-Type is nil or "", set it with application/x-www-form-urlencoded
        // in go sdk
        //
        Auth auth = Auth.create("ak", "sk");
        Charset utf8 = Charset.forName("UTF-8");

        String qiniuSign = "ak:0i1vKClRDWFyNkcTFzwcE7PzX74=";
        String qboxSign = "ak:NJolr7PWLYeqLKbDXI_LxhMUaw4=";
        String url = "";
        String method = "";
        Headers headers = new Headers.Builder().add("X-Qiniu-", "a").add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded").build();
        byte[] body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:K1DI0goT05yhGizDFE5FiPJxAj4=";
        qboxSign = "ak:qfWnqF1E_vfzjZnReCVkcSMl29M=";
        url = "";
        method = "";
        headers = new Headers.Builder().add("Content-Type", "application/json").build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:0i1vKClRDWFyNkcTFzwcE7PzX74=";
        qboxSign = "ak:NJolr7PWLYeqLKbDXI_LxhMUaw4=";
        url = "";
        method = "GET";
        headers = new Headers.Builder().add("X-Qiniu-", "a").add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded").build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:0ujEjW_vLRZxebsveBgqa3JyQ-w=";
        qboxSign = "ak:qfWnqF1E_vfzjZnReCVkcSMl29M=";
        url = "";
        method = "POST";
        headers = new Headers.Builder().add("Content-Type", "application/json").add("X-Qiniu", "b").build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:GShw5NitGmd5TLoo38nDkGUofRw=";
        qboxSign = "ak:NJolr7PWLYeqLKbDXI_LxhMUaw4=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder().add("X-Qiniu-", "a").add("X-Qiniu", "b")
                .add("Content-Type", "application/x-www-form-urlencoded").build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:_PAgAJVMMQD4SLLXp7f44lS8aTs=";
        qboxSign = "ak:qfWnqF1E_vfzjZnReCVkcSMl29M=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder().set("Content-Type", "application/json").add("X-Qiniu-Bbb", "BBB")
                .add("X-Qiniu-Bbb", "AAA").add("X-Qiniu-Aaa", "DDD").add("X-Qiniu-Aaa", "CCC").add("X-Qiniu-", "a")
                .add("X-Qiniu", "b").build();
        body = "{\"name\": \"test\"}".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:ERR7z4iI_gHYd80GVfCBZBtT3wg=";
        qboxSign = "ak:h8gBb1Adb2Jgoys1N8sRVAnNvpw=";
        url = "http://upload.qiniup.com";
        method = "";
        headers = new Headers.Builder().set("Content-Type", "application/x-www-form-urlencoded")
                .add("X-Qiniu-Bbb", "BBB").add("X-Qiniu-Bbb", "AAA").add("X-Qiniu-Aaa", "DDD").add("X-Qiniu-Aaa", "CCC")
                .add("X-Qiniu-", "a").add("X-Qiniu", "b").build();
        body = "name=test&language=go".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        headers = new Headers.Builder().add("Content-Type", "application/x-www")
                .set("Content-Type", "application/x-www-form-urlencoded").add("X-Qiniu-BBB", "BBB")
                .add("X-Qiniu-bBb", "AAA").add("X-Qiniu-AaA", "DDD").add("X-Qiniu-aAA", "CCC").build();
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        //////////// end of copy test ////

        qiniuSign = "ak:CLpJSqdLC3atDe8vDfUVj8i2bug=";
        qboxSign = "ak:fRfPzux63uUumMU_GRiF0uWdAgE=";
        url = "http://upload.qiniup.com/mkfile/sdf.jpg";
        method = "ak:fRfPzux63uUumMU_GRiF0uWdAgE=";
        headers = new Headers.Builder().set("Content-Type", "application/x-www-form-urlencoded")
                .add("X-Qiniu-Bbb", "BBB").add("X-Qiniu-Bbb", "AAA").add("X-Qiniu-Aaa", "DDD").add("X-Qiniu-Aaa", "CCC")
                .add("X-Qiniu-", "a").add("X-Qiniu", "b").build();
        body = "name=test&language=go".getBytes(utf8);
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);

        qiniuSign = "ak:QGATpJaEx-LEUO01-mVZ8jg1dtk=";
        qboxSign = "ak:K-R81jo5uqFvssxWcCUjZebb8Cw=";
        url = "http://upload.qiniup.com/mkfile/sdf.jpg?s=er3&df";
        checkSignQiniu(qiniuSign, auth, url, method, headers, body);
        checkSignQbox(qboxSign, auth, url, method, headers, body);
    }

    static class Policy {
        String scope;
        long deadline;
        String endUser;
    }
}
