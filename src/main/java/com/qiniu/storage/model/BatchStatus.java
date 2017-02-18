package com.qiniu.storage.model;

/**
 * 定义批量请求的状态码
 * 参考文档：<a href="https://developer.qiniu.com/kodo/api/batch">批量操作</a>
 */
public final class BatchStatus {
    /**
     * 批量请求的每个命令的执行结果状态码
     */
    public int code;
    /**
     * 批量请求的每个命令返回的结果
     */
    public BatchOpData data;
}
