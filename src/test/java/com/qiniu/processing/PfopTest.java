package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class PfopTest extends TestCase {

    private String testMp4FileKey;
    private String testPipeline;

    @Override
    protected void setUp() throws Exception {
        this.testMp4FileKey = "sintel_trailer.mp4";
        this.testPipeline = "demopipe";
    }

    @Test
    public void testAvthumb() {
        Map<String, Zone> cases = new HashMap<String, Zone>();
        cases.put(TestConfig.testBucket_z0, Zone.zone0());
        cases.put(TestConfig.testBucket_na0, Zone.zoneNa0());
        cases.put(TestConfig.testBucket_z0, Zone.autoZone());
        cases.put(TestConfig.testBucket_na0, Zone.autoZone());

        for (Map.Entry<String, Zone> entry : cases.entrySet()) {
            String bucket = entry.getKey();
            Zone zone = entry.getValue();

            String notifyURL = "";
            boolean force = true;

            String m3u8SaveEntry = String.format("%s:%s", bucket, this.testMp4FileKey + "_320x240.m3u8");
            String fopM3u8 = String.format("avthumb/m3u8/segtime/10/vcodec/libx264/s/320x240|saveas/%s",
                    UrlSafeBase64.encodeToString(String.format(m3u8SaveEntry)));

            String mp4SaveEntry = String.format("%s:%s", bucket, this.testMp4FileKey + "_320x240.mp4");
            String fopMp4 = String.format("avthumb/mp4/vcodec/libx264/s/320x240|saveas/%s",
                    UrlSafeBase64.encodeToString(mp4SaveEntry));

            //join fop together
            String fops = StringUtils.join(new String[]{fopM3u8, fopMp4}, ";");

            try {
                Configuration cfg = new Configuration(zone);
                OperationManager operationManager = new OperationManager(TestConfig.testAuth, cfg);
                String id = operationManager.pfop(bucket, this.testMp4FileKey, fops, testPipeline, notifyURL, force);
                assertNotNull(id);
                assertNotEquals("", id);
                String purl = "http://api.qiniu.com/status/get/prefop?id=" + id;
                System.out.println(purl);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testPrefop() {
        String persistentId = "na0.5899aaf692129336c2034e2d";
        try {
            Configuration cfg = new Configuration();
            //cfg.setUseHttpsDomains(true);
            OperationStatus status = new OperationManager(TestConfig.testAuth, cfg).prefop(persistentId);
            assertEquals(0, status.code);
        } catch (QiniuException ex) {
            Assert.assertEquals(612, ex.code());
        }
    }
}
