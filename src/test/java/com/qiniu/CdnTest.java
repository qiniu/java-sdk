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
}
