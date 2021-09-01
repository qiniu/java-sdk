package test.com.qiniu;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.ProxyConfiguration;
import com.qiniu.http.Response;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HttpTest {
    private Client httpManager = new Client();

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPost1() {
        Response r = null;
        try {
            r = httpManager.post("http://www.baidu.com", "hello", null);
            assertNull(r.reqId);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPost2() {
        Response r = null;
        try {
            r = httpManager.post("http://upload.qiniu.com", "hello", null);
            fail();
        } catch (QiniuException e) {
            assertNotNull(e.response.reqId);
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPost3() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/500", "hello", null);
            fail();
        } catch (QiniuException e) {
            if (e.code() != -1) {
                assertEquals(500, e.code());
            } else {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPost4() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/418", "hello", null);
            fail();
        } catch (QiniuException e) {
            if (e.code() != -1) {
                assertEquals(418, e.code());
            } else {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPost5() {
        Response r = null;
        try {
            r = httpManager.post("http://httpbin.org/status/298", "hello", null);
            if (r.statusCode != -1) {
                assertEquals(298, r.statusCode);
            }
        } catch (QiniuException e) {
            if (e.code() != -1) {
                fail();
            }
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testProxy() {
        ProxyConfiguration proxy = new ProxyConfiguration("115.231.183.168", 80);
        Client c = new Client(null, false, proxy, Constants.CONNECT_TIMEOUT, Constants.READ_TIMEOUT,
                Constants.WRITE_TIMEOUT, Constants.DISPATCHER_MAX_REQUESTS, Constants.DISPATCHER_MAX_REQUESTS_PER_HOST,
                Constants.CONNECTION_POOL_MAX_IDLE_COUNT, Constants.CONNECTION_POOL_MAX_IDLE_MINUTES);
        Response r = null;
        try {
            r = c.post("http://upproxy1.qiniu.com", "hello", null);
            fail();
        } catch (QiniuException e) {
            assertNotNull(e.response.reqId);
            assertEquals(e.response.statusCode, 400);
        }
    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testTimeout() throws NoSuchFieldException, IllegalAccessException {
        Client client0 = new Client();
        try {
            Response res = client0.get("https://www.qiniu.com/?v=12345");
            String r = res.toString();
            System.out.println(r);
            assertTrue(r.indexOf(":443") > 0, "https, must have port 443");
        } catch (QiniuException e) {
            e.printStackTrace();
            fail("should be ok");
        }

        try {
            Response res = client0.get("https://www.qiniu.com/?v=12345");
            String r = res.toString();
            System.out.println(r);
            assertTrue(r.indexOf(":443") > 0, "https, must have port 443");
        } catch (QiniuException e) {
            e.printStackTrace();
            fail("should be ok");
        }

        try {
            Response res = client0.get("https://www.qiniu.com/?v=12345");
            String r = res.toString();
            System.out.println(r);
            assertTrue(r.indexOf(":443") > 0, "https, must have port 443");
        } catch (QiniuException e) {
            e.printStackTrace();
            fail("should be ok");
        }

        Client client = new Client();
        Field field = client.getClass().getDeclaredField("httpClient");
        field.setAccessible(true);
        OkHttpClient okHttpClient = (OkHttpClient) field.get(client);
        okHttpClient = okHttpClient.newBuilder().connectTimeout(1, TimeUnit.MILLISECONDS).build();
        field.set(client, okHttpClient);

        try {
            client.get("http://rs.qbox.me/?v=12");
            fail("should be timeout");
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().indexOf(":80") > 10, "http, must have port 80");
        }
        try {
            client.get("http://rs.qbox.me/?v=12");
            fail("should be timeout");
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().indexOf(":80") > 10, "http, must have port 80");
        }
        try {
            client.get("http://rs.qbox.me/?v=12");
            fail("should be timeout");
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().indexOf(":80") > 10, "http, must have port 80");
        }
        try {
            client.get("https://rs.qbox.me/?v=we");
            fail("should be timeout");
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().indexOf(":443") > 10, "https, must have port 443");
        }

        long start = new Date().getTime();
        try {
            Response response = client.get("http://uc.qbox.me/?v=543");
            long end = new Date().getTime();
            fail("should be timeout," + " duration:" + (end - start) + " detail:" + response);
        } catch (QiniuException e) {
            e.printStackTrace();
            long end = new Date().getTime();
            assertTrue(e.getMessage().indexOf(":80") > 10,
                    "http, must have port 80," + " duration:" + (end - start) + "detail:" + e.getMessage());
        }
        try {
            Response response = client.get("https://uc.qbox.me/?v=kgd");
            fail("should be timeout, detail:" + response);
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().indexOf(":443") > 10, "https, must have port 443, detail:" + e.getMessage());
        }
    }
}
