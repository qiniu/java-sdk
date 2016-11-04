package com.qiniu.common;

import java.nio.charset.Charset;

/**
 * Created by bailong on 16/9/14.
 */
public final class Constants {
    /**
     * 版本号
     */
    public static final String VERSION = "7.2.2";
    /**
     * 块大小，不能改变
     */
    public static final int BLOCK_SIZE = 4 * 1024 * 1024;
    /**
     * 所有都是UTF-8编码
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    /**
     * 连接超时时间 单位秒(默认10s)
     */
    public static final int CONNECT_TIMEOUT = 10;
    /**
     * 写超时时间 单位秒(默认 0 , 不超时)
     */
    public static final int WRITE_TIMEOUT = 0;
    /**
     * 回复超时时间 单位秒(默认30s)
     */
    public static final int RESPONSE_TIMEOUT = 30;

    private Constants() {
    }
}

