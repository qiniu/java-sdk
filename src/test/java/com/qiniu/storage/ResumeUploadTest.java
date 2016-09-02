package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResumeUploadTest {

    @Test
    public void testXVar() throws IOException {
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
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("returnBody", returnBody));

        try {
            UploadManager uploadManager = new UploadManager();
            Response res = uploadManager.put(f, expectKey, token, params, null, true);
            StringMap m = res.jsonToMap();
            assertEquals("foo_val", m.get("foo"));
        } catch (QiniuException e) {
            assertEquals("", e.response.bodyString());
            fail();
        } finally {
            TempFile.remove(f);
        }
    }

    private void template(int size) throws IOException {
        final String expectKey = "\r\n?&r=" + size + "k";
        final File f = TempFile.createFile(size);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("returnBody", returnBody));

        try {
            ResumeUploader up = new ResumeUploader(new Client(), token, expectKey, f, null, null, null, null);
            Response r = up.upload();
            MyRet ret = r.jsonToObject(MyRet.class);
            assertEquals(expectKey, ret.key);
            assertEquals(f.getName(), ret.fname);
        } catch (QiniuException e) {
            assertEquals("", e.response.bodyString());
            fail();
        }
        TempFile.remove(f);
    }

    @Test
    public void test1K() throws Throwable {
        template(1);
    }

    @Test
    public void test600k() throws Throwable {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = true;
            template(600);
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void test600k2() throws IOException {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = false;
            template(600);
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void test4M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 4);
    }

    @Test
    public void test8M1k() throws Throwable {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = false;
            if (TestConfig.isTravis()) {
                return;
            }
            template(1024 * 8 + 1);
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void test8M1k2() throws Throwable {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = true;
            if (TestConfig.isTravis()) {
                return;
            }
            template(1024 * 8 + 1);
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
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
