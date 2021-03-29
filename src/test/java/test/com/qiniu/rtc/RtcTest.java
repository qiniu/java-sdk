package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.RtcAppManager;
import com.qiniu.rtc.RtcRoomManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;


public class RtcTest {
    private Auth auth = TestConfig.testAuth;
    private RtcAppManager manager = new RtcAppManager(auth);
    private RtcRoomManager rmanager = new RtcRoomManager(auth);

    //@Test
    public void createApp() {
        try {
            Response response = manager.createApp("test0024", "zwhome", 10, false);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void getApp() {
        try {
            Response response = manager.getApp("dg8lz5211");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void deleteApp() {
        try {
            Response response = manager.deleteApp("dg8lz5211");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void updateApp() {
        try {
            Response response = manager.updateApp("dg8lz5211", "333", "zwhome", 10, false);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void listUser() {
        try {
            Response response = rmanager.listUser("dg8lz5211", "zwhome");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    //@Test
    public void kickUser() {
        try {
            Response response = rmanager.kickUser("dg8lz5211", "zwhome", "userid");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e1) {

        }
    }

    //@Test
    public void listActiveRooms() {
        try {
            Response response = rmanager.listActiveRooms("dg8lz5211", null, 1, 2);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void getRoomToken() {
        try {
            System.out.print(rmanager.getRoomToken("dg8emfd4t", "zwhome", "zhangwei", 1525410499, "user"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCalculateForRoom(){
        try {
            Response response =rmanager.getCalculateForRoom("dix05zn6q", "roomtest", "20200317000000", "20200317193000", null,0,null);
            System.out.print(getString(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getString(Response response) {
        String[] resJson = response.getInfo().split("\n");
        return response.statusCode + "\n" + resJson[2];
    }
}
