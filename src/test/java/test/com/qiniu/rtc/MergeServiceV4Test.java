package test.com.qiniu.rtc;

import com.google.gson.Gson;
import com.qiniu.rtc.QRTC;
import com.qiniu.rtc.QRTCClient;
import com.qiniu.rtc.model.*;
import com.qiniu.util.StringUtils;

import com.qiniu.util.UrlSafeBase64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * author: 桌子
 * Email : zhuozi@meili-inc.com
 * datetime: 2022-04-24 13:58
 */
class MergeServiceV4Test {
    public static QRTCClient client = null;

    public static String InternalUrlTag = "/.i/";
    public static String roomName = "room1";

    @BeforeAll
    static void setUp() {
        client = QRTC.init(TestConfig.testAccessKey, TestConfig.testSecretKey, "g2m0ya7w7");
    }

    @Test
    void createMergeJob() throws Exception{
        MergeJob mergeJob = new MergeJob();
        mergeJob.setType("basic");

        MediaInput mediaInput1 = new MediaInput();
        mediaInput1.setKind("media");
        mediaInput1.setUserId("zhuozi001");
        mediaInput1.setPosition(new MediaPosition(0,0,1080,720, 1));

        MediaInput mediaInput2 = new MediaInput();
        mediaInput2.setKind("image");
        mediaInput2.setPosition(new MediaPosition(0,0,50,50, 2));
        mediaInput2.setUrl("https://www.boredpanda.com/blog/wp-content/uploads/2020/06/Artist-shows-alternative-versions-of-famous-logos-in-different-styles-5ed4ac823b564__880.jpg");

        List<MediaInput> inputs = new ArrayList<MediaInput>();
        inputs.add(mediaInput1); inputs.add(mediaInput2);

        MediaOutput mediaOutput1 = new MediaOutput();
        mediaOutput1.setType("rtmp");
        String rtmpurl = generateExpiryskUrl("zhuozi001001",  3600*3);
        System.out.println(rtmpurl);
        mediaOutput1.setUrl(rtmpurl);

        List<MediaOutput> outputs = new ArrayList<>();
        outputs.add(mediaOutput1);

        mergeJob.setInputs(inputs);
        mergeJob.setOutputs(outputs);
        System.out.println(new Gson().toJson(mergeJob));
        QRTCResult<MergeResult> result = client.createMergeJob(roomName, mergeJob);
        System.out.println("创建合流 res" + new Gson().toJson(result));
        assert result.getCode() == 200;
        assert result.getResult() != null;
        MergeResult mergeResult  = result.getResult();

        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());

        String mergeJobId = mergeResult.getId();

        //删除水印
        Thread.sleep(1000 * 2 * 60);
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
        Thread.sleep(1000 * 2 * 60);
        mediaInput2.setPosition(new MediaPosition(200, 200, 50, 50, 0));
        mergeJob.getInputs().add(mediaInput2);
        result = client.updateMergeJob(roomName, mergeJob);
        System.out.println("添加水印 res" +  new Gson().toJson(result));

        assert result.getCode() == 200;
        assert result.getResult() != null;

        mergeResult = result.getResult();
        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());

        // 停止合流
        Thread.sleep(1000 * 2 * 60);
        result = client.stopMergeJobById(roomName, mergeJobId);
        System.out.println("停止合流 res" + new Gson().toJson(result));

        assert result.getCode() == 200;
        assert result.getResult() != null;

        mergeResult = result.getResult();
        assert mergeResult.getStatus().equals("OK");
        assert !StringUtils.isNullOrEmpty(mergeResult.getId());
    }

    public static String generateExpiryskUrl(String streamKey, long expiretime) throws Exception {
        String domain = "rtmp://pili-publish.qnsdk.com";
        String hub = "sdk-live";
        String accesskey = TestConfig.testAccessKey;
        String secretkey = TestConfig.testSecretKey;

        long expire = System.currentTimeMillis() / 1000 + expiretime;
        String signStr = "/" + hub + "/" + streamKey + "?e=" + expire;
        byte[] sum = (new HMac()).HmacSHA1Encrypt(signStr, secretkey);
        String sign = UrlSafeBase64.encodeToString(sum);
        String token = accesskey + ":" + sign;
//        token = token.replaceAll("/", "_").replaceAll("\\+", "-");
        String path = domain + signStr + "&token=" + token;
        return path;
    }

    public static class HMac {
        private static final String MAC_NAME = "HmacSHA1";
        private static final String UTF8 = "UTF-8";

        public byte[] HmacSHA1Encrypt(String dataStr, String secretKeyStr) throws Exception {
            SecretKey secretKeySpec = new SecretKeySpec(secretKeyStr.getBytes(), MAC_NAME);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(MAC_NAME);
            //用给定密钥初始化 Mac 对象
            mac.init(secretKeySpec);

            //完成 Mac 操作
            return mac.doFinal(dataStr.getBytes());
        }
    }

}