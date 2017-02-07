package com.qiniu;


import com.qiniu.util.Auth;

public final class TestConfig {
    //dummy ak & sk
    public static final String dummyAccessKey = "abcdefghklmnopq";
    public static final String dummySecretKey = "1234567890";
    public static final Auth dummyAuth = Auth.create(dummyAccessKey, dummySecretKey);

    public static final String dummyBucket = "no-such-dummy-bucket";
    public static final String dummyKey = "no-such-dummy-key";
    public static final String dummyDomain = "dummy.qiniudn.com";

    public static final String dummyUptoken = "ak:token:putpolicy";
    public static final String dummyInvalidUptoken = "invalidtoken";


    //valid ak & sk
    public static final String testAccessKey = "QWYn5TFQsLLU1pL5MFEmX3s5DmHdUThav9WyOWOm";
    public static final String testSecretKey = "Bxckh6FA-Fbs9Yt3i3cbKVK22UPBmAOHJcL95pGz";
    public static final Auth testAuth = Auth.create(testAccessKey, testSecretKey);

    //z0
    public static final String testBucket_z0 = "javasdk";
    public static final String testKey_z0 = "java-duke.png";
    public static final String testDomain_z0 = "javasdk.qiniudn.com";

    //na0
    public static final String testBucket_na0 = "java-sdk-na0";
    public static final String testKey_na0 = "java-duke.png";
    public static final String testDomain_na0 = "okzjfj76f.bkt.gdipper.com";


    //code
    public static final int ERROR_CODE_BUCKET_NOT_EXIST = 631;
    public static final int ERROR_CODE_KEY_NOT_EXIST = 612;


    private TestConfig() {
    }

    public static boolean isTravis() {
        return "travis".equals(System.getenv("QINIU_TEST_ENV"));
    }
}
