package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.common.ZoneReqInfo;
import com.qiniu.http.ProxyConfiguration;
import qiniu.happydns.DnsClient;

/**
 * Created by bailong on 16/9/21.
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
        return useHttpsDomains ? zone.getUpHttps(zoneReqInfo) :
                zone.getUpHttp(zoneReqInfo);
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
        return useHttpsDomains ? zone.getIovipHttps(zoneReqInfo) :
                zone.getIovipHttp(zoneReqInfo);
    }

    public String apiHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }

        return useHttpsDomains ? zone.getApiHttps(zoneReqInfo) :
                zone.getApiHttp(zoneReqInfo);
    }

    public String rsHost() {
        return useHttpsDomains ? "https://rs.qbox.me" : "http://rs.qiniu.com";
    }

    public String rsHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getRsHttps(zoneReqInfo) :
                zone.getRsHttp(zoneReqInfo);
    }


    public String rsfHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        return useHttpsDomains ? zone.getRsfHttps(zoneReqInfo) :
                zone.getRsfHttp(zoneReqInfo);
    }
}
