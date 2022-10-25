package test.com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.rtc.QRTC;
import com.qiniu.rtc.QRTCClient;
import com.qiniu.rtc.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import java.util.Arrays;


public class RtcTest {

    public static QRTCClient client = null;

    @Disabled
    @BeforeAll
    public static void initQRTC() throws QiniuException {
        AppParam appParam = new AppParam();
        appParam.setTitle("test_rtc_example");
        appParam.setMaxUsers(20);
        appParam.setNoAutoKickUser(false);
        QRTCResult<AppResult> result = QRTC.createApp(appParam, TestConfig.testAccessKey, TestConfig.testSecretKey);
        //初始化接口，一般有appId的话建议直接走这个接口初始化了
        client = QRTC.init(TestConfig.testAccessKey, TestConfig.testSecretKey, result.getResult().getAppId());
    }

    @Disabled
    @Test
    public void testApp() throws QiniuException {
        QRTCResult<AppResult> result = client.getApp();
        System.out.println(result);
        assert result.getCode() == 200;

        //更新app
        AppParam appParam = new AppParam();
        appParam.setTitle("test_rtc_example");
        appParam.setMaxUsers(15);
        appParam.setAppId(result.getResult().getAppId());
        result = client.updateApp(appParam);
        assert result.getCode() == 200;
    }

    @Disabled
    @Test
    public void testRoom() throws Exception {
        //创建房间
        RoomParam roomParam = new RoomParam();
        roomParam.setRoomName("test_room_create");
        roomParam.setOpenRoom(true);
        roomParam.setAutoCloseTtlS(30);
        roomParam.setNoEntreTtlS(3600);
        roomParam.setMaxUsers(20);
        QRTCResult<RoomResult> roomResult = client.createRoom(roomParam);
        assert roomResult.getCode() == 200;
        //获取房间
        QRTCResult<RoomResult> result = client.listUser("1207");
        assert result.getCode() == 200;
        //拉取活跃用户
        QRTCResult<RoomResult> result1 = client.listActiveRoom("12", 0, 10);
        assert result1.getCode() == 200;
        //踢出房间内用户
        QRTCResult<RoomResult> result2 = client.kickUser("1207", "testUser");
        assert result2.getCode() == 200;
        //获取token
        String token = client.getRoomToken("1207", "testUser", System.currentTimeMillis() / 1000, "user");
        System.out.println(token);
        //删除房间
        roomResult = client.deleteRoom(roomParam.getRoomName());
        assert roomResult.getCode() == 200;
    }

    @Disabled
    @Test
    public void testForward() throws Exception {
        //发起转推
        ForwardParam param = new ForwardParam();
        param.setId("111");
        param.setPlayerId("admin");
        param.setPublishUrl("rtmp://pili-publish.qnsdk.com/sdk-live/1206_javaapi_test");
        ForwardParam.TrackInfo trackInfo = new ForwardParam.TrackInfo("trackId_test_111");
        param.setTracks(Arrays.asList(trackInfo));
        QRTCResult<ForwardResult> result = client.createForwardJob("1207", param);
        assert result.getCode() == 200;

        //stop forward
        result = client.stopForwardJob("1207", param);
        assert result.getCode() == 200;
    }

    @Disabled
    @Test
    public void testMerge() throws QiniuException {
        MergeParam mergeParam = new MergeParam();
        mergeParam.setId("test_merge");
        mergeParam.setAudioOnly(false);
        mergeParam.setPublishUrl("rtmp://pili-publish.qnsdk.com/sdk-live/1206_javaapi_test");
        mergeParam.setFps(24);
        mergeParam.setHeight(720);
        mergeParam.setWidth(1024);
        mergeParam.setKbps(1000);
        mergeParam.setHoldLastFrame(false);
        mergeParam.setStretchMode("aspectFill");
        mergeParam.setTemplate("horizontal");

        //设置用户流信息
        MergeParam.MergeUserInfo userInfo1 = new MergeParam.MergeUserInfo();
        userInfo1.setUserId("admin");
        userInfo1.setStretchMode("aspectFill");
        userInfo1.setSequence(1);
        userInfo1.setBackgroundUrl("https://img2.baidu.com/it/u=622902245,3661794572&fm=26&fmt=auto");
        MergeParam.MergeUserInfo userInfo2 = new MergeParam.MergeUserInfo();
        userInfo2.setUserId("testUser");
        userInfo2.setStretchMode("aspectFill");
        userInfo2.setSequence(1);
        userInfo2.setBackgroundUrl("https://img2.baidu.com/it/u=622902245,3661794572&fm=26&fmt=auto");
        mergeParam.setUserInfos(Arrays.asList(userInfo1, userInfo2));

        //设置背景图
        MergeParam.MergeBackGround backGround = new MergeParam.MergeBackGround();
        backGround.setStretchMode("aspectFill");
        backGround.setUrl("https://img2.baidu.com/it/u=622902245,3661794572&fm=26&fmt=auto");
        backGround.setX(0);
        backGround.setY(0);
        backGround.setH(120);
        backGround.setW(1000);
        mergeParam.setBackground(backGround);
        //添加水印
        MergeParam.MergeWaterMarks watermarks = new MergeParam.MergeWaterMarks();
        watermarks.setUrl("https://img2.baidu.com/it/u=622902245,3661794572&fm=26&fmt=auto");
        watermarks.setX(0);
        watermarks.setY(0);
        watermarks.setH(20);
        watermarks.setW(20);
        watermarks.setStretchMode("aspectFill");
        mergeParam.setWaterMarks(Arrays.asList(watermarks));

        QRTCResult<MergeResult> result = client.createMergeJob("1207", mergeParam);
        assert result.getCode() == 200;

        //更新合流track信息
        MergeTrackParam mergeTrackParam = new MergeTrackParam();
        MergeTrackParam.MergeTrack track = new MergeTrackParam.MergeTrack();
        track.setTrackID("12345xszsw");
        track.setSupportSei(true);
        track.setX(0);
        track.setY(0);
        track.setH(20);
        track.setW(20);
        track.setStretchMode("aspectFill");
        mergeTrackParam.setAdd(Arrays.asList(track));
        QRTCResult<MergeResult> upResult = client.updateMergeTrack(mergeTrackParam, "1207", result.getResult().getId());
        assert upResult.getCode() == 200;

        //停止合流
        QRTCResult<MergeResult> stopResult = client.stopMergeJob("1207", result.getResult().getId());
        assert stopResult.getCode() == 200;
    }


    /**
     * 最后清除创建的app数据
     *
     * @throws QiniuException
     */
    @Disabled
    @AfterAll
    public static void deleteApp() throws QiniuException {
        QRTCResult<AppResult> result = client.deleteApp();
        assert result.getCode() == 200;
    }

}
