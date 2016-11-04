package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Etag;
import com.qiniu.util.StringMap;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by long on 2016/11/4.
 */
public class StreamUploadTest {

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
            UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone0()));
            Response res = uploadManager.put(new FileInputStream(f), expectKey, token, params, null);
            StringMap m = res.jsonToMap();
            assertEquals("foo_val", m.get("foo"));
        } catch (QiniuException e) {
            assertEquals("", e.response.bodyString());
            fail();
        } finally {
            TempFile.remove(f);
        }
    }

    private void template(int size, boolean https) throws IOException {
        Configuration c = new Configuration(Zone.zone0());
        c.uploadByHttps = https;
        UploadManager uploadManager = new UploadManager(c);
        final String expectKey = "\r\n?&r=" + size + "k";
        final File f = TempFile.createFile(size);
        final String mime = "app/test";
        final String etag = Etag.file(f);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey, 3600,
                new StringMap().put("returnBody", returnBody));

        try {
            StreamUploader up = new StreamUploader(new Client(), token, expectKey,
                    new FileInputStream(f), null, mime, new Configuration(Zone.zone0()));
            Response r = up.upload();
            StreamUploadTest.MyRet ret = r.jsonToObject(StreamUploadTest.MyRet.class);
            assertEquals(expectKey, ret.key);
            assertEquals(String.valueOf(f.length()), ret.fsize);
            assertEquals(mime, ret.mimeType);
            assertEquals(etag, ret.hash);
        } catch (QiniuException e) {
            assertEquals("", e.response.bodyString());
            fail();
        }
        TempFile.remove(f);
    }

    @Test
    public void test1K() throws Throwable {
        template(1, false);
    }

    @Test
    public void test600k() throws Throwable {
        template(600, true);
    }

    @Test
    public void test600k2() throws IOException {
        template(600, false);
    }

    @Test
    public void test4M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 4, false);
    }

    @Test
    public void test8M1k() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 8 + 1, false);
    }

    @Test
    public void test8M1k2() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 8 + 1, true);
    }

    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
    }
}
