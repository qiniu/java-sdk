package test.com.qiniu.qvs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.DeviceManager;
import com.qiniu.qvs.model.Device;
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
    private final String namespaceId = "2xenzw3lpzpdz";
    private final String gbId = "31011500991320000127";
    private final String[] channels = { "31011500991320000127" };

    @BeforeEach
    public void setUp() throws Exception {
        this.deviceManager = new DeviceManager(auth);
    }

    @Test
    @Tag("IntegrationTest")
    public void testCreateDevice() {
        Device device = new Device();
        device.setUsername("admin");
        device.setPassword("QQQNNN111");
        try {
            res = deviceManager.createDevice(namespaceId, device);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals("401", res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryDevice() {
        try {
            res = deviceManager.queryDevice(namespaceId, gbId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals("401", res.statusCode);
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
        PatchOperation[] patchOperation = { new PatchOperation("replace", "name", "GBTEST") };
        try {
            res = deviceManager.updateDevice(namespaceId, gbId, patchOperation);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals("401", res.statusCode);
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
        String prefix = "310";
        String state = "notReg";
        try {
            res = deviceManager.listDevice(namespaceId, offset, line, prefix, state, qtype);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            assertEquals("401", res.statusCode);
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testListChannels() {
        String prefix = "310";
        try {
            res = deviceManager.listChannels(namespaceId, gbId, prefix);
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
            res = deviceManager.fetchCatalog("2xenzw5o81ods", "31011500991320000356");
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
            res = deviceManager.queryChannel("3nm4x0vyz7xlu", "31011500991180000270", "34020000001310000020");
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
            res = deviceManager.queryGBRecordHistories("3nm4x0vyz7xlu", "31011500991180000270", "34020000001310000020",
                    1604817540, 1604903940);
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
}
