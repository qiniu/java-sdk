package com.qiniu.common;

import com.qiniu.TestConfig;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Simon on 6/22/16.
 */
public class AutoZoneTest {
    private String ak = TestConfig.ak;
    private String bkt = TestConfig.bucket;

    @Test
    public void testHttp() {
        try {
            AutoZone zone = AutoZone.instance;
            AutoZone.ZoneInfo zoneInfo = zone.zoneInfo(ak, bkt);
            assertEquals(zoneInfo.upHttp, "http://up.qiniu.com");
            assertEquals(zoneInfo.upBackupHttp, "http://upload.qiniu.com");
            assertEquals(zoneInfo.upHttps, "https://up.qbox.me");
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        }
    }

    @Test
    public void testHttpFail() {
        try {
            AutoZone zone = AutoZone.instance;
            AutoZone.ZoneInfo zoneInfo = zone.zoneInfo(ak + "_not_be_ak", bkt);
            Assert.fail();
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
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
            AutoZone.ZoneInfo info = AutoZone.instance.zoneInfo(ak, bkt);
            System.out.println("zone0: " + info.toString());

            AutoZone.ZoneInfo info2 = AutoZone.instance.zoneInfo(ak, bkt);
            Assert.assertSame(info, info2);

        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        }
    }
}
