package test.com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.FixBlockUploader;
import com.qiniu.storage.Region;
import com.qiniu.util.EtagV2;
import com.qiniu.util.StringMap;
import test.com.qiniu.TempFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class FixBlockUploaderTest {
    int blockSize = 1024 * 1021 * 7;
    Configuration config;
    Client client;
    FixBlockUploader up;
    String bucket;
    BucketManager bm;

    @BeforeEach
    public void init() {
        init2(false);
    }

    private void init2(boolean useHttpsDomains) {
        config = new Configuration(Region.region0());
        config.useHttpsDomains = useHttpsDomains;
        client = new Client(config);
        up = new FixBlockUploader(blockSize, config, client, null);
        bucket = TestConfig.testBucket_z0;
        bm = new BucketManager(TestConfig.testAuth, config);
    }

    @Test
    @Tag("IntegrationTest")
    public void testEmpty() throws IOException {
        try {
            template(0, false, false);
        } catch (QiniuException e) {
            assertTrue(e.response.error.indexOf("parts param can't be empty") > -1, "empty file");
        }
        try {
            template(true, 0, false, false);
        } catch (QiniuException e) {
            assertTrue(e.response.error.indexOf("parts param can't be empty") > -1, "empty stream");
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void test1K() throws IOException {
        template(1, false, true);
    }

    @Test
    @Tag("IntegrationTest")
    public void test3MK() throws IOException {
        template(1024 * 3, false, true);
        try {
            template(true, 1024 * 3, false, false, true);
            fail("file exists, can not be success.");
        } catch (QiniuException e) {
            assertTrue(e.response.error.indexOf("file exists") > -1, "file exists");
        }
        // both the key and content are the same
        template(true, 1024 * 3, false, true, true);

        // update
        template(true, 1024 * 3, false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test6MK() throws IOException {
        template(1024 * 6, false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test6Mm1() throws IOException {
        template(1024 * 6 - 1, true, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test6M1K() throws IOException {
        template(true, 1024 * 6 + 1, false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test7M() throws IOException {
        template(1024 * 7, true, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test12M1K() throws IOException {
        template(1024 * 12 + 1024, false, false);
        template(true, 1024 * 12 + 1027, false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test24M() throws IOException {
        template(true, 1024 * 24, false, false);
        template(true, 1024 * 24 + 1027, false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void test26M1K() throws IOException {
        template(1024 * 26 + 1024, false, false);
    }

    private void template(int size, boolean https, boolean fixFile) throws IOException {
        template(false, size, https, fixFile);
    }

    private void template(boolean isStream, int size, boolean https, boolean fixFile) throws IOException {
        template(isStream, size, https, fixFile, false);
    }

    private void template(boolean isStream, int size, boolean https, boolean fixFile, boolean insertOnly)
            throws IOException {
        if (https) {
            init2(https);
        }
        System.out.println("Start testing " + new Date());
        final String expectKey = "\r\n?&r=" + size + "k";
        File f = null;
        if (!fixFile) {
            f = TempFile.createFile(size);
        } else {
            f = TempFile.createFileOld(size);
        }
        System.out.println(f.getAbsolutePath());
        final String etag = EtagV2.file(f, blockSize);
        System.out.println(etag);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";

        StringMap p = new StringMap().put("returnBody", returnBody);
        if (insertOnly) {
            p.put("insertOnly", 1);
        }
        String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, p);
        FixBlockUploader.OptionsMeta param = new FixBlockUploader.OptionsMeta();
        param.addMetadata("X-Qn-Meta-liubin", "sb").addMetadata("X-Qn-Meta-!Content-Type", "text/liubin")
                .addMetadata("X-Qn-Meta-Cache-Control", "public, max-age=1984");
        try {
            System.out.println("Start uploading " + new Date());
            Response r = null;
            if (isStream) {
                System.out.println("upload input stream");
                r = up.upload(new FileInputStream(f), f.length(), null, token, expectKey, param, null, 0);
            } else {
                System.out.println("upload file");
                r = up.upload(f, token, expectKey, param, null, 0);
            }
            System.out.println(r.getInfo());
            ResumeUploadTest.MyRet ret = r.jsonToObject(ResumeUploadTest.MyRet.class);
            assertEquals(expectKey, ret.key);
            if (!isStream) {
                assertEquals(f.getName(), ret.fname);
            }
            assertEquals(String.valueOf(f.length()), ret.fsize);
            assertEquals(etag, ret.hash);

            Response res = bm.statResponse(bucket, ret.key);
            System.out.println(res.getInfo());
            String resStr = res.bodyString();
            assertTrue(resStr.indexOf("text/liubin") > -1 && resStr.indexOf("sb") > -1 && resStr.indexOf("1984") > -1,
                    "// 要有额外设置的元信息 //\n" + new Gson().toJson(param));
        } catch (QiniuException e) {
            if (e.response != null) {
                System.out.println(e.response.getInfo());
            }
            throw e;
        } finally {
            TempFile.remove(f);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testEmptyKey() throws IOException {
        File f = TempFile.createFileOld(1);
        String etag = EtagV2.file(f, blockSize);
        String token = TestConfig.testAuth.uploadToken(bucket, null);
        Response res = up.upload(f, token, "");
        System.out.println(res.getInfo());
        ResumeUploadTest.MyRet ret = res.jsonToObject(ResumeUploadTest.MyRet.class);
        assertEquals("", ret.key);
        assertEquals(etag, ret.hash);
    }

    @Test
    @Tag("IntegrationTest")
    public void testNullKey() throws IOException {
        File f = TempFile.createFile(2);
        String etag = EtagV2.file(f, blockSize);
        String token = TestConfig.testAuth.uploadToken(bucket, null);
        Response res = up.upload(f, token, null);
        System.out.println(res.getInfo());
        ResumeUploadTest.MyRet ret = res.jsonToObject(ResumeUploadTest.MyRet.class);
        assertEquals(ret.hash, ret.key);
        assertEquals(etag, ret.hash);
    }

    @Test
    @Tag("IntegrationTest")
    public void testKey2() throws IOException {
        File f = TempFile.createFile(2);
        String etag = EtagV2.file(f, blockSize);
        String token = TestConfig.testAuth.uploadToken(bucket, "err");
        try {
            Response res = up.upload(f, token, null);
            fail("key not match, should failed");
        } catch (QiniuException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testMeat() throws IOException {
        File f = TempFile.createFile(1);
        String etag = EtagV2.file(f, blockSize);
        String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(x:biubiu)_$(fname)\",\"mimeType\":\"$(mimeType)\",\"biu2biu\":\"$(x:biu2biu)\"}";

        StringMap p = new StringMap().put("returnBody", returnBody);
        String key = "俩个中文试试1.txt";
        String token = TestConfig.testAuth.uploadToken(bucket, key, 3600, p);
        FixBlockUploader.OptionsMeta param = new FixBlockUploader.OptionsMeta();
        String mimeType = "mimetype/hehe";
        param.setMimeType(mimeType);
        param.addCustomVar("x:biubiu", "duDu/werfhue3");
        param.addCustomVar("x:biu2biu", "duDu/werfhue3");
        param.addMetadata("X-Qn-Meta-!Content-Type", "text/liubin");
        param.addMetadata("X-Qn-Meta-Cosbsbooe4", "teinYjf");
        // param.addMetadata("X-Qn-Meta-!Cobsboe4", 23);
        Response res = up.upload(f, token, key, param, null, 0);
        System.out.println(res.getInfo());
        MyRet ret = res.jsonToObject(MyRet.class);
        Response res2 = bm.statResponse(bucket, ret.key);
        System.out.println(res2.getInfo());
        assertNotEquals(ret.hash, ret.key);
        assertEquals(mimeType, ret.mimeType);
        assertEquals(etag, ret.hash);
        assertEquals("duDu/werfhue3_" + f.getName(), ret.fname);
        assertEquals("duDu/werfhue3", ret.biu2biu);
        String resStr = res2.bodyString();
        assertTrue(resStr.indexOf("text/liubin") > -1 && resStr.indexOf("teinYjf") > -1,
                "// 要有额外设置的元信息  metadata //\n" + new Gson().toJson(param));
    }

    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
        public String biu2biu;
    }

}
