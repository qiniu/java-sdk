package test.com.qiniu.streaming;

import com.qiniu.streaming.UrlFactory;
import test.com.qiniu.TestConfig;

import static org.junit.Assert.assertTrue;


/**
 * Created by bailong on 16/9/22.
 */
public class UrlTest {
    //@Test
    public void testUrl() {
        String hubName = "test";
        String pubDomain = "publish-rtmp.test.com";
        String rtmpDomain = "live-rtmp.test.com";
        String hlsDomain = "live-hls.test.com";
        String hdlDomain = "live-hdl.test.com";
        String snapDomain = "live-snapshot.test.com";

        UrlFactory uf = new UrlFactory(hubName, TestConfig.dummyAuth,
                pubDomain, rtmpDomain, hlsDomain, hdlDomain, snapDomain);
        String expect = "rtmp://publish-rtmp.test.com/" + hubName + "/key?e=";
        String url = uf.rtmpPublishUrl("key", 3600);
        System.out.println(url);
        assertTrue(url.startsWith(expect));

        expect = "rtmp://live-rtmp.test.com/" + hubName + "/key";
        url = uf.rtmpPlayUrl("key");
        assertTrue(url.startsWith(expect));

        expect = "http://live-hls.test.com/" + hubName + "/key.m3u8";
        url = uf.hlsPlayUrl("key");
        assertTrue(url.startsWith(expect));

        expect = "http://live-hdl.test.com/" + hubName + "/key.flv";
        url = uf.hdlPlayUrl("key");
        assertTrue(url.startsWith(expect));

        expect = "http://live-snapshot.test.com/" + hubName + "/key.jpg";
        url = uf.snapshotUrl("key");
        assertTrue(url.startsWith(expect));
    }
}
