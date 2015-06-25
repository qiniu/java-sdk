package com.qiniu.common;

/**
 * 上传多区域
 */
public final class Zone {
    /**
     * 默认上传服务器
     */
    public final String upHost;
    /**
     * 备用上传服务器，当默认服务器网络链接失败时使用
     */
    public final String upHostBackup;

    public Zone(String upHost, String upHostBackup) {
        this.upHost = upHost;
        this.upHostBackup = upHostBackup;
    }

    public static Zone zone0() {
        return new Zone("http://up.qiniu.com", "http://upload.qiniu.com");
    }

    public static Zone zone1() {
        return new Zone("http://up-z1.qiniu.com", "http://upload-z1.qiniu.com");
    }
}
