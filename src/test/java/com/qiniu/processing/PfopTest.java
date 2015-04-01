package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.util.GeneralOp;
import com.qiniu.processing.util.Operation;
import com.qiniu.processing.util.Pipe;
import com.qiniu.processing.util.SaveAsOp;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class PfopTest {
    private Auth auth = TestConfig.testAuth;
    private OperationManager operater = new OperationManager(auth);

    @Test
    public void testAvthumb() {
        String bucket = "testres";
        String key = "sintel_trailer.mp4";

        Operation save = new SaveAsOp("javasdk", "mp4_" + UUID.randomUUID());
        Operation avthumb = new GeneralOp("avthumb", "m3u8").put("segtime", 10)
                .put("vcodec", "libx264").put("s", "320x240");

        try {
            String id = operater.pfop(bucket, key, FopHelper.genFops(avthumb, save));
            assertNotNull(id);
            assertNotEquals("", id);
            String purl = "http://api.qiniu.com/status/get/prefop?id=" + id;
            System.out.println(purl);
        } catch (QiniuException e) {
            Response res = e.response;
            System.out.println(res);
            try {
                System.out.println(res.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
            fail();
        }
    }

    @Test
    public void testMuAvthumb() {
        String bucket = "testres";
        String key = "sintel_trailer.mp4";

        Operation save1 = new SaveAsOp("javasdk", "mp4_3_" + UUID.randomUUID());
        Operation avthumb1 = new GeneralOp("avthumb", "m3u8").put("segtime", 10)
                .put("vcodec", "libx264").put("s", "320x240");

        Operation save2 = new SaveAsOp("javasdk", "mp4_4_" + UUID.randomUUID());
        Operation avthumb2 = new GeneralOp("avthumb", "m3u8").put("segtime", 10)
                .put("vcodec", "libx264").put("s", "480x480");

        Operation save3 = new SaveAsOp("javasdk", "mp4_7_" + UUID.randomUUID());
        Operation avthumb3 = new GeneralOp("avthumb", "m3u8").put("segtime", 10)
                .put("vcodec", "libx264").put("s", "720x720");

        Operation avthumb4 = new GeneralOp("avthumb", "mp4").put("vcodec", "libx264");

        Pipe p1 = FopHelper.genPipe(avthumb1, save1);
        Pipe p2 = FopHelper.genPipe(avthumb2, save2);
        Pipe p3 = FopHelper.genPipe(avthumb3, save3);
        Pipe p4 = FopHelper.genPipe(avthumb4);


        try {
            String id = operater.pfop(bucket, key, FopHelper.genFops(p1, p2, p3, p4));
            assertNotNull(id);
            assertNotEquals("", id);
            String purl = "http://api.qiniu.com/status/get/prefop?id=" + id;
            System.out.println(purl);
        } catch (QiniuException e) {
            Response res = e.response;
            System.out.println(res);
            try {
                System.out.println(res.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
            fail();
        }
    }

    @Test
    public void testMkzip() {
        String url1 = "http://testres.qiniudn.com/gogopher.jpg";
        String alias1 = "gogopher.jpg";
        String url2 = "http://testres.qiniudn.com/gogopher.jpg";

        String cmd = "mkzip/2/url/" + UrlSafeBase64.encodeToString(url1) + "/alias/" + alias1 +
                "url/" + UrlSafeBase64.encodeToString(url2);
        GeneralOp mkzip = new GeneralOp("mkzip", "2").put("url", UrlSafeBase64.encodeToString(url1))
                .put("alias", UrlSafeBase64.encodeToString(alias1))
                .put("url", UrlSafeBase64.encodeToString(url2));

        String bucket = "testres";
        String key = "sintel_trailer.mp4";

        SaveAsOp save = new SaveAsOp("javasdk", "mkzip_" + UUID.randomUUID());
        try {
            String id = operater.pfop(bucket, key, FopHelper.genFops(mkzip, save));
            assertNotNull(id);
            assertNotEquals("", id);
            String purl = "http://api.qiniu.com/status/get/prefop?id=" + id;
            System.out.println(purl);
        } catch (QiniuException e) {
            Response res = e.response;
            System.out.println(res);
            try {
                System.out.println(res.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
            }
            fail();
        }
    }
}
