package com.qiniu.common;

import com.qiniu.http.ProxyConfiguration;
import qiniu.happydns.DnsClient;

import java.nio.charset.Charset;

// CHECKSTYLE:OFF

public final class Config {

    public static final String VERSION = "7.1.3";
    /**
     * 断点上传时的分块大小(默认的分块大小, 不允许改变)
     */
    public static final int BLOCK_SIZE = 4 * 1024 * 1024;

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    /**
     * 默认API服务器
     */
    public static String API_HOST = "http://api.qiniu.com";
    /**
     * 默认文件列表服务器
     */
    public static String RSF_HOST = "http://rsf.qbox.me";
    /**
     * 默认文件管理服务器
     */
    public static String RS_HOST = "http://rs.qbox.me";
    /**
     * 默认文件服务器
     */
    public static String IO_HOST = "http://iovip.qbox.me";

    /**
     * 获取上传地址服务器
     */
    public static String UC_HOST = "https://uc.qbox.me";

    /**
     * 使用的Zone, 若不指定,通过七牛服务自动判断
     */
    public static Zone zone = null;

    /**
     * 上传是否使用 https , 默认否
     */
    public static boolean UPLOAD_BY_HTTPS = false;

    /**
     * 如果文件大小大于此值则使用断点上传, 否则使用Form上传
     */
    public static int PUT_THRESHOLD = BLOCK_SIZE;
    /**
     * 连接超时时间 单位秒(默认10s)
     */
    public static int CONNECT_TIMEOUT = 10;
    /**
     * 写超时时间 单位秒(默认 0 , 不超时)
     */
    public static int WRITE_TIMEOUT = 0;
    /**
     * 回复超时时间 单位秒(默认30s)
     */
    public static int RESPONSE_TIMEOUT = 30;
    /**
     * 上传失败重试次数
     */
    public static int RETRY_MAX = 5;
    /**
     * 外部dns
     */
    public static DnsClient dns = null;
    /*
     * 解析域名时,优先使用host配置,主要针对内部局域网配置
     */
    public static boolean dnsHostFirst = false;
    /**
     * proxy
     */
    public static ProxyConfiguration proxy = null;

    private Config() {
    }

}
// CHECKSTYLE:ON
