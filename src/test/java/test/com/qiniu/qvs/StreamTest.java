package test.com.qiniu.qvs;

import com.qiniu.http.Response;
import com.qiniu.qvs.StreamManager;
import com.qiniu.qvs.model.Stream;
import com.qiniu.util.Auth;

import org.junit.jupiter.api.BeforeEach;
import test.com.qiniu.TestConfig;

public class StreamTest {

    Auth auth = TestConfig.testAuth;
    private final String streamid = "" + System.currentTimeMillis();
    Stream stream = new Stream("31011500991320007536");
    Stream createstream = new Stream(streamid);
    private StreamManager streamManager;
    private Response res = null;
    private Response res2 = null;
    private final String namespaceId = "3nm4x1e0xw855";
    private final int start = 1639379380;
    private final int end = 1639379981;
    private final int offset = 0;
    private final int line = 1;
    private final int qtype = 0;
    private final String maker = "";
    private final String format = "";

    @BeforeEach
    public void setUp() throws Exception {
        this.streamManager = new StreamManager(auth);

    }

/*  @Test
    @Tag("IntegrationTest")
    public void testCreateStream() {
        // stream.setStreamID("teststream002");
        try {
            res = streamManager.createStream("2xenzw02ke9s4", createstream);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryStream() {
        try {
            res = streamManager.queryStream(namespaceId, stream.getStreamID());
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUpdateStream() {
        PatchOperation[] patchOperation = { new PatchOperation("replace", "desc", streamid) };
        try {
            res = streamManager.updateStream(namespaceId, stream.getStreamID(), patchOperation);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testListStream() {
        String prefix = "test";
        String sortBy = "desc:updatedAt";
        try {
            res = streamManager.listStream(namespaceId, offset, line, qtype, prefix, sortBy);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testStaticPublishPlayURL() {
        StaticLiveRoute staticLiveRoute = new StaticLiveRoute("qvs-publish.qnlinking.com", "publishRtmp", 3600);
        try {
            res = streamManager.staticPublishPlayURL("2xenzw02ke9s4", "teststream005", staticLiveRoute);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testDynamicPublishPlayURL() {
        DynamicLiveRoute dynamicLiveRoute = new DynamicLiveRoute("127.0.0.1", "127.0.0.1", 0);
        try {
            res = streamManager.dynamicPublishPlayURL(namespaceId, stream.getStreamID(), dynamicLiveRoute);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryStreamRecordHistories() {
        try {
            res = streamManager.queryStreamRecordHistories(namespaceId, stream.getStreamID(), start, end, line, maker,
                    format);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryStreamCover() {
        try {
            res = streamManager.queryStreamCover(namespaceId, stream.getStreamID());
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testStreamsSnapshots() {
        try {
            res = streamManager.streamsSnapshots(namespaceId, stream.getStreamID(), start, end, 0, line, maker);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryStreamPubHistories() {
        try {
            res = streamManager.queryStreamPubHistories(namespaceId, stream.getStreamID(), start, end, offset, line);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

//    @Test
//    @Tag("IntegrationTest")
//    public void testDisableStream() {
//        try {
//            res = streamManager.disableStream(namespaceId, stream.getStreamID());
//            res2 = streamManager.enableStream(namespaceId, stream.getStreamID());
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }

    @Test
    @Tag("IntegrationTest")
    public void testEnableStream() {
        try {
            res = streamManager.enableStream(namespaceId, stream.getStreamID());
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

//    @Test
//    @Tag("IntegrationTest")
//    public void testDeleteStream() {
//        try {
//            res = streamManager.deleteStream("2xenzw02ke9s4", "teststream006");
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }

    @Test
    @Tag("IntegrationTest")
    public void testOndemandSnap() {
        try {
            res = streamManager.ondemandSnap(namespaceId, stream.getStreamID());
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
*/
}
