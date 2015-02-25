package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import org.junit.Test;

import static org.junit.Assert.*;

public class PfopTest {
    @Test
    public void testAvthumb() {
        PersistentOperationManager pfop = new PersistentOperationManager(TestConfig.testAuth,
                "testres", "sdktest", null, true);
        Operation save = new SaveAsOp("javasdk", "pfoptest");
        Operation avthumb = new GeneralOp("avthumb", "m3u8").put("segtime", 10)
                .put("vcodec", "libx264").put("s", "320x240");
        try {
            String id = pfop.post("sintel_trailer.mp4", Pipe.create().append(avthumb).append(save));
            String text = pfop.status(id);
            assertNotNull(text);
            assertNotEquals("", text);
        } catch (QiniuException e) {
            fail();
        }
    }

    @Test
    public void testMkzip() {
        PersistentOperationManager pfop = new PersistentOperationManager(TestConfig.testAuth,
                "testres", "sdktest", null, true);

        Operation mkzip = new ZipPackOp().append("http://testres.qiniudn.com/gogopher.jpg", "g.jpg")
                .append("http://testres.qiniudn.com/gogopher.jpg");

        Operation save = new SaveAsOp("javasdk", "mkziptest2.zip");
        try {
            String id = pfop.post("sintel_trailer.mp4", Pipe.create().append(mkzip).append(save));
            String text = pfop.status(id);
            assertNotNull(text);
            assertNotEquals("", text);
        } catch (QiniuException e) {
            fail();
        }
    }
}
