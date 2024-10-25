package com.qiniu.common;

import java.io.File;
import java.nio.charset.Charset;

/**
 * SDK相关配置常量
 */
public final class Constants {
    /**
     * 版本号
     */
    public static final String VERSION = "7.17.0";
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
    public static final int READ_TIMEOUT = 30;
    /**
     * 底层HTTP库所有的并发执行的请求数量
     */
    public static final int DISPATCHER_MAX_REQUESTS = 64;
    /**
     * 底层HTTP库对每个独立的Host进行并发请求的数量
     */
    public static final int DISPATCHER_MAX_REQUESTS_PER_HOST = 16;
    /**
     * 底层HTTP库中复用连接对象的最大空闲数量
     */
    public static final int CONNECTION_POOL_MAX_IDLE_COUNT = 32;
    /**
     * 底层HTTP库中复用连接对象的回收周期（单位分钟）
     */
    public static final int CONNECTION_POOL_MAX_IDLE_MINUTES = 5;

    public static final String CACHE_DIR = getCacheDir();

    private Constants() {
    }

    private static String getCacheDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null || tmpDir.isEmpty()) {
            return null;
        }

        String qiniuDir = tmpDir + "com.qiniu.java-sdk";
        File dir = new File(qiniuDir);
        if (!dir.exists()) {
            return dir.mkdirs() ? qiniuDir : null;
        }

        if (dir.isDirectory()) {
            return qiniuDir;
        }

        return null;
    }
}

