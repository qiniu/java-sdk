package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.FixBlockUploader;
import com.qiniu.util.Etag;
import com.qiniu.util.StringMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class FixBlockUploaderTest {
    int blockSize = 1024 * 1024 * 8;
    Configuration config;
    Client client;
    FixBlockUploader up;
    String bucket;
    BucketManager bm;

    @Before
    public void init() {
        init2(false);
    }

    private void init2(boolean useHttpsDomains) {
        config = new Configuration();
        config.useHttpsDomains = useHttpsDomains;
        client = new Client(config);
        up = new FixBlockUploader(blockSize, config, client, null);
        bucket = TestConfig.testBucket_z0;
        bm = new BucketManager(TestConfig.testAuth, config);
    }


    @Test
    public void testEmpty() throws IOException {
        try {
            template(0, false, false);
        } catch (QiniuException e) {
            assertTrue("empty file", e.response.error.indexOf("parts param can't be empty") > -1);
        }
        try {
            template(true, 0, false, false);
        } catch (QiniuException e) {
            assertTrue("empty stream", e.response.error.indexOf("parts param can't be empty") > -1);
        }
    }


    @Test
    public void test1K() throws IOException {
        template(1, false, true);
    }

    @Test
    public void test3MK() throws IOException {
        template(1024 * 3, false, true);
        try {
            template(true, 1024 * 3, false, false, true);
            Assert.fail("file exists, can not be success.");
        }  catch (QiniuException e) {
            assertTrue("file exists", e.response.error.indexOf("file exists") > -1);
        }
        // both the key and content are the same
        template(true, 1024 * 3, false, true, true);

        // update
        template(true, 1024 * 3, false, false);
    }


    @Test
    public void test6MK() throws IOException {
        template(1024 * 6, false, false);
    }


    @Test
    public void test6Mm1() throws IOException {
        template(1024 * 6 - 1, true, false);
    }

    @Test
    public void test6M1K() throws IOException {
        template(true, 1024 * 6 + 1, false, false);
    }


    @Test
    public void test7M() throws IOException {
        template(1024 * 7, true, false);
    }



    @Test
    public void test12M1K() throws IOException {
        template(1024 * 12 + 1024, false, false);
        template(true, 1024 * 12 + 1027, false, false);
    }

    @Test
    public void test24M() throws IOException {
        template(true, 1024 * 24, false, false);
        template(true, 1024 * 24 + 1027, false, false);
    }


    @Test
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
        final String etag = Etag.file(f);
        System.out.println(etag);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";

        StringMap p = new StringMap().put("returnBody", returnBody);
        if (insertOnly) {
            p.put("insertOnly", 1);
        }
        String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, p);
        final StringMap metaParams = new StringMap().put("X-Qn-Meta-liubin", "sb").
                put("X-Qn-Meta-!Content-Type", "text/liubin").
                put("X-Qn-Meta-Cache-Control", "public, max-age=1984");
        try {
            System.out.println("Start uploading " + new Date());
            Response r = null;
            if (isStream) {
                System.out.println("upload input stream");
                r = up.upload(new FileInputStream(f), f.length(), token, expectKey, metaParams, null, 0);
            } else {
                System.out.println("upload file");
                r = up.upload(f, token, expectKey, metaParams, null, 0);
            }
            System.out.println(r.getInfo());
            ResumeUploadTest.MyRet ret = r.jsonToObject(ResumeUploadTest.MyRet.class);
            assertEquals(expectKey, ret.key);
//            assertEquals(f.getName(), ret.fname);
            assertEquals(String.valueOf(f.length()), ret.fsize);
            assertEquals(etag, ret.hash);

            Response res = bm.statResponse(bucket, ret.key);
            System.out.println(res.getInfo());
            String resStr = res.bodyString();
            assertTrue("// 要有额外设置的元信息 //" + metaParams.formString(),
//                    resStr.indexOf("text/liubin") > -1 &&
                            resStr.indexOf("sb") > -1
                                    && resStr.indexOf("1984") > -1);
        } catch (QiniuException e) {
            if (e.response != null) {
                System.out.println(e.response.getInfo());
            }
            throw e;
        } finally {
            TempFile.remove(f);
        }

    }
}
