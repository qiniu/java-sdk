package test.com.qiniu.processing;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Region;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TestConfig;

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
        Map<String, Region> cases = new HashMap<String, Region>();
        cases.put(TestConfig.testBucket_z0, Region.region0());
        cases.put(TestConfig.testBucket_na0, Region.regionNa0());
        cases.put(TestConfig.testBucket_z0, Region.autoRegion());
        cases.put(TestConfig.testBucket_na0, Region.autoRegion());

        for (Map.Entry<String, Region> entry : cases.entrySet()) {
            String bucket = entry.getKey();
            Region region = entry.getValue();

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
                Configuration cfg = new Configuration(region);
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
            //cfg.useHttpsDomains = true;
            OperationStatus status = new OperationManager(TestConfig.testAuth, cfg).prefop(persistentId);
            assertEquals(0, status.code);
        } catch (QiniuException ex) {
            Assert.assertEquals(612, ex.code());
        }
    }


    /*
    *
    * http://api.qiniu.com/status/get/prefop?id=z0.5b2cd03638b9f324a561e56d
    *
    * {
        code: 0,
        desc: "The fop was completed successfully",
        id: "z0.5b2cd03638b9f324a561e56d",
        inputBucket: "kk-community-video",
        inputKey: "shortvideo-1529655406116.mp4",
        items: [
        {
        cmd: "vsample/jpg/ss/1/t/10/interval/1/pattern/dmZyYW1lLSQoY291bnQp",
        code: 0,
        desc: "The fop was completed successfully",
        keys: [
        "vframe-000001",
        "vframe-000002",
        "vframe-000003",
        "vframe-000004",
        "vframe-000005",
        "vframe-000006",
        "vframe-000007",
        "vframe-000008",
        "vframe-000009"
        ],
        returnOld: 0
        }
        ],
        pipeline: "1380312146.kkpri02",
        reqid: "O2kAAFx4s3jjdDoV"
        }
    * */
    @Test
    public void testPrefopVsample() throws QiniuException {
        String persistentId = "z0.5b2cd03638b9f324a561e56d";

        Configuration cfg = new Configuration();
        //cfg.useHttpsDomains = true;
        OperationStatus status = new OperationManager(TestConfig.testAuth, cfg).prefop(persistentId);
        assertEquals(0, status.code);
        assertTrue("vsample prefop's keys length gt 1", status.items[0].keys.length > 1);
    }
}
