package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Test;

import static org.junit.Assert.*;

public class FopTest {
    @Test
    public void testExifPub() {
        OperationManager opm = new OperationManager("testres.qiniudn.com");
        Operation op = OperationFactory.normal("exif");
        try {
            Response r = opm.get("gogopher.jpg", op);
            assertNotNull(r.bodyString());
        } catch (QiniuException e) {
            fail();
        }
    }

    @Test
    public void testExifPrivate() {
        OperationManager opm = new OperationManager("private-res.qiniudn.com", TestConfig.testAuth, 3600);
        Operation op = OperationFactory.normal("exif");
        try {
            opm.get("noexif.jpg", op);
            fail();
        } catch (QiniuException e) {
            assertEquals(400, e.code());
        }
    }
}
