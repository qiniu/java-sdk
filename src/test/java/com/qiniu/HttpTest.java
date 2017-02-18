package com.qiniu;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.ProxyConfiguration;
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
            r = httpManager.post("http://upload.qiniu.com", "hello", null);
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
        DnsClient dns = new DnsClient(new IResolver[]{r1, r2}, h);
        Client c = new Client(dns, false, null,
                Constants.CONNECT_TIMEOUT, Constants.READ_TIMEOUT, Constants.WRITE_TIMEOUT,
                Constants.DISPATCHER_MAX_REQUESTS, Constants.DISPATCHER_MAX_REQUESTS_PER_HOST,
                Constants.CONNECTION_POOL_MAX_IDLE_COUNT, Constants.CONNECTION_POOL_MAX_IDLE_MINUTES);
        Response r = null;
        try {
            r = c.post("http://upnonodns.qiniu.com", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertNotNull(e.response.reqId);
            Assert.assertEquals(e.response.statusCode, 400);
        }
    }

    @Test
    public void testPost3() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/500", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            if (e.code() != -1) {
                Assert.assertEquals(500, e.code());
            } else {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testPost4() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/418", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            if (e.code() != -1) {
                Assert.assertEquals(418, e.code());
            } else {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testPost5() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/298", "hello", null);
            if (r.statusCode != -1) {
                Assert.assertEquals(298, r.statusCode);
            }
        } catch (QiniuException e) {
            if (e.code() != -1) {
                Assert.fail();
            }
        }
    }

    @Test
    public void testProxy() {
        ProxyConfiguration proxy = new ProxyConfiguration("115.231.183.168", 80);
        Client c = new Client(null, false, proxy,
                Constants.CONNECT_TIMEOUT, Constants.READ_TIMEOUT, Constants.WRITE_TIMEOUT,
                Constants.DISPATCHER_MAX_REQUESTS, Constants.DISPATCHER_MAX_REQUESTS_PER_HOST,
                Constants.CONNECTION_POOL_MAX_IDLE_COUNT, Constants.CONNECTION_POOL_MAX_IDLE_MINUTES);
        Response r = null;
        try {
            r = c.post("http://upproxy1.qiniu.com", "hello", null);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertNotNull(e.response.reqId);
            Assert.assertEquals(e.response.statusCode, 400);
        }
    }
}
