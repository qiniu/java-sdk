package com.qiniu.common;

import test.com.qiniu.TestConfig;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simon on 6/22/16.
 */
public class AutoZoneTest {

    @Test
    @Tag("IntegrationTest")
    public void testHttp() {
        Map<String, String[]> cases = new HashMap<String, String[]>();
        cases.put(TestConfig.testBucket_z0,
                new String[] { "http://up.qiniu.com", "http://upload.qiniu.com", "https://up.qbox.me" });
        cases.put(TestConfig.testBucket_na0,
                new String[] { "http://up-na0.qiniu.com", "http://upload-na0.qiniu.com", "https://up-na0.qbox.me" });

        for (Map.Entry<String, String[]> entry : cases.entrySet()) {
            String bucket = entry.getKey();
            String[] domains = entry.getValue();
            try {
                AutoZone zone = AutoZone.instance;
                AutoZone.ZoneInfo zoneInfo = zone.queryZoneInfo(TestConfig.testAccessKey, bucket);
                assertEquals(zoneInfo.upHttp, domains[0]);
                assertEquals(zoneInfo.upBackupHttp, domains[1]);
                assertEquals(zoneInfo.upHttps, domains[2]);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
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
