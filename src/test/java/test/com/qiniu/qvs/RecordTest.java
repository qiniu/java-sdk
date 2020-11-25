package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.RecordManager;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class RecordTest {
    Auth auth = TestConfig.testAuth;
    private RecordManager recordManager;
    private Response res = null;
    private Response res2 = null;
    private final String namespaceId = "2xenzw5o81ods";
    private final String streamId = "31011500991320000356";
    private final String format = "m3u8";
    private final int start = 1605254612;
    private final int end = 1605255300;

    @Before
    public void setUp() throws Exception {
        this.recordManager = new RecordManager(auth);
    }

    @Test
    public void testOndemandRecord() throws Exception {
        try {
            res = recordManager.startRecord(namespaceId, streamId);
            Assert.assertNotNull(res);
            System.out.println(res.bodyString());
            Thread.sleep(1000 * 60);
            res2 = recordManager.stopRecord(namespaceId, streamId);
            Assert.assertNotNull(res2);
            System.out.println(res2.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
            if (res2 != null) {
                res2.close();
            }
        }
    }

    @Test
    public void testRecordClipsSaveas() {
        try {
            res = recordManager.recordClipsSaveas(namespaceId, streamId, "", format, start, end, false, "", "", 0);
            Assert.assertNotNull(res);
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
    public void testRecordsPlayback() {
        try {
            res = recordManager.recordsPlayback(namespaceId, streamId, start, end);
            Assert.assertNotNull(res);
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
