package com.qiniu.storage;


import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Dns;
import com.qiniu.http.ProxyConfiguration;

/**
 * 该类封装了SDK相关配置参数
 */
public final class Configuration implements Cloneable {

    /**
     * 特殊默认域名
     */
    public static String defaultRsHost = "rs.qiniu.com";
    public static String defaultApiHost = "api.qiniu.com";
    public static String defaultUcHost = "uc.qbox.me";
    /**
     * 使用的Region
     */
    public Region region;
    /**
     * 使用的Zone
     */
    @Deprecated
    public Zone zone;
    /**
     * 空间相关上传管理操作是否使用 https , 默认 是
     */
    public boolean useHttpsDomains = true;
    /**
     * 空间相关上传管理操作是否使用代理加速上传，默认 是
     */
    public boolean accUpHostFirst = true;
    /**
     * 使用 AutoRegion 时，如果从区域信息得到上传 host 失败，使用默认的上传域名上传，默认 是
     * upload.qiniup.com, upload-z1.qiniup.com, upload-z2.qiniup.com,
     * upload-na0.qiniup.com, upload-as0.qiniup.com
     */
    public boolean useDefaultUpHostIfNone = true;
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
    public Dns dns;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    @Deprecated
    public boolean useDnsHostFirst;
    /**
     * 代理对象
     */
    public ProxyConfiguration proxy;
    private ConfigHelper configHelper;

    public Configuration() {
        configHelper = new ConfigHelper(this);
    }

    public Configuration(Region region) {
        this.region = region;
        configHelper = new ConfigHelper(this);
    }

    @Deprecated
    public Configuration(Zone zone) {
        this.zone = zone;
        configHelper = new ConfigHelper(this);
    }

    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Deprecated
    public String upHost(String upToken) throws QiniuException {
        return configHelper.upHost(upToken);
    }


    @Deprecated
    public String upHostBackup(String upToken) throws QiniuException {
        return configHelper.tryChangeUpHost(upToken, null);
    }

    @Deprecated
    public String ioHost(String ak, String bucket) {
        try {
            return configHelper.ioHost(ak, bucket);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String apiHost(String ak, String bucket) {
        try {
            return configHelper.apiHost(ak, bucket);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String rsHost(String ak, String bucket) {
        try {
            return configHelper.rsHost(ak, bucket);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String rsfHost(String ak, String bucket) {
        try {
            return configHelper.rsfHost(ak, bucket);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String rsHost() {
        return configHelper.rsHost();
    }

    @Deprecated
    public String apiHost() {
        return configHelper.apiHost();
    }

    @Deprecated
    public String ucHost() {
        return configHelper.ucHost();
    }

}
