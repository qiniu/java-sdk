package com.qiniu.storage.model;

/**
 * 该类封装了batch接口回复中的data部分
 * 参考文档：<a href="https://developer.qiniu.com/kodo/api/batch">批量操作</a>
 */
public class BatchOpData {
    //batch stat结果
    public long fsize;
    public String hash;
    public String mimeType;
    public long putTime;
    //batch stat, move, copy, delete, chgm 遇到错误时才有值
    public String error;
}
