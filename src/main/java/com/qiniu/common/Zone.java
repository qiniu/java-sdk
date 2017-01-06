package com.qiniu.common;

/**
 * 上传多区域
 */
public abstract class Zone {

    //    华东
    public static Zone zone0() {
        return new FixedZone("http://up.qiniu.com", "http://upload.qiniu.com",
                "", "http://rs.qbox.me", "http://rsf.qbox.me", "http://iovip.qbox.me",
                "https://up.qbox.me", "http://api.qiniu.com");
    }

    //    华北
    public static Zone zone1() {
        return new FixedZone("http://up-z1.qiniu.com", "http://upload-z1.qiniu.com",
                "", "http://rs-z1.qbox.me", "http://rsf-z1.qbox.me", "http://iovip-z1.qbox.me",
                "https://up-z1.qbox.me", "http://api-z1.qiniu.com");
    }

    //    华南
    public static Zone zone2() {
        return new FixedZone("http://up-z2.qiniu.com", "http://upload-z2.qiniu.com",
                "", "http://rs-z2.qbox.me", "http://rsf-z2.qbox.me", "http://iovip-z2.qbox.me",
                "https://up-z2.qbox.me", "http://api-z2.qiniu.com");
    }

    //    北美
    public static Zone zoneNa0() {
        return new FixedZone("http://up-na0.qiniu.com", "http://upload-na0.qiniu.com",
                "", "http://rs-na0.qbox.me", "http://rsf-na0.qbox.me", "http://iovip-na0.qbox.me",
                "https://up-na0.qbox.me", "http://api-na0.qiniu.com");
    }

    public static Zone autoZone() {
        return AutoZone.instance;
    }

    protected static String upHostFromPolicy(String token) {
        return null;
    }

    public String upHost(String token) {
        throw new UnsupportedOperationException();
    }

    public String upHostBackup(String token) {
        throw new UnsupportedOperationException();
    }

    public String upIpBackup(String token) {
        throw new UnsupportedOperationException();
    }

    public String upHostHttps(String token) {
        throw new UnsupportedOperationException();
    }

    public String rsHost(String ak, String bucket) {
        throw new UnsupportedOperationException();
    }

    public String rsHost() {
        return rsHost("", "");
    }

    public String rsfHost(String ak, String bucket) {
        throw new UnsupportedOperationException();
    }

    public String rsfHost() {
        return rsfHost("", "");
    }

    public String ioHost(String ak, String bucket) {
        throw new UnsupportedOperationException();
    }

    public String ioHost() {
        return ioHost("", "");
    }

    public String apiHost(String ak, String bucket) {
        throw new UnsupportedOperationException();
    }

    public String apiHost() {
        return apiHost("", "");
    }
}
