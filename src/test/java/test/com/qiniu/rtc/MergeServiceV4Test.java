package test.com.qiniu.rtc;

import com.qiniu.rtc.QRTC;
import com.qiniu.rtc.QRTCClient;

import org.junit.jupiter.api.BeforeAll;
import test.com.qiniu.TestConfig;

class MergeServiceV4Test {
    public static QRTCClient client = null;

    public static String roomName = "room";

    @BeforeAll
    public void setUp() {
        client = QRTC.init(TestConfig.testAccessKey, TestConfig.testSecretKey, "fxrav2mql");
    }

/*    @Test
    void mergeJob() throws Exception {
        MergeJob mergeJob = new MergeJob();
        mergeJob.setType("basic");

        MediaInput mediaInput1 = new MediaInput();
        mediaInput1.setKind("media");
        mediaInput1.setUserId("zhuozi1");
        mediaInput1.setPosition(new MediaPosition(0, 0, 601, 480, 1));

        MediaInput mediaInput2 = new MediaInput();
        mediaInput2.setKind("image");
        mediaInput2.setPosition(new MediaPosition(0, 0, 50, 50, 2));
        mediaInput2.setUrl("https://www.boredpanda.com/blog/wp-content/uploads/2020/06/Artist-shows-alternative-versions-of-famous-logos-in-different-styles-5ed4ac823b564__880.jpg");

        List<MediaInput> inputs = new ArrayList<MediaInput>();
        inputs.add(mediaInput1);
        inputs.add(mediaInput2);

        MediaOutput mediaOutput1 = new MediaOutput();
        mediaOutput1.setType("rtmp");
        mediaOutput1.setUrl("rtmp://pili-publish.qnsdk.com/sdk-live/hugotest?expire=1651744685&token=Jt8FYi5eceUEoaFWMkcIQNsvQqs=");

        List<MediaOutput> outputs = new ArrayList<>();
        outputs.add(mediaOutput1);

        MediaConfig mediaConfig = new MediaConfig();
        mediaConfig.setFps(20);
        mediaConfig.setWidth(600);
        mediaConfig.setHeight(480);
        mediaConfig.setKbps(1000);

        mergeJob.setConfig(mediaConfig);
        mergeJob.setInputs(inputs);
        mergeJob.setOutputs(outputs);
        System.out.println(new Gson().toJson(mergeJob));
        QRTCResult<MergeResult> result = client.createMergeJob(roomName, mergeJob);

        System.out.println("创建合流 res" + new Gson().toJson(result));
        assert result.getCode() == 200;
        assert result.getResult() != null;
        MergeResult mergeResult = result.getResult();

        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());

        String mergeJobId = mergeResult.getId();

        //删除水印
        Thread.sleep(1000 * 60);
        mergeJob.setId(mergeJobId);
        mergeJob.getInputs().remove(1);
        result = client.updateMergeJob(roomName, mergeJob);
        System.out.println("删除水印 res" + new Gson().toJson(result));

        assert result.getCode() == 200;
        assert result.getResult() != null;

        mergeResult = result.getResult();
        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());

        //添加水印
        Thread.sleep(1000 * 60);
        mediaInput2.setPosition(new MediaPosition(200, 200, 50, 50, 2));
        mergeJob.getInputs().add(mediaInput2);
        result = client.updateMergeJob(roomName, mergeJob);
        System.out.println("添加水印 res" + new Gson().toJson(result));

        assert result.getCode() == 200;
        assert result.getResult() != null;

        mergeResult = result.getResult();
        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());

        // 停止合流
        Thread.sleep(1000 * 60);
        result = client.stopMergeJobById(roomName, mergeJobId);
        System.out.println("停止合流 res" + new Gson().toJson(result));

        assert result.getCode() == 200;
        assert result.getResult() != null;

        mergeResult = result.getResult();
        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());
    }
 */
}
