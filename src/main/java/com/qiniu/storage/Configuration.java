package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.Zone;
import com.qiniu.http.ProxyConfiguration;
import qiniu.happydns.DnsClient;

/**
 * Created by bailong on 16/9/21.
 */
public final class Configuration implements Cloneable {

    /**
     * 使用的Zone
     */
    public final Zone zone;
    /**
     * 上传是否使用 https , 默认否
     */
    public boolean uploadByHttps = false;
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
    public int responseTimeout = Constants.RESPONSE_TIMEOUT;
    /**
     * 上传失败重试次数
     */
    public int retryMax = 5;
    /**
     * 外部dns
     */
    public DnsClient dns = null;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    public boolean dnsHostFirst = false;
    /**
     * proxy
     */
    public ProxyConfiguration proxy = null;

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

}
