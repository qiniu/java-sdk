package com.qiniu.processing;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class PfopTest {
    private Auth auth = TestConfig.testAuth;
    private OperationManager operater = new OperationManager(auth, new Configuration(Zone.zone0()));

    @Test
    public void testAvthumb() {
        String bucket = "testres";
        String key = "sintel_trailer.mp4";

        String notifyURL = "";
        boolean force = true;
        String pipeline = "";
        StringMap params = new StringMap().putNotEmpty("notifyURL", notifyURL)
                .putWhen("force", 1, force).putNotEmpty("pipeline", pipeline);
        // CHECKSTYLE:OFF
        String fops = "avthumb/m3u8/segtime/10/vcodec/libx264/s/320x240|saveas/amF2YXNkazptcDRfODVmZWVjY2ItMGZmOS00NTg5LTk3MTMtZDM1ZmYzZGQ4ZjM2";
        // CHECKSTYLE:ON
        try {
            String id = operater.pfop(bucket, key, fops, params);
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
        // CHECKSTYLE:OFF
        String fops = "avthumb/m3u8/segtime/10/vcodec/libx264/s/320x240|saveas/amF2YXNkazptcDRfM19mMDNjZjQ4Mi1kMzgxLTQ5NWUtOThiNC04NjZkYTVkMDVlMTY=;avthumb/m3u8/segtime/10/vcodec/libx264/s/480x480|saveas/amF2YXNkazptcDRfNF8wYmQ2NjQxOS04MjZmLTRiZDItYjQ5MS00ZDRmM2Y1ZmQ1Mjk=;avthumb/m3u8/segtime/10/vcodec/libx264/s/720x720|saveas/amF2YXNkazptcDRfN19hMTM1ZmRlNS1hYTNlLTQ3MWItYjdhMi0yNmQ5MWEyZTc5MzM=;avthumb/mp4/vcodec/libx264";
        // CHECKSTYLE:ON
        StringMap params = new StringMap().putWhen("force", 1, true);
        try {
            String id = operater.pfop(bucket, key, fops, params);
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
        String bucket = "testres";
        String key = "sintel_trailer.mp4";
        // CHECKSTYLE:OFF
        String fops = "mkzip/2/url/aHR0cDovL3Rlc3RyZXMucWluaXVkbi5jb20vZ29nb3BoZXIuanBn/alias/Z29nb3BoZXIuanBn/url/aHR0cDovL3Rlc3RyZXMucWluaXVkbi5jb20vZ29nb3BoZXIuanBn";
        fops += "|saveas/" + UrlSafeBase64.encodeToString("javasdk" + ":" + key + "_" + UUID.randomUUID());
        // CHECKSTYLE:ON
        try {
            String id = operater.pfop(bucket, key, fops);
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
