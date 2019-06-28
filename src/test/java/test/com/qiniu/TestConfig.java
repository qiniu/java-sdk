package test.com.qiniu;

import com.qiniu.util.Auth;

public final class TestConfig {

    //dummy: ak, sk, ...
    public static final String dummyAccessKey = "abcdefghklmnopq";
    public static final String dummySecretKey = "1234567890";
    public static final Auth dummyAuth = Auth.create(dummyAccessKey, dummySecretKey);
    public static final String dummyBucket = "no-such-dummy-bucket";
    public static final String dummyKey = "no-such-dummy-key";
    public static final String dummyDomain = "dummy.qiniudn.com";
    public static final String dummyUptoken = "ak:token:putpolicy";
    public static final String dummyInvalidUptoken = "invalidtoken";

    //test: ak, sk, auth
    public static final String testAccessKey = System.getenv("QINIU_ACCESS_KEY");
    public static final String testSecretKey = System.getenv("QINIU_SECRET_KEY");
    //sms: ak, sk, auth
    public static final String smsAccessKey = "test";
    public static final String smsSecretKey = "test";
    //z0
    public static final String testBucket_z0 = "javasdk";
    public static final String testKey_z0 = "do_not_delete/1.png";
    public static final String testDomain_z0 = "javasdk.peterpy.cn";
    public static final String testUrl_z0 = "http://" + testDomain_z0 + "/" + testKey_z0;
    public static final String testDomain_z0_timeStamp = "javasdk-timestamp.peterpy.cn";
    public static final String testUrl_z0_timeStamp = "http://" + testDomain_z0_timeStamp + "/" + testKey_z0;
    public static final String testMp4FileKey = "do_not_delete/1.mp4";
    public static final String testPipeline = "sdktest";
    //na0
    public static final String testBucket_na0 = "java-sdk-na0";
    public static final String testKey_na0 = "do_not_delete/1.png";
    public static final String testDomain_na0 = "javasdk-na0.peterpy.cn";
    public static final String testUrl_na0 = "http://" + testDomain_na0 + "/" + testKey_na0;
    //sg
    public static final String testBucket_as0 = "sdk-as0";
    //code
    public static final int ERROR_CODE_BUCKET_NOT_EXIST = 631;
    public static final int ERROR_CODE_KEY_NOT_EXIST = 612;
    public static final String testLinkingAppid = System.getenv("QINIU_LINKING_APPID");
    public static Auth testAuth = Auth.create(testAccessKey, testSecretKey);

    private TestConfig() {
    }

    public static boolean isTravis() {
        return "travis".equals(System.getenv("QINIU_TEST_ENV"));
    }

}
