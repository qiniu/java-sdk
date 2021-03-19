package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.ConcurrentResumeUploader;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.ResumeUploader;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Etag;
import com.qiniu.util.StringMap;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResumeUploadTest {

//    private static boolean[][] TestConfigList = {
//            // isHttps, isResumeV2, isStream, isConcurrent
//            {false, true, true, true},
//    };

    private static boolean[][] testConfigList = {
            // isHttps, isResumeV2, isStream, isConcurrent
            {false, true, false, false},
            {false, false, false, true},
            {false, false, true, false},
            {false, false, true, true},
            {false, true, false, false},
            {false, true, false, true},
            {false, true, true, false},
            {false, true, true, true},
            {true, false, false, false},
            {true, false, false, true},
            {true, false, true, false},
            {true, false, true, true},
            {true, true, false, false},
            {true, true, false, true},
            {true, true, true, false},
            {true, true, true, true}
    };

    /**
     * 检测自定义变量foo是否生效
     *
     * @throws IOException
     */
    @Test
    public void testXVar() throws IOException {

        Map<String, Zone> bucketKeyMap = new HashMap<String, Zone>();
        bucketKeyMap.put(TestConfig.testBucket_z0, Zone.zone0());
        bucketKeyMap.put(TestConfig.testBucket_na0, Zone.zoneNa0());
        for (Map.Entry<String, Zone> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            Zone zone = entry.getValue();
            final String expectKey = "世/界";
            File f = null;
            try {
                f = TempFile.createFile(1024 * 4 + 2341);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert f != null;
            StringMap params = new StringMap().put("x:foo", "foo_val");
            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\",\"foo\":\"$(x:foo)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("returnBody", returnBody));

            try {
                UploadManager uploadManager = new UploadManager(new Configuration(zone));
                Response res = uploadManager.put(f, expectKey, token, params, null, true);
                StringMap m = res.jsonToMap();
                assertEquals("foo_val", m.get("foo"));
            } catch (QiniuException e) {
                assertEquals("", e.response == null ? "e.response is null" : e.response.bodyString());
                fail();
            } finally {
                TempFile.remove(f);
            }
        }
    }

    /**
     * 分片上传
     * 检测key、hash、fszie、fname是否符合预期
     *
     * @param size         文件大小
     * @param isHttps      是否采用 https 方式, 反之为 http
     * @param isResumeV2   是否使用分片上传 api v2, 反之为 v1
     * @param isStream     是否上传 stream, 反之为 file
     * @param isConcurrent 是否采用并发方式上传
     * @throws IOException
     */
    private void template(int size, boolean isHttps, boolean isResumeV2, boolean isStream, boolean isConcurrent) throws IOException {
        Map<String, Zone> bucketKeyMap = new HashMap<String, Zone>();
        bucketKeyMap.put(TestConfig.testBucket_z0, Zone.zone0());
        bucketKeyMap.put(TestConfig.testBucket_na0, Zone.zoneNa0());
        for (Map.Entry<String, Zone> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            Zone zone = entry.getValue();
            Configuration config = new Configuration(zone);
            if (isResumeV2) {
                config.resumeVersion = Configuration.ResumeVersion.V2;
            }

            config.useHttpsDomains = isHttps;
            String key = "";
            key += isHttps ? "_https" : "_http";
            key += isResumeV2 ? "_resumeV2" : "_resumeV1";
            key += isStream ? "_stream" : "_file";
            key += isConcurrent ? "_concurrent" : "_serial";
            final String expectKey = "\r\n?&r=" + size + "k" + key;
            final File f = TempFile.createFile(size);
            final String etag = Etag.file(f);
            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("returnBody", returnBody));

            System.out.printf("\r\nkey:%s zone:%s\n", expectKey, zone.getRegion());

            try {
                ResumeUploader up = null;
                if (!isConcurrent) {
                    if (isStream) {
                        up = new ResumeUploader(new Client(), token, expectKey, new FileInputStream(f), null, null, config);
                    } else {
                        up = new ResumeUploader(new Client(), token, expectKey, f, null, null, null, config);
                    }
                } else {
                    config.resumeMaxConcurrentTaskCount = 3;
                    if (isStream) {
                        up = new ConcurrentResumeUploader(new Client(), token, expectKey, new FileInputStream(f), null, null, config);
                    } else {
                        up = new ConcurrentResumeUploader(new Client(), token, expectKey, f, null, null, null, config);
                    }
                }

                Response r = up.upload();
                MyRet ret = r.jsonToObject(MyRet.class);
                assertEquals(expectKey, ret.key);
                if (!isStream) {
                    assertEquals(f.getName(), ret.fname);
                }
                assertEquals(String.valueOf(f.length()), ret.fsize);
                assertEquals(etag, ret.hash);
            } catch (QiniuException e) {
                assertEquals("", e.response == null ? e + "e.response is null" : e.response.bodyString());
                fail();
            }
            TempFile.remove(f);
        }
    }

    @Test
    public void test1K() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test600k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(600, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test4M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 4, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test8M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 8, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test8M1k() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 8 + 1, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test10M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 10, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test20M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 20, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test20M1K() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (boolean[] config : testConfigList) {
            template(1024 * 20 + 1, config[0], config[1], config[2], config[3]);
        }
    }


    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
    }
}
