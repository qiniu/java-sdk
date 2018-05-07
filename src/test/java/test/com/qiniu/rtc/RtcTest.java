package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.rtc.RtcAppManager;
import com.qiniu.rtc.RtcRoomManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;


public class RtcTest {
    private String ak = "DXFtikq1YuDT"; //AccessKey you get from qiniu
    private String sk = "F397hz"; //SecretKey you get from qiniu
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
            System.out.print(manager.getApp("dg8emfd4t"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteApp() {
        try {
            System.out.print(manager.deleteApp("dg8emfd4t"));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    @Test
    public void updateApp() {
        try {
            System.out.print(manager.updateApp("dg8emfd4t", "333", "zwhome", 10, false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listUser() {
        try {
            System.out.print(rmanager.listUser("dg8emfd4t", "zwhome"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void kickUser() {
        try {
            System.out.print(rmanager.kickUser("dg8emfd4t", "zwhome", "userid"));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    @Test
    public void listActiveRooms() {
        try {
            System.out.print(rmanager.listActiveRooms("dg8emfd4t", null, 1, 2));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getRoomToken() {
        try {
            System.out.print(rmanager.getRoomToken("dg8emfd4t", "zwhome", "zhangwei", 1525410499, "user"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
