package com.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;


public class HttpTest {
    private Client httpManager = new Client();

    @Test
    public void testPost1() {
        Response r = null;
        try {
            r = httpManager.post("http://www.baidu.com", "hello", null);
            Assert.assertNull(r.reqId);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPost2() {
        Response r = null;
        try {
            r = httpManager.post("http://up.qiniu.com", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertNotNull(e.response.reqId);
        }
    }

    @Test
    public void testPost3() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/500", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertEquals(500, e.code());
        }
    }

    @Test
    public void testPost4() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/418", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertEquals(418, e.code());
        }
    }

    @Test
    public void testPost5() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/298", "hello", null);
            Assert.assertEquals(298, r.statusCode);
        } catch (QiniuException e) {
            Assert.fail();
        }
    }
}
