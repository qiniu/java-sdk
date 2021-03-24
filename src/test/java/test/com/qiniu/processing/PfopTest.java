package test.com.qiniu.processing;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

import java.util.*;

import static org.junit.Assert.fail;

public class PfopTest {

    /**
     * 测试pfop
     * 检测jobid是否不为空
     */
    @Test
    public void testPfop() throws QiniuException {
        Map<String, Region> bucketKeyMap = new HashMap<String, Region>();
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile testFile : files) {
            bucketKeyMap.put(testFile.getBucketName(), testFile.getRegion());
        }
        List<String> ids = new ArrayList<>();

        Configuration cfg = new Configuration();
        OperationManager operationManager = new OperationManager(TestConfig.testAuth, cfg);

        for (Map.Entry<String, Region> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            Region region = entry.getValue();

            String notifyURL = null;
            boolean force = true;

            String m3u8SaveEntry = String.format("%s:%s", bucket, TestConfig.testMp4FileKey + "_320x240.m3u8");
            String fopM3u8 = String.format("avthumb/m3u8/segtime/10/vcodec/libx264/s/320x240|saveas/%s",
                    UrlSafeBase64.encodeToString(String.format(m3u8SaveEntry)));

            String mp4SaveEntry = String.format("%s:%s", bucket, TestConfig.testMp4FileKey + "_320x240.mp4");
            String fopMp4 = String.format("avthumb/mp4/vcodec/libx264/s/320x240|saveas/%s",
                    UrlSafeBase64.encodeToString(mp4SaveEntry));

            String fops = StringUtils.join(new String[]{fopM3u8, fopMp4}, ";");
            System.out.println(fops);

            try {
                String jobid = operationManager.pfop(bucket, TestConfig.testMp4FileKey, fops, null,
                        notifyURL, force);
                Assert.assertNotNull(jobid);
                Assert.assertNotEquals("", jobid);
                ids.add(jobid);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
        System.out.println("\n\n");
        for (String jobid : ids) {
            String purl = "https://api.qiniu.com/status/get/prefop?id=" + jobid;
            System.out.println(purl);
            OperationStatus status = operationManager.prefop(jobid);
            System.out.println(new Gson().toJson(status));
            Assert.assertEquals(jobid, status.id);
        }

        System.out.println("\n\n");
        try {
            Thread.sleep(1000 * 7);
        } catch (Exception e) {
            // ingore
        }

        for (String jobid : ids) {
            String purl = "https://api.qiniu.com/status/get/prefop?id=" + jobid;
            System.out.println(purl);
            OperationStatus status = operationManager.prefop(jobid);
            System.out.println(new Gson().toJson(status));
            Assert.assertEquals(jobid, status.id);
        }

        for (String jobid : ids) {
            testPfopIsSuccess(jobid);
        }
    }

    /**
     * 测试prefop
     * 检测status是否为0（成功）
     */
    private void testPfopIsSuccess(String jobid) {
        long maxWaitTime = 30 * 60 * 1000;
        Date startDate = new Date();
        OperationStatus status = null;
        do {
            try {
                Configuration cfg = new Configuration(Zone.autoZone());
                OperationManager operationManager = new OperationManager(TestConfig.testAuth, cfg);
                status = operationManager.prefop(jobid);
            } catch (QiniuException ex) {
                ex.printStackTrace();
                Assert.assertTrue(ResCode.find(ex.code(), ResCode.getPossibleResCode(612)));
                break;
            }

            Date currentDate = new Date();
            if (currentDate.getTime() - startDate.getTime() > maxWaitTime) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }

        } while (status == null || status.code != 0);

        Assert.assertNotNull(status);
        System.out.println(new Gson().toJson(status));
        Assert.assertEquals(0, status.code);
    }

}
