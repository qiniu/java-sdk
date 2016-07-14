package com.qiniu;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.DnsClient;
import qiniu.happydns.IResolver;
import qiniu.happydns.local.Hosts;
import qiniu.happydns.local.Resolver;
import qiniu.happydns.local.SystemDnsServer;

import java.io.IOException;
import java.net.InetAddress;


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
    public void testDns() {
        IResolver r1 = SystemDnsServer.defaultResolver();
        IResolver r2 = null;
        try {
            r2 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Hosts h = new Hosts();
        h.put("upnonodns.qiniu.com", "115.231.183.168");
        Config.dns = new DnsClient(new IResolver[]{r1, r2}, h);
        Response r = null;
        try {
            r = new Client().post("http://upnonodns.qiniu.com", "hello", null);
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
