package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.DeviceManager;
import com.qiniu.qvs.model.Device;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class DeviceManagerTest {
    Auth auth = TestConfig.testAuth;
    private DeviceManager deviceManager;
    private Response res = null;
    private String namespaceId = "2xenzw3lpzpdz";
    private String gbId = "31011500991320000127";
    private String[] channels = {"31011500991320000127"};


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
            Assert.assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            Assert.assertEquals("401", res.statusCode);
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
            Assert.assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            Assert.assertEquals("401", res.statusCode);
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
            Assert.assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            Assert.assertEquals("401", res.statusCode);
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
            Assert.assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            Assert.assertEquals("401", res.statusCode);
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
    public void testStartDevice() {
        try {
            res = deviceManager.startDevice(namespaceId, gbId, channels);
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
    public void testStopDevice() {
        try {
            res = deviceManager.stopDevice(namespaceId, gbId, channels);
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
