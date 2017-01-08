package com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by bailong on 16/9/21.
 */
public class CdnTest {
    @Test
    public void testRefresh() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        Response r = null;
        try {
            r = c.refreshUrls(new String[]{"http://javasdk.qiniudn.com/gopher.jpg"});
            Assert.assertEquals(200, r.statusCode);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPrefetch() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        Response r = null;
        try {
            r = c.prefetchUrls(new String[]{"http://javasdk.qiniudn.com/gopher.jpg"});
            Assert.assertEquals(200, r.statusCode);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetBandwidth() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        Response r = null;
        String[] domains = {TestConfig.domain};
        String startDate = "2017-01-01";
        String endDate = "2017-01-06";
        String granularity = "day";
        try {
            r = c.getBandwidthData(domains, startDate, endDate, granularity);
            Assert.assertEquals(200, r.statusCode);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetFlux() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        Response r = null;
        String[] domains = {TestConfig.domain};
        String startDate = "2017-01-01";
        String endDate = "2017-01-06";
        String granularity = "day";
        try {
            r = c.getFluxData(domains, startDate, endDate, granularity);
            Assert.assertEquals(200, r.statusCode);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetCdnLogList() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        Response r = null;
        String[] domains = {TestConfig.domain};
        String logDate = "2017-01-01";

        try {
            r = c.getCdnLogList(domains, logDate);
            Assert.assertEquals(200, r.statusCode);
        } catch (QiniuException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateAntileechUrlBasedOnTimestampSimple() {
        String host = "http://img.abc.com";
        String fileName = "2017/01/07/测试.png";
        String queryString = "";
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "";
        String signedUrl;
        try {
            signedUrl = CdnManager.ceateTimestampAntiLeechUrl(host, fileName,
                    queryString, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateAntileechUrlBasedOnTimestampWithQueryString() {
        String host = "http://video.abc.com";
        String fileName = "测试.mp4";
        String queryString = "name=七牛&year=2017";
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        String encryptKey = "";
        String signedUrl;
        try {
            signedUrl = CdnManager.ceateTimestampAntiLeechUrl(host, fileName,
                    queryString, encryptKey, deadline);
            System.out.println(signedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
