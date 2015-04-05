package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.DefaultPutRet;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResumeUploadTest {

    private void template(int size) throws IOException {
        final String expectKey = "\r\n?&r=" + size + "k";
        final File f = TempFile.createFile(size);
        String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);

        try {
            ResumeUploader up = new ResumeUploader(new Client(), token, expectKey, f, null, null, null, null);
            Response r = up.upload();
            DefaultPutRet ret = r.jsonToObject(DefaultPutRet.class);
            assertEquals(expectKey, ret.key);
        } catch (QiniuException e) {
            e.response.bodyString();
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
        template(600);
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
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 8 + 1);
    }
}
