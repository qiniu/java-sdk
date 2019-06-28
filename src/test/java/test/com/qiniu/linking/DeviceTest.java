package test.com.qiniu.linking;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.linking.LinkingDeviceManager;
import com.qiniu.linking.model.*;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.util.Date;

public class DeviceTest {

    private static final String testDeviceName1 = "test1";
    private static final String testDeviceName2 = "test2";
    private static final Client client = new Client();

    @Test
    @Ignore
    public void testDevice() throws QiniuException {
        Auth auth = TestConfig.testAuth;
        String testAppid = TestConfig.testLinkingAppid;
        LinkingDeviceManager deviceManager = new LinkingDeviceManager(auth);
        try {
            // CHECKSTYLE:OFF

            {
                //创建设备
                deviceManager.createDevice(testAppid, testDeviceName1);
            }

            {
                //添加dak
                DeviceKey[] keys = deviceManager.addDeviceKey(testAppid, testDeviceName1);
                Assert.assertNotEquals(keys.length, 0);
            }

            {
                //查询设备
                DeviceKey[] keys = deviceManager.queryDeviceKey(testAppid, testDeviceName1);
                Assert.assertEquals(keys.length, 1);

                //删除设备
                deviceManager.deleteDeviceKey(testAppid, testDeviceName1, keys[0].getAccessKey());
                keys = deviceManager.queryDeviceKey(testAppid, testDeviceName1);
                Assert.assertEquals(keys.length, 0);
            }

            {
                //列出设备
                DeviceListing deviceslist = deviceManager.listDevice(testAppid, "", "", 1, false);
                Assert.assertNotEquals(deviceslist.items.length, 0);
            }


            {
                //修改设备字段
                PatchOperation[] operations = {new PatchOperation("replace", "segmentExpireDays", 9)};
                Device device = deviceManager.updateDevice(testAppid, testDeviceName1, operations);
                Assert.assertEquals(device.getSegmentExpireDays(), 9);
            }

            {
                //查询设备在线历史记录
                DeviceHistoryListing history = deviceManager.listDeviceHistory(testAppid, testDeviceName1,
                        0, (new Date().getTime()) / 1000, "", 0);
            }

            // CHECKSTYLE:ON
        } finally {
            //删除设备信息
            deviceManager.deleteDevice(testAppid, testDeviceName1);
        }

    }

    @Test
    @Ignore
    public void testDeviceKey() throws QiniuException {
        Auth auth = TestConfig.testAuth;
        String testAppid = TestConfig.testLinkingAppid;
        LinkingDeviceManager deviceManager = new LinkingDeviceManager(auth);
        try {
            deviceManager.createDevice(testAppid, testDeviceName1);
            deviceManager.createDevice(testAppid, testDeviceName2);

            //添加dak
            deviceManager.addDeviceKey(testAppid, testDeviceName1);
            DeviceKey[] keys = deviceManager.queryDeviceKey(testAppid, testDeviceName1);
            String dak = keys[0].getAccessKey();

            //移动dak
            deviceManager.cloneDeviceKey(testAppid, testDeviceName1, testDeviceName2, true, false, dak);
            Device device = deviceManager.getDeviceByAccessKey(dak);

            deviceManager.deleteDeviceKey(testAppid, testDeviceName2, dak);

        } finally {
            try {
                deviceManager.deleteDevice(testAppid, testDeviceName1);
            } catch (Exception ignored) {
            }
            try {
                deviceManager.deleteDevice(testAppid, testDeviceName2);
            } catch (Exception ignored) {
            }
        }
    }


    @Test
    @Ignore
    public void testLinkingDeviceToken() throws QiniuException {
        String vodToken = TestConfig.testAuth.generateLinkingDeviceVodTokenWithExpires(
                TestConfig.testLinkingAppid, testDeviceName1, 1000);
        String statusToken = TestConfig.testAuth.generateLinkingDeviceStatusTokenWithExpires(
                TestConfig.testLinkingAppid, testDeviceName1, 1000);
        String testAppid = TestConfig.testLinkingAppid;
        LinkingDeviceManager deviceManager = new LinkingDeviceManager(TestConfig.testAuth);
        try {
            deviceManager.createDevice(testAppid, testDeviceName1);
            StringMap map = new StringMap().put("dtoken", statusToken);
            String queryString = map.formString();
            String url = String.format("%s/v1/device/resource/status?%s",
                    "http://linking.qiniuapi.com", queryString);
            Response res = client.get(url);
            int code = res.statusCode;
            res.close();
            Assert.assertNotEquals(code, 401);
        } finally {
            deviceManager.deleteDevice(testAppid, testDeviceName1);
        }

    }

}
