package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.rtc.AppManager;
import com.qiniu.rtc.RoomManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;


public class RtcTest {
    private String ak = "DXFtikq1YuD"; //AccessKey you get from qiniu
    private String sk = "F397hz"; //SecretKey you get from qiniu
    private Auth auth = null;

    {
        try {
            auth = Auth.create(ak, sk);
        } catch (Exception e) {
            auth = TestConfig.testAuth;
        }
    }

    private AppManager manager = new AppManager(auth);
    private RoomManager rmanager = new RoomManager(auth);

    @Test
    public void creatApp() {
        try {
            System.out.print(manager.creatApp("test0022", "zwhome", 10, false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getApp() {
        try {
            System.out.print(manager.getApp("test0001"));
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
            System.out.print(manager.updateApp("dfykiv8e5", "kongjiang", "zw111", 10, false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listUser() {
        try {
            System.out.print(rmanager.listUser("dfykiv8e5", "ww"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void kickUser() {
        try {
            System.out.print(rmanager.kickUser("dfykiv8e5", "roomid", "userid"));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    @Test
    public void listActiveRoom() {
        try {
            System.out.print(rmanager.listActiveRoom("dfykiv8e5", null, 1, 2));
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
