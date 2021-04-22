package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadPrivateCloudUrl;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DownloadUrlTest {

    @Test
    public void testUrl() {
        String key = TestConfig.testChineseKey_na0;
        String domain = TestConfig.testDomain_na0;

        String attname = "download" + key;
        String fop = "imageView2/2/w/320/h/480";
        String style = "iphone";
        String styleSeparator = "-";
        String styleParam = "";
        String customQueryKey = "Key";
        String customQueryValue = "Value";
        try {
            String url = new DownloadUrl(domain, false, key)
                    .setAttname(attname).setFop(fop).setStyle(style, styleSeparator, styleParam)
                    .addCustomQuery(customQueryKey, customQueryValue)
                    .buildURL();
            System.out.println("create url:" + url);
            testHasAuthority(url);

            url = new DownloadUrl(domain, true, key)
                    .setAttname(attname).setFop(fop).setStyle(style, styleSeparator, styleParam)
                    .addCustomQuery(customQueryKey, customQueryValue)
                    .buildURL();
            Assert.assertTrue("url:" + url, url.contains("https://"));
        } catch (QiniuException e) {
            Assert.assertTrue(e.error(), false);
        }
    }

    @Test
    public void testSpecialKey() {
        String domain = "abc.com:123";
        Map<String, String> keys = new HashMap<String, String>() {{
            put("", "");
            put("abc_def.mp4", "abc_def.mp4");
            put("/ab/cd", "/ab/cd");
            put("ab/中文/de", "ab/%E4%B8%AD%E6%96%87/de");
            put("ab+-*de f", "ab%2B-%2Ade%20f");
            put("ab:cd", "ab%3Acd");
            put("ab@cd", "ab%40cd");
            put("ab?cd=ef", "ab%3Fcd%3Def");
            put("ab#e~f", "ab%23e~f");
            put("ab//cd", "ab//cd");
            put("abc%2F%2B", "abc%252F%252B");
            put("ab cd", "ab%20cd");
            put("ab/c:d?e#f//gh汉子", "ab/c%3Ad%3Fe%23f//gh%E6%B1%89%E5%AD%90");
        }};

        for (String key : keys.keySet()) {
            String encodeKey = keys.get(key);
            try {
                String url = new DownloadUrl(domain, false, key).buildURL();
                String exceptUrl = "http://" + domain + "/" + encodeKey;
                Assert.assertEquals("url:" + url + " exceptUrl:" + exceptUrl, exceptUrl, url);
            } catch (QiniuException e) {
                Assert.assertTrue(e.error(), false);
            }
        }


    }

    @Test
    public void testUrlWithDeadline() {
        String key = TestConfig.testKey_na0;
        String domain = TestConfig.testPrivateBucketDomain_na0;
        Auth auth = TestConfig.testAuth;

        try {
            long expire = 10;
            long deadline = new Date().getTime() / 1000 + expire;
            String url = new DownloadUrl(domain, false, key).buildURL(auth, deadline);
            System.out.println("create url:" + url);
            Client client = new Client();
            Response response = client.get(url);
            Assert.assertTrue(response.toString(), response.isOK());

            try {
                Thread.sleep((expire + 5) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testNoAuthority(url);
        } catch (QiniuException e) {
            Assert.assertTrue(e.response.toString(), false);
        }
    }

    private void testNoAuthority(String url) {
        try {
            Client client = new Client();
            Response response = client.get(url);
            Assert.assertFalse(url, response.isOK());
        } catch (QiniuException e) {
            Assert.assertNotNull("except no authority:" + url + "\n but no response:" + e, e.response);
            Assert.assertTrue("except no authority:" + url + "\n but:" + e.response, e.response.statusCode == 401);
        }
    }

    private void testHasAuthority(String url) {
        try {
            Client client = new Client();
            Response response = client.get(url);
            Assert.assertTrue(url, response.isOK());
        } catch (QiniuException e) {
            Assert.assertTrue("except has authority:" + url + "\n response:" + e.response, false);
        }
    }

    @Test
    public void testPrivateCloudUrl() {
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            String key = file.getKey();
            String bucket = file.getBucketName();
            String domain = file.getTestDomain();

            String attname = "test_file.jpg";
            String fop = "imageView2/2/w/320/h/480";
            String style = "iphone";
            String styleSeparator = "-";
            try {
                DownloadPrivateCloudUrl downloadUrl = new DownloadPrivateCloudUrl(domain, false, bucket, key, TestConfig.testAccessKey);
                String url = downloadUrl.setAttname(attname).setFop(fop).setStyle(style, styleSeparator, null).buildURL();
                String urlExpire = "http://" + domain + "/getfile/" + TestConfig.testAccessKey + "/" + bucket + "/" + key + URLEncoder.encode(styleSeparator) + URLEncoder.encode(style) + "?" + URLEncoder.encode(fop) + "&attname=" + URLEncoder.encode(attname);
                System.out.println("create url:" + url + " expire url:" + urlExpire);
                Assert.assertEquals("create url:" + url + " expire url:" + urlExpire, urlExpire, url);

                downloadUrl = new DownloadPrivateCloudUrl(domain, true, bucket, key, TestConfig.testAccessKey);
                url = downloadUrl.setAttname(attname).setFop(fop).setStyle(style, styleSeparator, null).buildURL();
                urlExpire = "https://" + domain + "/getfile/" + TestConfig.testAccessKey + "/" + bucket + "/" + key + URLEncoder.encode(styleSeparator) + URLEncoder.encode(style) + "?" + URLEncoder.encode(fop) + "&attname=" + URLEncoder.encode(attname);
                System.out.println("create url:" + url + " expire url:" + urlExpire);
                Assert.assertEquals("create url:" + url + " expire url:" + urlExpire, urlExpire, url);


                Configuration cfg = new Configuration();
                cfg.useHttpsDomains = false;
                String host = cfg.ioHost(TestConfig.testAccessKey, bucket);


                downloadUrl = new DownloadPrivateCloudUrl(cfg, bucket, key, TestConfig.testAccessKey);
                url = downloadUrl.setAttname(attname).setFop(fop).setStyle(style, styleSeparator, null).buildURL();
                urlExpire = host + "/getfile/" + TestConfig.testAccessKey + "/" + bucket + "/" + key + URLEncoder.encode(styleSeparator) + URLEncoder.encode(style) + "?" + URLEncoder.encode(fop) + "&attname=" + URLEncoder.encode(attname);
                System.out.println("create url:" + url + " expire url:" + urlExpire);
                Assert.assertEquals("create url:" + url + " expire url:" + urlExpire, urlExpire, url);


                cfg.useHttpsDomains = true;
                host = cfg.ioHost(TestConfig.testAccessKey, bucket);
                downloadUrl = new DownloadPrivateCloudUrl(cfg, bucket, key, TestConfig.testAccessKey);
                url = downloadUrl.setAttname(attname).setFop(fop).setStyle(style, styleSeparator, null).buildURL();
                urlExpire = host + "/getfile/" + TestConfig.testAccessKey + "/" + bucket + "/" + key + URLEncoder.encode(styleSeparator) + URLEncoder.encode(style) + "?" + URLEncoder.encode(fop) + "&attname=" + URLEncoder.encode(attname);
                System.out.println("create url:" + url + " expire url:" + urlExpire);
                Assert.assertEquals("create url:" + url + " expire url:" + urlExpire, urlExpire, url);
            } catch (QiniuException e) {
                Assert.assertTrue(e.error(), false);
            }
        }
    }
}
