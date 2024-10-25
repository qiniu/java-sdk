package com.qiniu.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Simon on 6/22/16.
 */
public class AutoZoneTest {

    @Test
    @Tag("IntegrationTest")
    public void testHttp() {

        String bucket = TestConfig.testBucket_z0;
        try {
            AutoZone zone = AutoZone.instance;
            AutoZone.ZoneInfo zoneInfo = zone.queryZoneInfo(TestConfig.testAccessKey, bucket);
            assertTrue(zoneInfo.upHttp.contains("http://"));
            assertTrue(zoneInfo.upBackupHttp.contains("http://"));
            assertTrue(zoneInfo.upHttps.contains("https://"));
            assertTrue(zoneInfo.upBackupHttps.contains("https://"));
        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testHttpFail() {
        try {
            AutoZone zone = AutoZone.instance;
            zone.queryZoneInfo(TestConfig.dummyAccessKey, TestConfig.dummyBucket);
            fail();
        } catch (QiniuException e) {
            assertEquals(e.code(), 612);
        }
    }

    @Test
    @Tag("UnitTest")
    public void testSplitE() {
        String s1 = "bkt:key";
        String s2 = "bkt";
        assertEquals(s1.split(":")[0], s2.split(":")[0]);
    }

    @Test
    @Tag("IntegrationTest")
    public void testC1() {
        try {
            AutoZone.ZoneInfo info = AutoZone.instance.queryZoneInfo(TestConfig.testAccessKey,
                    TestConfig.testBucket_z0);
            System.out.println("zone0: " + info.toString());

            AutoZone.ZoneInfo info2 = AutoZone.instance.queryZoneInfo(TestConfig.testAccessKey,
                    TestConfig.testBucket_z0);
            assertSame(info, info2);

        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    @Tag("UnitTest")
    public void testZ() {
        Zone z1 = new Zone.Builder(Zone.zone0()).upHttp("http://uphttp").build();
        assertSame(z1.getUpHttp(null), "http://uphttp");
        assertSame(Zone.zone0().getUpHttp(null), "http://upload.qiniup.com");
    }
}
