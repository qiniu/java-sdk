package com.qiniu.common;

import java.nio.charset.Charset;

/**
 * Created by bailong on 16/9/14.
 */
public interface Constants {
    String VERSION = "8.0.0";
    int BLOCK_SIZE = 4 * 1024 * 1024;
    Charset UTF_8 = Charset.forName("UTF-8");
    /**
     * 连接超时时间 单位秒(默认10s)
     */
    public int CONNECT_TIMEOUT = 10;
    /**
     * 写超时时间 单位秒(默认 0 , 不超时)
     */
    public int WRITE_TIMEOUT = 0;
    /**
     * 回复超时时间 单位秒(默认30s)
     */
    public int RESPONSE_TIMEOUT = 30;
}

