package com.qiniu.common;

/**
 * 上传多区域
 */
public abstract class Zone {

    public static Zone zone0() {
        return new FixedZone("http://up.qiniu.com", "http://upload.qiniu.com",
                "", "http://rs.qbox.me", "http://rsf.qbox.me", "http://iovip.qbox.me",
                "https://up.qbox.me", "http://api.qiniu.com");
    }

    public static Zone zone1() {
        return new FixedZone("http://up-z1.qiniu.com", "http://upload-z1.qiniu.com",
                "", "http://rs-z1.qbox.me", "http://rsf-z1.qbox.me", "http://iovip-z1.qbox.me",
                "https://up-z1.qbox.me", "http://api-z1.qiniu.com");
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
