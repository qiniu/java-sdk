package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
}
