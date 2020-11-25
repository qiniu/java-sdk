package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.StreamManager;
import com.qiniu.qvs.model.DynamicLiveRoute;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.qvs.model.StaticLiveRoute;
import com.qiniu.qvs.model.Stream;
import com.qiniu.util.Auth;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class StreamTest {

    Auth auth = TestConfig.testAuth;
    Stream stream = new Stream("teststream005");
    private StreamManager streamManager;
    private Response res = null;
    private final String namespaceId = "2akrarsj8zp0w";
    private final int start = 1587975463;
    private final int end = 1587976463;
    private final int offset = 0;
    private final int line = 1;
    private final int qtype = 0;
    private final String maker = "";
    private final String format = "";

    @Before
    public void setUp() throws Exception {
        this.streamManager = new StreamManager(auth);

    }

    @Test
    public void testCreateStream() {
//        stream.setStreamID("teststream002");
        try {
            res = streamManager.createStream(namespaceId, stream);
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
    public void testUpdateStream() {
        PatchOperation[] patchOperation = {new PatchOperation("replace", "desc", "test")};
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
    public void testStaticPublishPlayURL() {
        StaticLiveRoute staticLiveRoute = new StaticLiveRoute("qvs-publish.qtest.com", "publishRtmp", 3600);
        try {
            res = streamManager.staticPublishPlayURL(namespaceId, stream.getStreamID(), staticLiveRoute);
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
    public void testQueryStreamRecordHistories() {
        try {
            res = streamManager.queryStreamRecordHistories(namespaceId, stream.getStreamID(), start, end, line, maker, format);
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

    @Test
    public void testDisableStream() {
        try {
            res = streamManager.disableStream(namespaceId, stream.getStreamID());
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

    @Test
    public void testDeleteStream() {
        try {
            res = streamManager.deleteStream(namespaceId, stream.getStreamID());
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
    public void testOndemandSnap() {
        try {
            res = streamManager.ondemandSnap("2xenzw5o81ods", "31011500991320000356");
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
}
