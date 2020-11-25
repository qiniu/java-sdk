package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.StatsManager;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class StatsTest {
    Auth auth = TestConfig.testAuth;
    private StatsManager statsManager;
    private Response res = null;
    private final String namespaceId = "";
    private final String streamId = "";
    private final String tu = "5min";
    private final int start = 20200901;
    private final int end = 20200902;

    @Before
    public void setUp() throws Exception {
        this.statsManager = new StatsManager(auth);
    }

    @Test
    public void testQueryFlow() {
        try {
            res = statsManager.queryFlow(namespaceId, streamId, tu, start, end);
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
    public void testQueryBandwidth() {
        try {
            res = statsManager.queryBandwidth(namespaceId, streamId, tu, start, end);
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
