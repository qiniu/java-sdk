package test.com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.Etag;
import com.qiniu.util.Md5;
import com.qiniu.util.StringMap;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import static org.junit.Assert.*;

public class SwitchRegionTest {

    private static final int httpType = 0;
    private static final int httpsType = 1;

    private static final int resumableV1Type = 0;
    private static final int resumableV2Type = 1;

    private static final int formType = 0;
    private static final int serialType = 1;
    private static final int concurrentType = 2;

    private static int[][] testConfigList = {
            {httpType, -1, formType},
            {httpType, resumableV1Type, serialType},
            {httpType, resumableV1Type, concurrentType},
            {httpType, resumableV2Type, serialType},
            {httpType, resumableV2Type, concurrentType},

            {httpsType, -1, formType},
            {httpsType, resumableV1Type, serialType},
            {httpsType, resumableV1Type, concurrentType},
            {httpsType, resumableV2Type, serialType},
            {httpsType, resumableV2Type, concurrentType},
    };

    /**
     * 分片上传
     * 检测key、hash、fszie、fname是否符合预期
     *
     * @param size        文件大小
     * @param httpType    采用 http / https 方式
     * @param uploadType  使用 form / 分片上传 api v1/v2
     * @param uploadStyle 采用串行或者并发方式上传
     * @throws IOException
     */
    private void template(int size, int httpType, int uploadType, int uploadStyle) throws IOException {
        boolean isHttps = httpType == httpsType;
        boolean isConcurrent = uploadStyle == concurrentType;

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            // 雾存储不支持 v1
            if (file.isFog() && uploadType == resumableV1Type) {
                continue;
            }
            String bucket = file.getBucketName();

            Region mockRegion = new Region.Builder()
                    .region("custom")
                    .srcUpHost("mock.src.host.com")
                    .accUpHost("mock.acc.host.com")
                    .build();
            Region region = file.getRegion();
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.addRegion(mockRegion);
            regionGroup.addRegion(region);

            Configuration config = new Configuration(regionGroup);
            if (uploadType == resumableV2Type) {
                config.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
                config.resumableUploadAPIV2BlockSize = 2 * 1024 * 1024;
            }

            config.useHttpsDomains = isHttps;
            String key = "switch_region_";
            key += isHttps ? "_https" : "_http";

            if (uploadType == resumableV1Type) {
                key += "_resumeV1";
            } else if (uploadType == resumableV2Type) {
                key += "_resumeV2";
            }

            if (uploadStyle == formType) {
                key += "_form";
            } else if (uploadStyle == serialType) {
                key += "_serial";
            } else if (uploadStyle == concurrentType) {
                key += "_concurrent";
            }

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

            System.out.printf("\r\nkey:%s zone:%s\n", expectKey, regionGroup);


            StringMap param = new StringMap();
            param.put("x:" + fooKey, fooValue);
            param.put("x-qn-meta-" + metaDataKey, metaDataValue);
            try {
                BaseUploader up = null;
                if (uploadStyle == formType) {
                    up = new FormUploader(new Client(), token, expectKey, f, param, Client.DefaultMime, false, config);
                } else if (uploadStyle == serialType) {
                    up = new ResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
                } else {
                    config.resumableUploadMaxConcurrentTaskCount = 3;
                    up = new ConcurrentResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
                }

                Response r = up.upload();
                assertTrue(r + "", r.isOK());

                StringMap ret = r.jsonToMap();
                assertEquals(expectKey, ret.get("key"));
                assertEquals(f.getName(), ret.get("fname"));
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
    public void test600k() throws Throwable {
        for (int[] config : testConfigList) {
            template(600, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test3M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (int[] config : testConfigList) {
            template(1024 * 3, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test5M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        for (int[] config : testConfigList) {
            template(1024 * 4, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test8M() throws Throwable {
        for (int[] config : testConfigList) {
            template(1024 * 8, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test8M1k() throws Throwable {
        for (int[] config : testConfigList) {
            template(1024 * 8 + 1, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test10M() throws Throwable {
        for (int[] config : testConfigList) {
            template(1024 * 10, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test20M() throws Throwable {
        for (int[] config : testConfigList) {
            template(1024 * 20, config[0], config[1], config[2]);
        }
    }

    @Test
    public void test20M1K() throws Throwable {
        for (int[] config : testConfigList) {
            template(1024 * 20 + 1, config[0], config[1], config[2]);
        }
    }

    // 内部环境测试
//    @Test
    public void testInnerEnvSwitchRegion() {
        try {
            long s = new Date().getTime();
            uploadByInnerEnvSwitchRegion(1024 * 500 + 1, httpType, resumableV2Type, concurrentType);
            long e = new Date().getTime();
            System.out.println("耗时：" + (e - s));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("testInnerEnvSwitchRegion:" + e);
        }
    }

    public void uploadByInnerEnvSwitchRegion(int size, int httpType, int uploadType, int uploadStyle) throws Exception {

        boolean isHttps = httpType == httpsType;
        boolean isConcurrent = uploadStyle == concurrentType;
        String bucket = "aaaabbbbb";

        Region region0 = new Region.Builder()
                .srcUpHost("10.200.20.23:5010")
                .accUpHost("10.200.20.23:5010")
                .build();

        Region region1 = new Region.Builder()
                .srcUpHost("10.200.20.24:5010")
                .accUpHost("10.200.20.24:5010")
                .build();

        RegionGroup regionGroup = new RegionGroup();
        regionGroup.addRegion(region0);
        regionGroup.addRegion(region1);

        Configuration config = new Configuration(regionGroup);
        if (uploadType == resumableV2Type) {
            config.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
            config.resumableUploadAPIV2BlockSize = 4 * 1024 * 1024;
        }

        config.useHttpsDomains = isHttps;
        String key = "switch_region_";
        key += isHttps ? "_https" : "_http";

        if (uploadType == resumableV1Type) {
            key += "_resumeV1";
        } else if (uploadType == resumableV2Type) {
            key += "_resumeV2";
        }

        if (uploadStyle == formType) {
            key += "_form";
        } else if (uploadStyle == serialType) {
            key += "_serial";
        } else if (uploadStyle == concurrentType) {
            key += "_concurrent";
        }

        key += "_" + new Date().getTime();
        final String expectKey = "\r\n?&r=" + size + "k" + key;
        final File f = TempFile.createFile(size);

        final String fooKey = "foo";
        final String fooValue = "fooValue";
        final String metaDataKey = "metaDataKey";
        final String metaDataValue = "metaDataValue";
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\",\"foo\":\"$(x:foo)\"}";

        Auth auth = Auth.create(TestConfig.innerAccessKey, TestConfig.innerSecretKey);

        String token = auth.uploadToken(bucket, expectKey, 3600,
                new StringMap().put("returnBody", returnBody));

        System.out.printf("\r\nkey:%s zone:%s\n", expectKey, regionGroup);


        StringMap param = new StringMap();
        param.put("x:" + fooKey, fooValue);
        param.put("x-qn-meta-" + metaDataKey, metaDataValue);
        try {
            BaseUploader up = null;
            if (uploadStyle == formType) {
                up = new FormUploader(new Client(), token, expectKey, f, param, Client.DefaultMime, false, config);
            } else if (uploadStyle == serialType) {
                up = new ResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
            } else {
                config.resumableUploadMaxConcurrentTaskCount = 3;
                up = new ConcurrentResumeUploader(new Client(), token, expectKey, f, param, null, null, config);
            }

            Response r = up.upload();
            assertTrue(r + "", r.isOK());

            StringMap ret = r.jsonToMap();
            assertEquals(expectKey, ret.get("key"));
            assertEquals(f.getName(), ret.get("fname"));
            assertEquals(String.valueOf(f.length()), ret.get("fsize").toString());
            assertEquals(fooValue, ret.get(fooKey).toString());

            final String etag = Etag.file(f);
            System.out.println("      etag:" + etag);
            System.out.println("serverEtag:" + ret.get("hash"));
            assertNotNull(ret.get("hash"));
            assertEquals(etag, ret.get("hash"));
        } catch (QiniuException e) {
            assertEquals("", e.response == null ? e + "e.response is null" : e.response.bodyString());
            fail();
        }
        TempFile.remove(f);
    }


    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
    }
}
