package com.qiniu.common;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simon on 6/22/16.
 */
public class AutoZoneTest extends TestCase {

    @Test
    public void testHttp() {
        Map<String, String[]> cases = new HashMap<String, String[]>();
        cases.put(TestConfig.testBucket_z0,
                new String[]{"http://up.qiniu.com",
                        "http://upload.qiniu.com",
                        "https://up.qbox.me"});
        cases.put(TestConfig.testBucket_na0,
                new String[]{"http://up-na0.qiniu.com",
                        "http://upload-na0.qiniu.com",
                        "https://up-na0.qbox.me"});

        for (Map.Entry<String, String[]> entry : cases.entrySet()) {
            String bucket = entry.getKey();
            String[] domains = entry.getValue();
            try {
                AutoZone zone = AutoZone.instance;
                AutoZone.ZoneInfo zoneInfo = zone.queryZoneInfo(TestConfig.testAccessKey,
                        bucket);
                assertEquals(zoneInfo.upHttp, domains[0]);
                assertEquals(zoneInfo.upBackupHttp, domains[1]);
                assertEquals(zoneInfo.upHttps, domains[2]);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testHttpFail() {
        try {
            AutoZone zone = AutoZone.instance;
            zone.queryZoneInfo(TestConfig.dummyAccessKey, TestConfig.dummyBucket);
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertEquals(e.code(), 612);
        }
    }

    @Test
    public void testSplitE() {
        String s1 = "bkt:key";
        String s2 = "bkt";
        Assert.assertEquals(s1.split(":")[0], s2.split(":")[0]);
    }

    @Test
    public void testC1() {
        try {
            AutoZone.ZoneInfo info = AutoZone.instance.queryZoneInfo(TestConfig.testAccessKey,
                    TestConfig.testBucket_z0);
            System.out.println("zone0: " + info.toString());

            AutoZone.ZoneInfo info2 = AutoZone.instance.queryZoneInfo(TestConfig.testAccessKey,
                    TestConfig.testBucket_z0);
            Assert.assertSame(info, info2);

        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    public void testZ() {
        Zone z1 = new Zone.Builder(Zone.zone0()).upHttp("http://uphttp").build();
        Assert.assertSame(z1.getUpHttp(null), "http://uphttp");
        Assert.assertSame(Zone.zone0().getUpHttp(null), "http://upload.qiniup.com");
    }
}
