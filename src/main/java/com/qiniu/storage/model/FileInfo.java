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
     * 归档/深度归档存储文件的解冻状态，归档/深度归档文件冻结时，不返回该字段。
     * 1 表示解冻中
     * 2 表示解冻完成
     */
    public Integer restoreStatus;

    /**
     * 文件的mimeType
     */
    public String mimeType;

    /**
     * 文件上传时设置的endUser
     */
    public String endUser;

    /**
     * 文件的存储类型，
     * 0 表示普通存储
     * 1 表示低频存存储
     * 2 表示归档存储
     * 3 表示深度归档存储
     */
    public int type;

    /**
     * 文件的状态
     * 0 表示启用
     * 1 表示禁用
     */
    public int status;

    /**
     * 文件的md5值
     */
    public String md5;

    /**
     * 文件过期删除日期，int64 类型，Unix 时间戳格式，具体文件过期日期计算参考 生命周期管理。
     * 文件在设置过期时间后才会返回该字段（通过生命周期规则设置文件过期时间，仅对该功能发布后满足规则条件新上传文件返回该字段；
     * 历史文件想要返回该字段需要在功能发布后可通过 修改文件过期删除时间 API 或者 修改文件生命周期 API 指定过期时间；对于已
     * 经设置过过期时间的历史文件，到期都会正常过期删除，只是服务端没有该字段返回)
     *
     * 例如：值为1568736000的时间，表示文件会在2019/9/18当天内删除。
     */
    public Long expiration;


    /**
     * 文件生命周期中转为低频存储的日期，int64 类型，Unix 时间戳格式 ，具体日期计算参考 生命周期管理。
     * 文件在设置转低频后才会返回该字段（通过生命周期规则设置文件转低频，仅对该功能发布后满足规则条件新上传文件返回该字段；
     * 历史文件想要返回该字段需要在功能发布后可通过 修改文件生命周期 API 指定转低频时间；对于已经设置过转低频时间的历史文
     * 件，到期都会正常执行，只是服务端没有该字段返回)
     *
     * 例如：值为1568736000的时间，表示文件会在2019/9/18当天转为低频存储类型。
     */
    public Long transitionToIA;

    /**
     * 文件生命周期中转为归档存储的日期，int64 类型，Unix 时间戳格式 ，具体日期计算参考 生命周期管理。
     * 文件在设置转归档后才会返回该字段（通过生命周期规则设置文件转归档，仅对该功能发布后满足规则条件新上传文件返回该字段；
     * 历史文件想要返回该字段需要在功能发布后可通过 修改文件生命周期 API 指定转归档时间；对于已经设置过转归档时间的历史文
     * 件，到期都会正常执行，只是服务端没有该字段返回)
     *
     * 例如：值为1568736000的时间，表示文件会在2019/9/18当天转为归档存储类型。
     */
    @SerializedName("transitionToARCHIVE")
    public Long transitionToArchive;

    /**
     * 文件生命周期中转为深度归档存储的日期，int64 类型，Unix 时间戳格式 ，具体日期计算参考 生命周期管理。
     * 文件在设置转深度归档后才会返回该字段（通过生命周期规则设置文件转深度归档，仅对该功能发布后满足规则条件新上传文件返回该字段；
     * 历史文件想要返回该字段需要在功能发布后可通过 修改文件生命周期 API 指定转深度归档时间；对于已经设置过转深度归档时间的历史文
     * 件，到期都会正常执行，只是服务端没有该字段返回)
     *
     * 例如：值为1568736000的时间，表示文件会在2019/9/18当天转为深度归档存储类型。
     */
    public Long transitionToDeepArchive;

    /**
     * 自定义 meta data 数据
     */
    @SerializedName("x-qn-meta")
    public Map<String, Object> meta;
}
