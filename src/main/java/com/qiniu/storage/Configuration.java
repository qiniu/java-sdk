package com.qiniu.storage;

import com.qiniu.common.*;
import com.qiniu.http.Dns;
import com.qiniu.http.ProxyConfiguration;
import com.qiniu.util.StringUtils;

import java.util.List;

/**
 * 该类封装了SDK相关配置参数
 */
public final class Configuration implements Cloneable {

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
    public boolean useAccUpHost = true;

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

    /**
     * 特殊默认域名
     */
    public static String defaultRsHost = "rs.qiniu.com";
    public static String defaultApiHost = "api.qiniu.com";
    public static String defaultUcHost = "uc.qbox.me";

    public Configuration() {

    }

    public Configuration(Region region) {
        this.region = region;
    }

    @Deprecated
    public Configuration(Zone zone) {
        this.zone = zone;
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
        return upHost(upToken, false);
    }

    public String upHost(String upToken, boolean changeHost) throws QiniuException {
        makeSureRegion();
        RegionReqInfo regionReqInfo = new RegionReqInfo(upToken);

        List<String> accHosts = region.getAccUpHost(regionReqInfo);
        List<String> srcHosts = region.getSrcUpHost(regionReqInfo);

        return getScheme() + getHelper().upHost(accHosts, srcHosts, changeHost);
    }

    @Deprecated
    public String upHostBackup(String upToken) throws QiniuException {
        return upHost(upToken, true);
    }

    public String ioHost(String ak, String bucket) {
        makeSureRegion();
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + region.getIovipHost(regionReqInfo);
    }

    public String apiHost(String ak, String bucket) {
        makeSureRegion();
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + region.getApiHost(regionReqInfo);
    }

    public String rsHost(String ak, String bucket) {
        makeSureRegion();
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + region.getRsHost(regionReqInfo);
    }

    public String rsfHost(String ak, String bucket) {
        makeSureRegion();
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
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

    private String getScheme() {
        return useHttpsDomains ? "https://" : "http://";
    }

    private void makeSureRegion() {
        if (region == null) {
            if (zone != null) {
                region = toRegion(zone);
            } else {
                region = Region.autoRegion();
            }
        }
    }

    private UpHostHelper helper;
    private UpHostHelper getHelper() {
        if (helper == null) {
            helper = new UpHostHelper(this, 60 * 15);
        }
        return helper;
    }



    /*
    * public Builder(Zone originZone) {
            this();
            zone.region = originZone.region;
            zone.upHttp = originZone.upHttp;
            zone.upHttps = originZone.upHttps;
            zone.upBackupHttp = originZone.upBackupHttp;
            zone.upBackupHttps = originZone.upBackupHttps;
            zone.upIpHttp = originZone.upIpHttp;
            zone.upIpHttps = originZone.upIpHttps;
            zone.iovipHttp = originZone.iovipHttp;
            zone.iovipHttps = originZone.iovipHttps;
            zone.rsHttp = originZone.rsHttp;
            zone.rsHttps = originZone.rsHttps;
            zone.rsfHttp = originZone.rsfHttp;
            zone.rsfHttps = originZone.rsfHttps;
            zone.apiHttp = originZone.apiHttp;
            zone.apiHttps = originZone.apiHttps;
        }

        return new Builder().region("z0").upHttp("http://up.qiniu.com").upHttps("https://up.qbox.me").
                upBackupHttp("http://upload.qiniu.com").upBackupHttps("https://upload.qbox.me").
                iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me").
                rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();

    *
    * public Builder(Region originRegion) {
            init();
            region.region = originRegion.region;
            region.srcUpHosts = originRegion.srcUpHosts;
            region.accUpHosts = originRegion.accUpHosts;
            region.iovipHost = originRegion.iovipHost;
            region.rsHost = originRegion.rsHost;
            region.rsfHost = originRegion.rsfHost;
            region.apiHost = originRegion.apiHost;
        }

        return new Builder().
                region("z0").
                srcUpHost("up.qiniup.com", "up-jjh.qiniup.com", "up-xs.qiniup.com").
                accUpHost("upload.qiniup.com", "upload-jjh.qiniup.com", "upload-xs.qiniup.com").
                iovipHost("iovip.qbox.me").
                rsHost("rs.qbox.me").
                rsfHost("rsf.qbox.me").
                apiHost("api.qiniu.com").
                build();
    * */

    private Region toRegion(Zone zone) {
        if (zone == null || zone instanceof AutoZone) {
            return Region.autoRegion();
        }
        // useAccUpHost default value is true
        // from the zone useAccUpHost must be true, (it is a new field)
        // true, acc map the upHttp, upHttps
        // false, src map to the backs
        // non autozone, zoneRegionInfo is useless
        return new Region.Builder()
                .region(zone.getRegion())
                .accUpHost(getHosts(zone.getUpHttps(null), zone.getUpHttp(null)))
                .srcUpHost(getHosts(zone.getUpBackupHttps(null), zone.getUpBackupHttp(null)))
                .iovipHost(getHost(zone.getIovipHttps(null), zone.getIovipHttp(null)))
                .rsHost(getHost(zone.getRsHttps(), zone.getRsHttp()))
                .rsfHost(getHost(zone.getRsfHttps(), zone.getRsfHttp()))
                .apiHost(getHost(zone.getApiHttps(), zone.getApiHttp()))
                .build();
    }


    private String getHost(String https, String http) {
        if (useHttpsDomains) {
            return https;
        } else {
            return http;
        }
    }

    private String[] getHosts(String https, String http) {
        if (useHttpsDomains) {
            // https would not be null
            return new String[]{toDomain(https)};
        } else {
            // http, s1 would not be null
            String s1 = toDomain(http);
            String s2 = toDomain(https);
            if (s2 != null && !s2.equalsIgnoreCase(s1)) {
                return new String[]{s1, s2};
            }
            return new String[]{s1};
        }
    }

    private String toDomain(String d1) {
        if (StringUtils.isNullOrEmpty(d1)) {
            return null;
        }
        int s = d1.indexOf("://");
        if (s > -1) {
            return d1.substring(s + 3);
        }
        return d1;
    }

}
