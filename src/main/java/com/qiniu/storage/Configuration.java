package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Region;
import com.qiniu.common.RegionReqInfo;
import com.qiniu.http.Dns;
import com.qiniu.http.ProxyConfiguration;

/**
 * 该类封装了SDK相关配置参数
 */
public final class Configuration implements Cloneable {

    /**
     * 使用的Region
     */
    public Region region;

    /**
     * 空间相关上传管理操作是否使用 https , 默认 是
     */
    public boolean useHttpsDomains = true;
    /**
     * 空间相关上传管理操作是否使用vdn加速上传，默认 是
     */
    public boolean useAccUpHost = true;
    /**
     * 如果文件大小大于此值则使用断点上传, 否则使用Form上传
     */
    public long putThreshold = Constants.BLOCK_SIZE;
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
    public Dns dns;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    public boolean useDnsHostFirst;
    /**
     * 代理对象
     */
    public ProxyConfiguration proxy;

    /*
     * 特殊默认域名
     */
    @Deprecated
    public static String defaultRsHost = "rs.qiniu.com";
    @Deprecated
    public static String defaultApiHost = "api.qiniu.com";
    @Deprecated
    public static String defaultUcHost = "api.qiniu.com";

    public Configuration() {
    }

    public Configuration(Region region) {
        this.region = region;
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
        RegionReqInfo regionReqInfo = new RegionReqInfo(upToken);
        if (region == null) {
            region = Region.autoRegion();
        }
        return useAccUpHost ? getScheme() + region.getAccUpHost(regionReqInfo)
                : getScheme() + region.getSrcUpHost(regionReqInfo);
    }

    public String upHostBackup(String upToken) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(upToken);
        if (region == null) {
            region = Region.autoRegion();
        }
        return useAccUpHost ? getScheme() + region.getAccUpHostBackup(regionReqInfo)
                : getScheme() + region.getSrcUpHostBackup(regionReqInfo);
    }

    public String ioHost(String ak, String bucket) {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        if (region == null) {
            region = Region.autoRegion();
        }
        return getScheme() + region.getIovipHost(regionReqInfo);
    }

    public String apiHost(String ak, String bucket) {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        if (region == null) {
            region = Region.autoRegion();
        }
        return getScheme() + region.getApiHost(regionReqInfo);
    }

    public String rsHost(String ak, String bucket) {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        if (region == null) {
            region = Region.autoRegion();
        }
        return getScheme() + region.getRsHost(regionReqInfo);
    }

    public String rsfHost(String ak, String bucket) {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        if (region == null) {
            region = Region.autoRegion();
        }
        return getScheme() + region.getRsfHost(regionReqInfo);
    }

    public String rsHost() {
        return getScheme() + defaultRsHost;
    }

    public String apiHost() {
        return getScheme() + defaultApiHost;
    }

    public String ucHost() {
        return getScheme() + defaultUcHost;
    }

    String getScheme() {
        return useHttpsDomains ? "https://" : "http://";
    }

}
