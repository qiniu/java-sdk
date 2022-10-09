package test.com.qiniu.qvs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.DeviceManager;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

public class DeviceManagerTest {
    Auth auth = TestConfig.testAuth;
    private DeviceManager deviceManager;
    private Response res = null;
    private Response res2 = null;
    private final String namespaceId = "3nm4x1e0xw855";
    private final String gbId = "31011500991320007536";
    private final String[] channels = {"31011500991320007536"};

    @BeforeEach
    public void setUp() throws Exception {
        this.deviceManager = new DeviceManager(auth);
    }

/*  @Test
    @Tag("IntegrationTest")
    public void testCreateDevice() {
        Device device = Device.builder().username("admin").password("QQQNNN111").build();
        try {
            res = deviceManager.createDevice(namespaceId, device);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
//            assertEquals(401, res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
*/

    @Test
    @Tag("IntegrationTest")
    public void testQueryDevice() {
        try {
            res = deviceManager.queryDevice(namespaceId, gbId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals(401, res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUpdateDevice() {
        PatchOperation[] patchOperation = {new PatchOperation("replace", "name", "GBTEST001")};
        try {
            res = deviceManager.updateDevice(namespaceId, gbId, patchOperation);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals(401, res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testListDevice() {
        int offset = 0;
        int line = 3;
        int qtype = 0;
        String prefix = "中文";
        String state = "notReg";
        try {
            res = deviceManager.listDevice(namespaceId, offset, line, prefix, state, qtype);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals(401, res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

//    @Test
//    @Tag("IntegrationTest")
//    public void testListChannels() {
//        String prefix = "310";
//        try {
//            res = deviceManager.listChannels(namespaceId, gbId, prefix);//TODO
//            assertNotNull(res);
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }

 /* @Test
    @Tag("IntegrationTest")
    public void testStartDevice() {
        try {
            res = deviceManager.startDevice(namespaceId, gbId, channels);
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
    public void testStopDevice() {
        try {
            res = deviceManager.stopDevice(namespaceId, gbId, channels);
            res2 = deviceManager.startDevice(namespaceId, gbId, channels);
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
    public void testFetchCatalog() {
        try {
            res = deviceManager.fetchCatalog(namespaceId, gbId);
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
    public void testQueryChannel() {
        try {
            res = deviceManager.queryChannel(namespaceId, gbId, channels[0]);//TODO
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
    public void testQueryGBRecordHistories() {
        try {
            res = deviceManager.queryGBRecordHistories(namespaceId, gbId, channels[0],
                    1639379380, 1639379981);
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
    public void testQueryGBRecordHistories() {
        try {
            res = deviceManager.queryGBRecordHistories("qiniu", "31011500991180013385", "34020000001310000001",
                    1665190800, 1665192093);
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
     public void controlGBRecord() {
        try {
            PlayContral playContral = new PlayContral("play","5",2.0F);
            res = deviceManager.controlGBRecord("qiniu", "31011500991180013385_34020000001310000001_history_1665190800_1665192092", playContral);
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
