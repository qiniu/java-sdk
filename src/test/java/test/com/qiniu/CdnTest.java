package test.com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringMap;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by bailong on 16/9/21.
 */
public class CdnTest {

    private String getDate(int daysBefore) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -daysBefore);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }

    @Test
    public void testRefresh() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.RefreshResult r = null;
        try {
            r = c.refreshUrls(new String[]{"http://javasdk.qiniudn.com/gopher.jpg"});
            Assert.assertEquals(200, r.code);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPrefetch() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.PrefetchResult r = null;
        try {
            r = c.prefetchUrls(new String[]{"http://javasdk.qiniudn.com/gopher.jpg"});
            Assert.assertEquals(200, r.code);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetBandwidth() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.BandwidthResult r = null;
        String[] domains = {TestConfig.testDomain_z0};
        String startDate = getDate(3);
        String endDate = getDate(1);
        String granularity = "day";
        try {
            r = c.getBandwidthData(domains, startDate, endDate, granularity);
            Assert.assertEquals(200, r.code);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetFlux() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.FluxResult r = null;
        String[] domains = {TestConfig.testDomain_z0};
        String startDate = getDate(3);
        String endDate = getDate(1);

        String granularity = "day";
        try {
            r = c.getFluxData(domains, startDate, endDate, granularity);
            Assert.assertEquals(200, r.code);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetCdnLogList() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.LogListResult r = null;
        String[] domains = {TestConfig.testDomain_z0};
        String logDate = getDate(2);

        try {
            r = c.getCdnLogList(domains, logDate);
            Assert.assertEquals(true, r.data.size() >= 0);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple1() {
        String host = "http://video.example.com";
        String fileName = "2017/01/07/test.png";

        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "xxx";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    null, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple2() {
        String host = "http://video.example.com";
        String fileName = "基本概括.mp4";
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "xxx";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    null, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }


    @Test
    public void testCreateTimestampAntiLeechUrlWithQueryString1() {
        String host = "http://video.example.com";
        String fileName = "2017/01/07/test.png";
        StringMap queryStringMap = new StringMap();
        queryStringMap.put("name", "七牛");
        queryStringMap.put("year", 2017);
        queryStringMap.put("年龄", 28);
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "xxx";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    queryStringMap, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlWithQueryString2() {
        String host = "http://video.example.com";
        String fileName = "基本概括.mp4";
        StringMap queryStringMap = new StringMap();
        queryStringMap.put("name", "七牛");
        queryStringMap.put("year", 2017);
        queryStringMap.put("年龄", 28);
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "xxx";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    queryStringMap, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple3() throws MalformedURLException {
        String host = "http://xxx.yyy.com";
        String fileName = "DIR1/dir2/vodfile.mp4";
        StringMap queryStringMap = new StringMap();
        queryStringMap.put("name", "七牛");
        queryStringMap.put("year", 2017);
        queryStringMap.put("年龄", 28);
        long deadline = 1438358400;
        String encryptKey = "12345678";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    queryStringMap, encryptKey, deadline);
            Assert.assertTrue(signedUrl, signedUrl.indexOf("19eb212771e87cc3d478b9f32d6c7bf9&t=55bb9b80") > -1);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple4() throws MalformedURLException {
        String host = "http://xxx.yyy.com";
        String fileName = "DIR1/中文/vodfile.mp4";
        StringMap queryStringMap = new StringMap();
        queryStringMap.put("name", "七牛");
        queryStringMap.put("year", 2017);
        queryStringMap.put("年龄", 28);
        long deadline = 1438358400;
        String encryptKey = "12345678";
        String signedUrl;
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(host, fileName,
                    queryStringMap, encryptKey, deadline);
            Assert.assertTrue(signedUrl, signedUrl.indexOf("sign=6356bca0d2aecf7211003e468861f5ea&t=55bb9b80") > -1);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple5() throws MalformedURLException {
        URL url = new URL("http://xxx.yyy.com/DIR1/dir2/vodfile.mp4");
        long deadline = 1438358400;
        String encryptKey = "12345678";
        String signedUrl;
        String ret = "http://xxx.yyy.com/DIR1/dir2/vodfile.mp4?sign=19eb212771e87cc3d478b9f32d6c7bf9&t=55bb9b80";
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(url, encryptKey, deadline);
            Assert.assertEquals(ret, signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple6() throws MalformedURLException {
        URL url = new URL("http://xxx.yyy.com/DIR1/dir2/vodfile.mp4?v=1.1");
        long deadline = 1438358400;
        String encryptKey = "12345678";
        String signedUrl;
        // CHECKSTYLE:OFF
        String ret = "http://xxx.yyy.com/DIR1/dir2/vodfile.mp4?v=1.1&sign=19eb212771e87cc3d478b9f32d6c7bf9&t=55bb9b80";
        // CHECKSTYLE:ON
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(url, encryptKey, deadline);
            Assert.assertEquals(ret, signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateTimestampAntiLeechUrlSimple7() throws MalformedURLException {
        URL url = new URL("http://xxx.yyy.com/DIR1/中文/vodfile.mp4?v=1.2");
        long deadline = 1438358400;
        String encryptKey = "12345678";
        String signedUrl;
        // CHECKSTYLE:OFF
        String ret = "http://xxx.yyy.com/DIR1/%E4%B8%AD%E6%96%87/vodfile.mp4?v=1.2&sign=6356bca0d2aecf7211003e468861f5ea&t=55bb9b80";
        // CHECKSTYLE:ON
        try {
            signedUrl = CdnManager.createTimestampAntiLeechUrl(url, encryptKey, deadline);
            Assert.assertEquals(ret, signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
