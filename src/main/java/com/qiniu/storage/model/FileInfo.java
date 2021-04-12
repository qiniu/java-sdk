package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * list 接口的回复文件对象信息
 * 参考文档：<a href="https://developer.qiniu.com/kodo/api/list">资源列举</a>
 */
public final class FileInfo {
    /**
     * 文件名
     */
    public String key;
    /**
     * 文件hash值
     */
    public String hash;
    /**
     * 文件大小，单位：字节
     */
    public long fsize;
    /**
     * 文件上传时间，单位为：100纳秒
     */
    public long putTime;
    /**
     * 文件的mimeType
     */
    public String mimeType;
    /**
     * 文件上传时设置的endUser
     */
    public String endUser;
    /**
     * 文件的存储类型，0为普通存储，1为低频存储
     */
    public int type;
    /**
     * 文件的状态，0表示启用，1表示禁用
     */
    public int status;
    /**
     * 文件的md5值
     */
    public String md5;

    /**
     * 自定义 meta data 数据
     */
    @SerializedName("x-qn-meta")
    public Map<String, Object> meta;
}
