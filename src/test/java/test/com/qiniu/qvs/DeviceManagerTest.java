package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.DeviceManager;
import com.qiniu.qvs.model.Device;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.util.Auth;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class DeviceManagerTest {
    Auth auth = TestConfig.testAuth;
    private DeviceManager deviceManager;
    private Response res = null;
    private String namespaceId = "3nm4x0v0h6vjr";
    private String gbId = "31011500991320000056";


    @Before
    public void setUp() throws Exception {
        this.deviceManager = new DeviceManager(auth);
    }

    @Test
    public void testCreateDevice() {
        Device device = new Device();
        device.setUsername("admin");
        device.setPassword("QQQNNN111");
        try {
            res = deviceManager.createDevice(namespaceId, device);
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
    public void testQueryDevice() {
        try {
            res = deviceManager.queryDevice(namespaceId, gbId);
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
    public void testUpdateDevice() {
        PatchOperation[] patchOperation = {new PatchOperation("replace", "name", "GBTEST")};
        try {
            res = deviceManager.updateDevice(namespaceId, gbId, patchOperation);
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
    public void testListDevice() {
        int offset = 0;
        int line = 3;
        int qtype = 0;
        String prefix = "310";
        String state = "notReg";
        try {
            res = deviceManager.listDevice(namespaceId, offset, line, prefix, state, qtype);
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
    public void testListChannels() {
        String prefix = "310";
        try {
            res = deviceManager.listChannels(namespaceId, gbId, prefix);
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
    public void testStartDevice() {
        try {
            res = deviceManager.startDevice(namespaceId, gbId, "31011500991320000056");
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
    public void testStopDevice() {
        try {
            res = deviceManager.stopDevice(namespaceId, gbId, "31011500991320000056");
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
    public void testDeleteDevice() {
        try {
            res = deviceManager.deleteDevice(namespaceId, gbId);
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
