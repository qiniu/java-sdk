package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UpCompletionHandler;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.StringMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FormUploadTest {

    UploadManager uploadManager = new UploadManager(new Configuration());

    @Test
    @Tag("IntegrationTest")
    void testUploadWithFop() {
        TestConfig.TestFile file = TestConfig.getTestFileArray()[0];
        final String expectKey = "test-fop";
        final String bucket = file.getBucketName();

        String persistentOpfs = String.format("%s:vframe_test_target.jpg", bucket);
        StringMap policy = new StringMap();
        policy.put("persistentOps", persistentOpfs);
        policy.put("persistentType", 1);

        Configuration config = new Configuration();
        config.useHttpsDomains = true;

        Response r = null;
        try {
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, policy);
            UploadManager uploadManager = new UploadManager(config);
            StringMap params = new StringMap().put("x:foo", "foo_val");
            r = uploadManager.put("hello".getBytes(), expectKey, token, params, null, false);
        } catch (QiniuException e) {
            fail(e.toString());
        }
        assertEquals(200, r.statusCode);

        StringMap map = null;
        try {
            map = r.jsonToMap();
        } catch (QiniuException e) {
            fail(e.toString());
        }

        assertNotNull(map, "1. testUploadWithFop error");

        String persistentId = (String) map.get("persistentId");
        assertNotNull(persistentId, "2. testUploadWithFop error");

        try {
            OperationManager operationManager = new OperationManager(TestConfig.testAuth, config);
            OperationStatus status = operationManager.prefop(bucket, persistentId);
            assertNotNull(status, "3. prefop type error");
            assertNotNull(status.creationDate, "4. prefop type error");
            assertTrue(status.code == 0 || status.code == 1 || status.code == 3, "5. prefop type error");
            assertEquals(1, (int) status.type, "6. prefop type error");
        } catch (QiniuException e) {
            fail(e.toString());
        }
    }

    /**
     * hello上传测试
     */
    @Test
    @Tag("IntegrationTest")
    public void testSimple() {
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);
            hello(uploadManager, file.getBucketName());
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testEmptyUploadHosts() {
        Region region = new Region.Builder()
                .srcUpHost(null)
                .accUpHost(null)
                .build();
        Configuration config = new Configuration(region);
        UploadManager uploadManager = new UploadManager(config);
        try {
            String key = "empty_upload_hosts";
            String token = TestConfig.testAuth.uploadToken(TestConfig.testBucket_na0, key);
            Response response = uploadManager.put("".getBytes(), key, token, null, "", true);
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e) {
            fail(e);
        }

        region = new Region.Builder()
                .srcUpHost("aaa")
                .build();
        config = new Configuration(region);
        uploadManager = new UploadManager(config);
        try {
            String key = "empty_upload_hosts";
            String token = TestConfig.testAuth.uploadToken(TestConfig.testBucket_na0, key);
            Response response = uploadManager.put("".getBytes(), key, token, null, "", true);
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e) {
            fail(e);
        }

        region = new Region.Builder()
                .accUpHost("aaa")
                .build();
        config = new Configuration(region);
        uploadManager = new UploadManager(config);
        try {
            String key = "empty_upload_hosts";
            String token = TestConfig.testAuth.uploadToken(TestConfig.testBucket_na0, key);
            Response response = uploadManager.put("".getBytes(), key, token, null, "", true);
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testSyncRetry() {
        TestConfig.TestFile[] files = TestConfig.getRetryTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);
            hello(uploadManager, file.getBucketName());
        }
    }

    /**
     * hello上传测试2
     */
    @Test
    @Tag("IntegrationTest")
    public void testHello2() {
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration();
            config.useHttpsDomains = true;
            UploadManager uploadManager = new UploadManager(config);
            hello(uploadManager, file.getBucketName());
        }
    }

    /**
     * hello上传，scope:<bucket:key> 检测是否请求200 检测返回值hash、key是否匹配
     */
    public void hello(UploadManager up, String bucket) {
        final String expectKey = "你好?&=\r\n";
        StringMap params = new StringMap().put("x:foo", "foo_val");

        String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
        Response r = null;
        try {
            r = up.put("hello".getBytes(), expectKey, token, params, null, false);
        } catch (QiniuException e) {
            fail(e.toString());
        }
        StringMap map = null;
        try {
            map = r.jsonToMap();
        } catch (QiniuException e) {
            fail(e.toString());
        }
        assertEquals(200, r.statusCode);
        assert map != null;
        assertEquals("Fqr0xh3cxeii2r7eDztILNmuqUNN", map.get("hash"));
        assertEquals(expectKey, map.get("key"));
    }

    /**
     * 无key上传，scope:<bucket> 检测是否返回200 检测返回值hash、key是否匹配
     */
    @Test
    @Tag("IntegrationTest")
    public void testNoKey() {
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            StringMap params = new StringMap().put("x:foo", "foo_val");
            String token = TestConfig.testAuth.uploadToken(bucket, null);
            Response r = null;
            try {
                r = uploadManager.put("hello".getBytes(), null, token, params, null, true);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            StringMap map = null;
            try {
                map = r.jsonToMap();
            } catch (QiniuException e) {
                fail();
            }
            assertEquals(200, r.statusCode);
            assert map != null;
            assertEquals("Fqr0xh3cxeii2r7eDztILNmuqUNN", map.get("hash"));
            assertEquals("Fqr0xh3cxeii2r7eDztILNmuqUNN", map.get("key"));
        }
    }

    /**
     * 错误token上传 检测是否返回401 检测reqid是否不为null
     */
    @Test
    @Tag("IntegrationTest")
    public void testInvalidToken() {
        final String expectKey = "你好";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            try {
                uploadManager.put("hello".getBytes(), expectKey, TestConfig.dummyUptoken);
                fail();
            } catch (QiniuException e) {
                if (e.code() != -1) {
                    assertEquals(401, e.code());
                    assertNotNull(e.response.reqId);
                }
            }
        }
    }

    /**
     * 空data上传 检测Exception是否为IllegalArgumentException一类
     */
    @Test
    @Tag("IntegrationTest")
    public void testNoData() {
        final String expectKey = "你好";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            try {
                uploadManager.put((byte[]) null, expectKey, TestConfig.dummyInvalidUptoken);
                fail();
            } catch (Exception e) {
                assertTrue(e instanceof IllegalArgumentException);
            }
        }
    }

    /**
     * NULL token上传 检测Exception是否为IllegalArgumentException一类
     *
     * @throws Throwable
     */
    @Test
    @Tag("IntegrationTest")
    public void testNoToken() throws Throwable {
        final String expectKey = "你好";
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            try {
                uploadManager.put(new byte[0], expectKey, null);
                fail();
            } catch (Exception e) {
                assertTrue(e instanceof IllegalArgumentException);
            }
        }
    }

    /**
     * 空token上传 检测Exception是否为IllegalArgumentException一类
     */
    @Test
    @Tag("IntegrationTest")
    public void testEmptyToken() {
        final String expectKey = "你好";
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            try {
                uploadManager.put(new byte[0], expectKey, "");
                fail();
            } catch (Exception e1) {
                assertTrue(e1 instanceof IllegalArgumentException);
            }
        }
    }

    /**
     * 文件上传 检测是否有Exception
     */
    @Test
    @Tag("IntegrationTest")
    public void testFile() {
        final String expectKey = "世/界";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert f != null;
            StringMap params = new StringMap().put("x:foo", "foo_val");
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
            try {
                uploadManager.put(f, expectKey, token, params, null, true);
            } catch (QiniuException e) {
                TempFile.remove(f);
                fail(e.response.toString());
            }
            TempFile.remove(f);
        }
    }

    /**
     * 异步上传 检测是否返回200 检测返回值hash、key是否匹配
     */
    @Test
    @Tag("IntegrationTest")
    public void testAsync() {
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);
            String bucket = file.getBucketName();
            asyncUpload(uploadManager, bucket);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testAsyncRetry() {
        TestConfig.TestFile[] files = TestConfig.getRetryTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);
            asyncUpload(uploadManager, file.getBucketName());
        }
    }

    private void asyncUpload(UploadManager up, String bucket) {
        final String expectKey = "你好?&=\r\n";
        StringMap params = new StringMap().put("x:foo", "foo_val");

        String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
        final CountDownLatch signal = new CountDownLatch(1);
        try {
            up.asyncPut("hello".getBytes(), expectKey, token, params, null, false,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, Response r) {
                            signal.countDown();
                            StringMap map = null;
                            try {
                                map = r.jsonToMap();
                            } catch (QiniuException e) {
                                e.printStackTrace();
                                fail();
                            }
                            assertEquals(200, r.statusCode);
                            assert map != null;
                            assertEquals("Fqr0xh3cxeii2r7eDztILNmuqUNN", map.get("hash"));
                            assertEquals(expectKey, map.get("key"));
                        }
                    });
        } catch (IOException e) {
            fail();
        }
        try {
            signal.await(120, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测自定义参数foo是否生效
     */
    @Test
    @Tag("IntegrationTest")
    public void testXVar() {
        final String expectKey = "世/界";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(1);
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
                Response res = uploadManager.put(f, expectKey, token, params, null, true);
                StringMap m = res.jsonToMap();
                assertEquals("foo_val", m.get("foo"));
            } catch (QiniuException e) {
                fail(e.response.toString());
            } finally {
                TempFile.remove(f);
            }
        }
    }

    /**
     * 检测fname是否生效
     */
    @Test
    @Tag("IntegrationTest")
    public void testFname() {
        final String expectKey = "世/界";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(1);
                File f1 = TempFile.createFile(1);
                f = new File(f1.getParentFile(), "_试一个中文看下效果_" + f1.getName());
                f1.renameTo(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert f != null;
            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("returnBody", returnBody));
            try {
                Response res = uploadManager.put(f, expectKey, token, null, null, true);
                MyRet ret = res.jsonToObject(MyRet.class);
                assertEquals(f.getName(), ret.fname);
            } catch (QiniuException e) {
                TempFile.remove(f);
                fail(e.response.toString());
            }
            TempFile.remove(f);
        }
    }

    /**
     * 检测fsizeMin是否生效
     */
    @Test
    @Tag("IntegrationTest")
    public void testSizeMin() {
        final String expectKey = "世/界";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert f != null;
            StringMap params = new StringMap().put("x:foo", "foo_val");
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("fsizeMin", 1024 * 1025));
            try {
                uploadManager.put(f, expectKey, token, params, null, true);
            } catch (QiniuException e) {
                Response res = e.response;
                try {
                    assertEquals("{\"error\":\"request entity size is smaller than FsizeMin\"}", res.bodyString());
                } catch (QiniuException e1) {
                    e1.printStackTrace();
                }
            } finally {
                TempFile.remove(f);
            }
        }
    }

    /**
     * 检测fsizeMin是否生效
     */
    @Test
    @Tag("IntegrationTest")
    public void testSizeMin2() {
        final String expectKey = "世/界";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert f != null;
            StringMap params = new StringMap().put("x:foo", "foo_val");
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600,
                    new StringMap().put("fsizeMin", 1023));
            try {
                uploadManager.put(f, expectKey, token, params, null, true);
            } catch (QiniuException e) {
                e.printStackTrace();
                fail();
            } finally {
                TempFile.remove(f);
            }
        }
    }

    /**
     * 检测putThreshold是否生效
     */
    @Test
    @Tag("IntegrationTest")
    public void testFormLargeSize() {
        final String expectKey = "yyyyyy";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            config.putThreshold = 25 * 1024 * 1024;
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            File f = null;
            try {
                f = TempFile.createFile(config.putThreshold / 1024 - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
            try {
                uploadManager.put(f, expectKey, token, null, null, false);
            } catch (QiniuException e) {
                e.printStackTrace();
                try {
                    String err = e.response != null ? e.response.bodyString() : e.error();
                    assertEquals("_", err);
                } catch (QiniuException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 检测putThreshold是否生效
     */
    @SuppressWarnings("resource")
    @Test
    @Tag("IntegrationTest")
    public void testFormLargeSize2() {
        final String expectKey = "xxxxxxx";

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration config = new Configuration(file.getRegion());
            config.putThreshold = 25 * 1024 * 1024;
            UploadManager uploadManager = new UploadManager(config);

            String bucket = file.getBucketName();
            byte[] bb = null;
            File f = null;
            try {
                f = TempFile.createFile(config.putThreshold / 1024 - 1);
                bb = new byte[(int) (f.length())];
                FileInputStream fis = new FileInputStream(f);
                fis.read(bb, 0, (int) (f.length()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
            try {
                uploadManager.put(bb, expectKey, token, null, null, false);
            } catch (QiniuException e) {
                e.printStackTrace();
                try {
                    assertEquals("_", e.response.bodyString());
                } catch (QiniuException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试inputStream 表单上传 检测reqid是否为Null 检测状态码是否为200
     */
    @Test
    @Tag("IntegrationTest")
    public void testFormUploadWithInputStream() {
        testFormUploadWithInputStream(1, -1);
        testFormUploadWithInputStream(1, 0);
        testFormUploadWithInputStream(1, 1000);
        testFormUploadWithInputStream(4 * 1024, 4 * 1024 * 1024);
        testFormUploadWithInputStream(5 * 1024, -1);
        testFormUploadWithInputStream(5 * 1024, 5 * 1024 * 1024);
    }

    /**
     * 测试inputStream 表单上传 检测reqid是否为Null 检测状态码是否为200
     */
    private void testFormUploadWithInputStream(long kiloSize, long size) {

        try {
            File file = TempFile.createFile(kiloSize);
            InputStream inputStream = new FileInputStream(file);
            System.out.println("length=" + file.length());
            System.out.println("size=" + size);

            TestConfig.TestFile[] files = TestConfig.getTestFileArray();
            for (TestConfig.TestFile testFile : files) {
                Configuration config = new Configuration(testFile.getRegion());
                config.putThreshold = 25 * 1024 * 1024;
                UploadManager uploadManager = new UploadManager(config);

                String bucket = testFile.getBucketName();
                String token = TestConfig.testAuth.uploadToken(bucket, bucket, 3600, null);
                System.out.println("token=" + token);

                Response response = uploadManager.put(inputStream, size, bucket, token, null, null, false);
                System.out.println("code=" + response.statusCode);
                System.out.println("reqid=" + response.reqId);
                System.out.println(response.bodyString());
                assertNotNull(response.reqId);
                assertEquals(200, response.statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
