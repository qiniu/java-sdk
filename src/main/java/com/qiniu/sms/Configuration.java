package com.qiniu.sms;

import com.qiniu.common.Constants;

public class Configuration implements Cloneable{
	
	public boolean useHttpsDomains = false;
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
    
    /*
     * 特殊默认域名
     */
    public static String defaultSmsHost = "sms.qiniuapi.com";
    
    public Configuration() {
    }
    
    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String smsHost() {
        return getScheme() + defaultSmsHost;
    }
    
    String getScheme() {
        return useHttpsDomains ? "https://" : "http://";
    }

}
