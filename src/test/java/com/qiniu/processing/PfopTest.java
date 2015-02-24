package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class PfopTest {
    @Test
    public void testAvthumb() {
        PersistentOperationManager pfop = new PersistentOperationManager(TestConfig.testAuth,
                "testres", "sdktest", null, true);
        Operation save = OperationFactory.saveAs("javasdk", "pfoptest");
        Operation avthumb = OperationFactory.normal("avthumb", "m3u8",
                new StringMap().put("segtime", 10).put("vcodec", "libx264").put("s", "320x240"));
        try {
            String id = pfop.post("sintel_trailer.mp4", Pipe.createPersistent().append(avthumb).append(save));
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
        StringMap map = new StringMap().put("http://testres.qiniudn.com/gogopher.jpg", "g.jpg")
                .put("http://testres.qiniudn.com/gogopher.jpg", "");
        Operation mkzip = OperationFactory.mkzip(map);
        Operation save = OperationFactory.saveAs("javasdk", "mkziptest2.zip");
        try {
            String id = pfop.post("sintel_trailer.mp4", Pipe.createPersistent().append(mkzip).append(save));
            String text = pfop.status(id);
            assertNotNull(text);
            assertNotEquals("", text);
        } catch (QiniuException e) {
            fail();
        }
    }
}
