package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FormUploadTest {
    private UploadManager uploadManager = new UploadManager();

    @Test
    public void testHello() {
        final String expectKey = "你好?&=\r\n";
        StringMap params = new StringMap().put("x:foo", "foo_val");

        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        Response r = null;
        try {
            r = uploadManager.put("hello".getBytes(), expectKey, token, params, null, false);
        } catch (QiniuException e) {
            fail();
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
        assertEquals(expectKey, map.get("key"));
    }

    @Test
    public void testNoKey() {
        StringMap params = new StringMap().put("x:foo", "foo_val");

        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, null);
        Response r = null;
        try {
            r = uploadManager.put("hello".getBytes(), null, token, params, null, true);
        } catch (QiniuException e) {
            fail();
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

    @Test
    public void testInvalidToken() {
        final String expectKey = "你好";

        try {
            uploadManager.put("hello".getBytes(), expectKey, "invalid");
            fail();
        } catch (QiniuException e) {
            assertEquals(401, e.code());
            assertNotNull(e.response.reqId);
        }
    }

    @Test
    public void testNoData() {
        final String expectKey = "你好";
        try {
            uploadManager.put((byte[]) null, expectKey, "invalid");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testNoToken() throws Throwable {
        final String expectKey = "你好";
        try {
            uploadManager.put(new byte[0], expectKey, null);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testEmptyToken() {
        final String expectKey = "你好";
        try {
            uploadManager.put(new byte[0], expectKey, "");
            fail();
        } catch (Exception e1) {
            assertTrue(e1 instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFile() {
        final String expectKey = "世/界";
        File f = null;
        try {
            f = TempFile.createFile(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert f != null;
        StringMap params = new StringMap().put("x:foo", "foo_val");
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        try {
            uploadManager.put(f, expectKey, token, params, null, true);
        } catch (QiniuException e) {
            TempFile.remove(f);
            fail();
        }
        TempFile.remove(f);
    }

    @Test
    public void testAsync() {
        final String expectKey = "你好?&=\r\n";
        StringMap params = new StringMap().put("x:foo", "foo_val");

        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        final CountDownLatch signal = new CountDownLatch(1);
        Response r = null;
        try {
            uploadManager.asyncPut("hello".getBytes(), expectKey, token, params,
                    null, false, new UpCompletionHandler() {
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

    @Test
    public void testFname() {
        final String expectKey = "世/界";
        File f = null;
        try {
            f = TempFile.createFile(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert f != null;
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("returnBody", returnBody));
        try {
            Response res = uploadManager.put(f, expectKey, token, null, null, true);
            MyRet ret = res.jsonToObject(MyRet.class);
            assertEquals(f.getName(), ret.fname);
        } catch (QiniuException e) {
            TempFile.remove(f);
            fail();
        }
        TempFile.remove(f);
    }

    public void testSizeMin() {
        final String expectKey = "世/界";
        File f = null;
        try {
            f = TempFile.createFile(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert f != null;
        StringMap params = new StringMap().put("x:foo", "foo_val");
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("fsizeMin", 1024 * 1025));
        try {
            Response res = uploadManager.put(f, expectKey, token, params, null, true);

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

    @Test
    public void testSizeMin2() {
        final String expectKey = "世/界";
        File f = null;
        try {
            f = TempFile.createFile(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert f != null;
        StringMap params = new StringMap().put("x:foo", "foo_val");
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("fsizeMin", 1023));
        try {
            uploadManager.put(f, expectKey, token, params, null, true);
        } catch (QiniuException e) {
            fail();
        } finally {
            TempFile.remove(f);
        }
    }

    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
    }

    //    @Test
    public void testFormLargeSize() {
        Config.PUT_THRESHOLD = 25 * 1024 * 1024;

        final String expectKey = "yyyyyy";
        File f = null;
        try {
            f = TempFile.createFile(Config.PUT_THRESHOLD / 1024 - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        Response r = null;
        try {
            r = uploadManager.put(f, expectKey, token, null, null, false);
        } catch (QiniuException e) {
            try {
                assertEquals("_", e.response.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
        }

    }


    //    @Test
    public void testFormLargeSize2() {
        Config.PUT_THRESHOLD = 25 * 1024 * 1024;

        final String expectKey = "xxxxxxx";
        byte[] bb = null;
        File f = null;
        try {
            f = TempFile.createFile(Config.PUT_THRESHOLD / 1024 - 1);
            bb = new byte[(int) (f.length())];
            FileInputStream fis = new FileInputStream(f);
            fis.read(bb, 0, (int) (f.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        Response r = null;
        try {
            r = uploadManager.put(bb, expectKey, token, null, null, false);
        } catch (QiniuException e) {
            try {
                assertEquals("_", e.response.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
        }

    }

}
