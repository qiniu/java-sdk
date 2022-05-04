package test.com.qiniu.qvs;

import com.qiniu.http.Response;
import com.qiniu.qvs.RecordManager;
import com.qiniu.util.Auth;
import test.com.qiniu.TestConfig;
import org.junit.jupiter.api.BeforeEach;

public class RecordTest {
    Auth auth = TestConfig.testAuth;
    private RecordManager recordManager;
    private Response res = null;
    private Response res2 = null;
    private final String namespaceId = "3nm4x1e0xw855";
    private final String streamId = "31011500991320007536";
    private final String format = "m3u8";
    private final int start = 1640599830;
    private final int end = 1640600430;

    @BeforeEach
    public void setUp() throws Exception {
        this.recordManager = new RecordManager(auth);
    }

/*  @Test
    @Tag("IntegrationTest")
    public void testOndemandRecord() throws Exception {
        try {
            res = recordManager.startRecord(namespaceId, streamId);
            assertNotNull(res);
            System.out.println(res.bodyString());
            Thread.sleep(1000 * 60);
            res2 = recordManager.stopRecord(namespaceId, streamId);
            assertNotNull(res2);
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
    @Tag("IntegrationTest")
    public void testRecordClipsSaveas() {
        try {
            res = recordManager.recordClipsSaveas(namespaceId, streamId, "", format, start, end, false, "", "", 0);
            assertNotNull(res);
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
    public void testRecordsPlayback() {
        try {
            res = recordManager.recordsPlayback(namespaceId, streamId, start, end);
            assertNotNull(res);
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
