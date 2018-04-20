package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.rtc.AppManager;
import com.qiniu.rtc.RoomManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;


public class RtcTest {
    private String ak = "DXFtikq1Y";//AccessKey you get from qiniu
    private String sk = "F397hz";//SecretKey you get from qiniu
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
    public void creatApp(){
        try {
            System.out.print( manager.creatApp("zw111","zwhome",10,false,false,false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getApp(){
        try {
            System.out.print( manager.getApp("dex74xpqd"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteApp(){
        try {
            System.out.print( manager.deleteApp("dex74xpqd"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        catch (Exception e1){

        }
    }

    @Test
    public void updateApp(){
        try {
            System.out.print( manager.updateApp("dex74xpqd","zwte123","zw111",10,false,false,false));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //鉴权失败
    @Test
    public void listUser(){
        try {
            System.out.print(rmanager.listUser("dex74xpqd","ww"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void kickUser(){
        try {
            //String token = rmanager.getRoomToken(mac,"d7rqwfxqd","roomid","userid",3600,"admin");
            System.out.print(rmanager.kickUser("dex74xpqd","roomid","userid"));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        catch (Exception e1){

        }
    }
    @Test
    public void listActiveRoom(){
        try {
            System.out.print(rmanager.listActiveRoom("dex74xpqd",null,1,2));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getRoomToken(){
        try {
            System.out.print(rmanager.getRoomToken("dex74xpqd","roomid","userid",3600,"admin"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
