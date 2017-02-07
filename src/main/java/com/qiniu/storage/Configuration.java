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
    private Zone zone;
    /**
     * 空间相关上传管理操作是否使用 https , 默认否
     */
    private boolean useHttpsDomains = false;
    /**
     * 如果文件大小大于此值则使用断点上传, 否则使用Form上传
     */
    private int putThreshold = Constants.BLOCK_SIZE;
    /**
     * 连接超时时间 单位秒(默认10s)
     */
    private int connectTimeout = Constants.CONNECT_TIMEOUT;
    /**
     * 写超时时间 单位秒(默认 0 , 不超时)
     */
    private int writeTimeout = Constants.WRITE_TIMEOUT;
    /**
     * 回复超时时间 单位秒(默认30s)
     */
    private int readTimeout = Constants.READ_TIMEOUT;
    /**
     * 上传失败重试次数
     */
    private int retryMax = 5;
    /**
     * 外部dns
     */
    private DnsClient dnsClient;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    private boolean useDnsHostFirst;
    /**
     * 代理对象
     */
    private ProxyConfiguration proxy;

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
        if (useHttpsDomains) {
            return zone.getUpHttps(zoneReqInfo);
        } else {
            return zone.getUpHttp(zoneReqInfo);
        }
    }

    public String upHostBackup(String upToken) throws QiniuException {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(upToken);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        if (useHttpsDomains) {
            return zone.getUpBackupHttps(zoneReqInfo);
        } else {
            return zone.getUpBackupHttp(zoneReqInfo);
        }
    }


    public String ioHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        if (useHttpsDomains) {
            return zone.getIovipHttps(zoneReqInfo);
        } else {
            return zone.getIovipHttp(zoneReqInfo);
        }
    }

    public String apiHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        if (useHttpsDomains) {
            return zone.getApiHttps(zoneReqInfo);
        } else {
            return zone.getApiHttp(zoneReqInfo);
        }
    }

    public String rsHost() {
        if (useHttpsDomains) {
            return "https://rs.qbox.me";
        } else {
            return "http://rs.qiniu.com";
        }
    }

    public String rsHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        if (useHttpsDomains) {
            return zone.getRsHttps(zoneReqInfo);
        } else {
            return zone.getRsHttp(zoneReqInfo);
        }
    }

    public String rsfHost(String ak, String bucket) {
        ZoneReqInfo zoneReqInfo = new ZoneReqInfo(ak, bucket);
        if (zone == null) {
            zone = Zone.autoZone();
        }
        if (useHttpsDomains) {
            return zone.getRsfHttps(zoneReqInfo);
        } else {
            return zone.getRsfHttp(zoneReqInfo);
        }
    }

    public Zone getZone() {
        return zone;
    }

    public boolean isUseHttpsDomains() {
        return useHttpsDomains;
    }

    public void setUseHttpsDomains(boolean useHttpsDomains) {
        this.useHttpsDomains = useHttpsDomains;
    }

    public int getPutThreshold() {
        return putThreshold;
    }

    public void setPutThreshold(int putThreshold) {
        this.putThreshold = putThreshold;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getRetryMax() {
        return retryMax;
    }

    public void setRetryMax(int retryMax) {
        this.retryMax = retryMax;
    }

    public DnsClient getDnsClient() {
        return dnsClient;
    }

    public boolean isUseDnsHostFirst() {
        return useDnsHostFirst;
    }

    public void setUseDnsHostFirst(boolean useDnsHostFirst) {
        this.useDnsHostFirst = useDnsHostFirst;
    }

    public ProxyConfiguration getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
    }
}
