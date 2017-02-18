package com.qiniu.storage.model;

/**
 * 封装了分片上传请求的回复内容
 */
public final class ResumeBlockInfo {
    public String ctx;
    public long crc32;
}
