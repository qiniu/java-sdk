package test.com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Etag;
import com.qiniu.util.Md5;
import com.qiniu.util.StringMap;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import static org.junit.Assert.*;

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

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            // 雾存储不支持 v1
            if (file.isFog()) {
                continue;
            }
            String bucket = file.getBucketName();
            Region region = file.getRegion();
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
                UploadManager uploadManager = new UploadManager(new Configuration(region));
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
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            // 雾存储不支持 v1
            if (file.isFog() && !isResumeV2) {
                continue;
            }
            String bucket = file.getBucketName();
            Region region = file.getRegion();
            Configuration config = new Configuration(region);
            if (isResumeV2) {
                config.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
                config.resumableUploadAPIV2BlockSize = 2 * 1024 * 1024;
            }

            config.useHttpsDomains = isHttps;
            String key = "";
            key += isHttps ? "_https" : "_http";
            key += isResumeV2 ? "_resumeV2" : "_resumeV1";
            key += isStream ? "_stream" : "_file";
            key += isConcurrent ? "_concurrent" : "_serial";
            key += "_" + new Date().getTime();
            final String expectKey = "\r\n?&r=" + size + "k" + key;
            final File f = TempFile.createFile(size);
            final String fooKey = "foo";
            final String fooValue = "fooValue";
            final String metaDataKey = "metaDataKey";
            final String metaDataValue = "metaDataValue";
            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\",\"foo\":\"$(x:foo)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("returnBody", returnBody));

            System.out.printf("\r\nkey:%s zone:%s\n", expectKey, region);


            StringMap param = new StringMap();
            param.put("x:" + fooKey, fooValue);
            param.put("x-qn-meta-" + metaDataKey, metaDataValue);
            try {
                ResumeUploader up = null;
                if (!isConcurrent) {
                    if (isStream) {
                        up = new ResumeUploader(new Client(), token, expectKey, new FileInputStream(f), param, null, config);
                    } else {
                        up = new ResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
                    }
                } else {
                    config.resumableUploadMaxConcurrentTaskCount = 3;
                    if (isStream) {
                        up = new ConcurrentResumeUploader(new Client(), token, expectKey, new FileInputStream(f), param, null, config);
                    } else {
                        up = new ConcurrentResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
                    }
                }
                Response r = up.upload();
                assertTrue(r + "", r.isOK());

                StringMap ret = r.jsonToMap();
                assertEquals(expectKey, ret.get("key"));
                if (!isStream) {
                    assertEquals(f.getName(), ret.get("fname"));
                }
                assertEquals(String.valueOf(f.length()), ret.get("fsize").toString());
                assertEquals(fooValue, ret.get(fooKey).toString());

                boolean checkMd5 = false;
                if (config.resumableUploadAPIVersion == Configuration.ResumableUploadAPIVersion.V2
                        && config.resumableUploadAPIV2BlockSize != Constants.BLOCK_SIZE) {
                    checkMd5 = true;
                }
                if (checkMd5) {
                    if (file.isFog()) {
                        String md5 = Md5.md5(f);
                        String serverMd5 = getFileMD5(file.getTestDomain(), expectKey);
                        System.out.println("      md5:" + md5);
                        System.out.println("serverMd5:" + serverMd5);
                        assertNotNull(serverMd5);
                        assertEquals(md5, serverMd5);
                    }
                } else {
                    final String etag = Etag.file(f);
                    System.out.println("      etag:" + etag);
                    System.out.println("serverEtag:" + ret.get("hash"));
                    assertNotNull(ret.get("hash"));
                    assertEquals(etag, ret.get("hash"));
                }

                FileInfo fileInfo = getFileInfo(config, bucket, expectKey);
                assertEquals(fileInfo + "", metaDataValue, fileInfo.meta.get(metaDataKey).toString());
            } catch (QiniuException e) {
                assertEquals("", e.response == null ? e + "e.response is null" : e.response.bodyString());
                fail();
            }
            TempFile.remove(f);
        }
    }

    private String getFileMD5(String bucketDomain, String key) {
        String url = "http://" + bucketDomain + "/" + URLEncoder.encode(key) + "?qhash/md5";
        Client client = new Client();

        String md5 = null;
        try {
            Response response = client.get(url);
            StringMap data = response.jsonToMap();
            md5 = data.get("hash").toString();
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return md5;
    }

    private FileInfo getFileInfo(Configuration cfg, String bucket, String key) {
        BucketManager manager = new BucketManager(TestConfig.testAuth, cfg);
        try {
            return manager.stat(bucket, key);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return null;
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
        for (boolean[] config : testConfigList) {
            template(1024 * 8, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test8M1k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 8 + 1, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test10M() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 10, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test20M() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 20, config[0], config[1], config[2], config[3]);
        }
    }

    @Test
    public void test20M1K() throws Throwable {
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
