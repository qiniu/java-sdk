package com.qiniu.common;

/**
 * Created by bailong on 16/9/15.
 */
public final class FixedZone extends Zone {
    /**
     * 默认上传服务器
     */
    private final String upHost;
    /**
     * 备用上传服务器，当默认服务器网络链接失败时使用
     */
    private final String upHostBackup;

    private final String upHostHttps;

    private final String upHttpIp;

    private final String rsHost;

    private final String rsfHost;

    private final String ioHost;

    private final String apiHost;


    public FixedZone(String upHost, String upHostBackup, String upHttpIp,
                     String rsHost, String rsfHost, String ioHost, String upHostHttps, String apiHost) {
        this.upHost = upHost;
        this.upHostBackup = upHostBackup;
        this.upHttpIp = upHttpIp;
        this.rsHost = rsHost;
        this.rsfHost = rsfHost;
        this.ioHost = ioHost;
        this.upHostHttps = upHostHttps;
        this.apiHost = apiHost;
    }

    public String upHost(String token) {
        return upHost;
    }

    public String upHostBackup(String token) {
        return upHostBackup;
    }

    public String upIpBackup(String token) {
        return upHttpIp;
    }

    public String upHostHttps(String token) {
        return upHostHttps;
    }

    public String rsHost(String ak, String bucket) {
        return rsHost;
    }

    public String rsfHost(String ak, String bucket) {
        return rsfHost;
    }

    public String ioHost(String ak, String bucket) {
        return ioHost;
    }

    public String apiHost(String ak, String bucket) {
        return apiHost;
    }
}
