package com.qiniu;


import com.qiniu.util.Auth;

public final class TestConfig {
    public static final Auth dummyAuth = Auth.create("abcdefghklmnopq", "1234567890");
    public static final Auth testAuth = Auth.create(
            "QWYn5TFQsLLU1pL5MFEmX3s5DmHdUThav9WyOWOm",
            "Bxckh6FA-Fbs9Yt3i3cbKVK22UPBmAOHJcL95pGz");
    public static final String bucket = "javasdk";
    public static final String key = "java-duke.svg";
    public static final String domain = "javasdk.qiniudn.com";

    private TestConfig() {
    }

    public static boolean isTravis() {
        return "travis".equals(System.getenv("QINIU_TEST_ENV"));
    }
}
