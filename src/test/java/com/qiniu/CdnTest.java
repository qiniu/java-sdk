package com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by bailong on 16/9/21.
 */
public class CdnTest {
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
        String startDate = "2017-01-01";
        String endDate = "2017-01-06";
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
        String startDate = "2017-01-01";
        String endDate = "2017-01-06";
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
        String logDate = "2017-01-01";

        try {
            r = c.getCdnLogList(domains, logDate);
            Assert.assertEquals(true, r.data.size() > 0);
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
}
