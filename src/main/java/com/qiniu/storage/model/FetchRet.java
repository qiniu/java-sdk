package com.qiniu.storage.model;

/**
 * fetch 接口的回复对象
 * 参考文档：<a href="https://developer.qiniu.com/kodo/api/fetch">资源抓取</a>
 */
public class FetchRet {
    /**
     * 抓取后保存到空间文件名
     */
    public String key;
    /**
     * 抓取后保存到空间的文件hash值
     */
    public String hash;
    /**
     * 抓取后保存到空间的文件的mimeType
     */
    public String mimeType;
    /**
     * 抓取后保存到空间的文件的大小，单位：字节
     */
    public long fsize;
}

