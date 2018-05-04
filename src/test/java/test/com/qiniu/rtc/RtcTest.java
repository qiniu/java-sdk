package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.rtc.RtcAppManager;
import com.qiniu.rtc.RtcRoomManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;


public class RtcTest {
    private String ak = "DXFtikq1YuDT_WMUntOpzpWPm2UZVtEnYvN3-CUD"; //AccessKey you get from qiniu
    private String sk = "F397hzMohpORVZ-bBbb-IVbpdWlI4SWu8sWq78v3"; //SecretKey you get from qiniu
    private Auth auth = null;

    {
        try {
            auth = Auth.create(ak, sk);
        } catch (Exception e) {
            auth = TestConfig.testAuth;
        }
    }

    private RtcAppManager manager = new RtcAppManager(auth);
    private RtcRoomManager rmanager = new RtcRoomManager(auth);

    @Test
    public void createApp() {
        try {
            System.out.print(manager.createApp("test0024", "zwhome", 10, false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getApp() {
        try {
            System.out.print(manager.getApp("dg0yls84i"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteApp() {
        try {
            System.out.print(manager.deleteApp("dex74xpqd"));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    @Test
    public void updateApp() {
        try {
            System.out.print(manager.updateApp("dg0yls84i", "hello", "zw111", 10, false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listUser() {
        try {
            System.out.print(rmanager.listUser("dg0yls84i", "zw111"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void kickUser() {
        try {
            System.out.print(rmanager.kickUser("dg0yls84i", "zw111", "userid"));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    @Test
    public void listActiveRoom() {
        try {
            System.out.print(rmanager.listActiveRoom("dg0yls84i", null, 1, 2));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getRoomToken() {
        try {
            System.out.print(rmanager.getRoomToken("dg0b80olh", "zwhome", "zhangwei", 1525410499, "user"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
