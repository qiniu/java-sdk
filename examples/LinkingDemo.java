import com.qiniu.common.QiniuException;
import com.qiniu.linking.*;
import com.qiniu.linking.model.*;
import com.qiniu.util.Auth;
import sun.security.tools.jarsigner.TimestampedSigner;

import java.security.Timestamp;
import java.util.Date;


public class LinkingDemo {

    final static String testAk = "ak";
    final static String testSk = "sk";
    final static String testAppid = "appid";
    final static String testHost = "http://linking.qiniuapi.com";
    final static String testDeviceName1 = "test1";
    final static String testDeviceName2 = "test2";
    public static void main(String[] args) {

        Auth auth = Auth.create(testAk,testSk);
        LinkingDeviceManager deviceManager = new LinkingDeviceManager(auth,testHost);
        try{
            //创建设备
            deviceManager.createDevice(testAppid,testDeviceName1);
        } catch (QiniuException e){
            System.out.println(e.error());
        }

        try{
            //添加dak
            DeviceKey[] keys = deviceManager.addDeviceKey(testAppid,testDeviceName1);
            System.out.println(keys[0].getAccessKey());
            System.out.println(keys[0].getSecretKey());
        }catch (QiniuException e){
            System.out.println(e.error());
        }

        try{
            //查询设备
            DeviceKey[] keys = deviceManager.queryDeviceKey(testAppid,testDeviceName1);
            if (keys.length==1){
                throw new QiniuException(new Exception(),"expect one length");
            }
            //删除设备
            deviceManager.deleteDeviceKey(testAppid,testDeviceName1,keys[0].getAccessKey());
            keys = deviceManager.queryDeviceKey(testAppid,testDeviceName1);
            if (keys.length==0){
                throw new QiniuException(new Exception(),"expect zero length");
            }
        }catch (QiniuException e){
            System.out.println(e.error());
        }

        try{
            //列出设备
            DeviceListing deviceslist = deviceManager.listDevice(testAppid,"","",  1,false);
            System.out.println(deviceslist.items.length);
        }catch (QiniuException e){
            System.out.println(e.error());
        }



        try{
            //修改设备字段
            PatchOperation[] operations={new PatchOperation("replace","segmentExpireDays",9)};
            Device device= deviceManager.updateDevice(testAppid,testDeviceName1,operations);
            System.out.println(device.getSegmentExpireDays());
        }catch (QiniuException e){
            System.out.println(e.error());
        }


        try{
            //查询设备在线历史记录
            DeviceHistoryListing history= deviceManager.listDeviceHistory(testAppid,testDeviceName1,
                    0,(new Date().getTime())/1000,"",0);
        }catch (QiniuException e){
            System.out.println(e.error());
        }



        try{
            //删除设备信息
            deviceManager.deleteDevice(testAppid,testDeviceName1);
        } catch (QiniuException e){
            System.out.println(e.error());
        }

        try{
            deviceManager.createDevice(testAppid,testDeviceName1);
            deviceManager.createDevice(testAppid,testDeviceName2);

            //添加dak
            deviceManager.addDeviceKey(testAppid,testDeviceName1);
            DeviceKey[] keys = deviceManager.queryDeviceKey(testAppid,testDeviceName1);
            String dak = keys[0].getAccessKey();

            //移动dak
            deviceManager.cloneDeviceKey(testAppid,testDeviceName1,testDeviceName2,true, false,dak);
            Device device = deviceManager.getDeviceByAccessKey(dak);
            device.getDeviceName();

            deviceManager.deleteDeviceKey(testAppid,testDeviceName2,dak);

            String token;
            //生成具有所有功能的token
            String[] actions = new String[]{Auth.DTOKEN_ACTION_STATUS,Auth.DTOKEN_ACTION_VOD,Auth.DTOKEN_ACTION_TUTK};
            token = auth.generateLinkingDeviceTokenWithExpires(testAppid,testDeviceName1,1000,actions);
            //生成视频相关功能的token
            token = auth.generateLinkingDeviceVodTokenWithExpires(testAppid,testDeviceName1,1000,actions);
            //生成获取设备状态的token
            token = auth.generateLinkingDeviceStatusTokenWithExpires(testAppid,testDeviceName1,1000,actions);

        }catch (QiniuException e){
            System.out.println(e.error());
        }finally {
            try{
                deviceManager.deleteDevice(testAppid,testDeviceName1);
            }catch (Exception ignored){
            }
            try{
                deviceManager.deleteDevice(testAppid,testDeviceName2);
            }catch (Exception ignored){
            }
        }


    }
}
