package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.common.ZoneReqInfo;
import com.qiniu.http.ProxyConfiguration;
import qiniu.happydns.DnsClient;

/**
 * 该类封装了SDK相关配置参数
 */
public final class Configuration implements Cloneable {

    /**
     * 使用的Zone
     */
    public Zone zone;
    /**
     * 空间相关上传管理操作是否使用 https , 默认否
     */
    public boolean useHttpsDomains = false;
    /**
     * 如果文件大小大于此值则使用断点上传, 否则使用Form上传
     */
    public int putThreshold = Constants.BLOCK_SIZE;
    /**
     * 连接超时时间 单位秒(默认10s)
     */
    public int connectTimeout = Constants.CONNECT_TIMEOUT;
    /**
     * 写超时时间 单位秒(默认 0 , 不超时)
     */
    public int writeTimeout = Constants.WRITE_TIMEOUT;
    /**
     * 回复超时时间 单位秒(默认30s)
     */
    public int readTimeout = Constants.READ_TIMEOUT;
    /**
     * 底层HTTP库所有的并发执行的请求数量
     */
    public int dispatcherMaxRequests = Constants.DISPATCHER_MAX_REQUESTS;
    /**
     * 底层HTTP库对每个独立的Host进行并发请求的数量
     */
    public int dispatcherMaxRequestsPerHost = Constants.DISPATCHER_MAX_REQUESTS_PER_HOST;
    /**
     * 底层HTTP库中复用连接对象的最大空闲数量
     */
    public int connectionPoolMaxIdleCount = Constants.CONNECTION_POOL_MAX_IDLE_COUNT;
    /**
     * 底层HTTP库中复用连接对象的回收周期（单位分钟）
     */
    public int connectionPoolMaxIdleMinutes = Constants.CONNECTION_POOL_MAX_IDLE_MINUTES;
    /**
     * 上传失败重试次数
     */
    public int retryMax = 5;
    /**
     * 外部dns
     */
    public DnsClient dnsClient;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    public boolean useDnsHostFirst;
    /**
     * 代理对象
     */
    public ProxyConfiguration proxy;

    /**
     * 特殊默认域名
     */
    public static String defaultRsHost = "rs.qiniu.com";
    public static String defaultApiHost = "api.qiniu.com";

    public Configuration() {
        this.zone = null;
        this.dnsClient = null;
    }

    public Configuration(Zone zone) {
        this.zone = zone;
        this.dnsClient = null;
    }

    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String upHost(String upToken) throws QiniuException {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(upToken);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getUpHttps(zoneReqInfo)
                : zone.getUpHttp(zoneReqInfo);
    }

    public String upHostBackup(String upToken) throws QiniuException {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(upToken);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getUpBackupHttps(zoneReqInfo)
                : zone.getUpBackupHttp(zoneReqInfo);
    }


    public String ioHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getIovipHttps(zoneReqInfo)
                : zone.getIovipHttp(zoneReqInfo);
    }

    public String apiHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }

        return useHttpsDomains ? zone.getApiHttps(zoneReqInfo)
                : zone.getApiHttp(zoneReqInfo);
    }

    public String rsHost() {
        String scheme = "http://";
        if (useHttpsDomains) {
            scheme = "https://";
        }
        return scheme + defaultRsHost;
    }

    public String apiHost() {
        String scheme = "http://";
        if (useHttpsDomains) {
            scheme = "https://";
        }
        return scheme + defaultApiHost;
    }


    public String rsHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getRsHttps(zoneReqInfo)
                : zone.getRsHttp(zoneReqInfo);
    }


    public String rsfHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getRsfHttps(zoneReqInfo)
                : zone.getRsfHttp(zoneReqInfo);
    }
}
