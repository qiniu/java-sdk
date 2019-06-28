package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

/**
 * BucketLifeCycleRule 定义了关于七牛存储空间关于生命周期的一些配置，规则。<br>
 * 比如存储空间中文件可以设置多少天后删除，多少天后转低频存储等等<br>
 * <br>
 * 规则按最长前缀匹配，前缀规则优先于全局规则匹配，每个上传的文件最多匹配一个规则<br>
 */
public class BucketLifeCycleRule {

    /**
     * 规则名称， 在设置的bucket中规则名称需要是唯一的<br>
     * 同时长度小于50， 不能为空<br>
     * 由字母，数字和下划线组成
     */
    @SerializedName("name")
    String name;

    /**
     * 以该前缀开头的文件应用此规则，同一个 bucket 里面前缀不能重复，规则按最长前缀匹配
     */
    @SerializedName("prefix")
    String prefix;

    /**
     * 指定存储空间内的文件多少天后删除<br>
     * 0 - 不删除<br>
     * > 0 表示多少天后删除
     */
    @SerializedName("delete_after_days")
    int deleteAfterDays;

    /**
     * 在多少天后转低频存储<br>
     * 0  - 表示不转低频<br>
     * < 0 表示上传的文件立即使用低频存储<br>
     * > 0 表示转低频的天数
     */
    @SerializedName("to_line_after_days")
    int toLineAfterDays;

    public BucketLifeCycleRule(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    /**
     * 获得规则名称，在设置的bucket中规则名称是唯一的
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * 规则名称， 在设置的bucket中规则名称需要是唯一的<br>
     * 同时长度小于50， 不能为空<br>
     * 由字母，数字和下划线组成
     *
     * @param name
     * @return
     */
    public BucketLifeCycleRule setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 获得前缀
     *
     * @return
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * 以该前缀开头的文件应用此规则<br>
     * 同一个 bucket 里面前缀不能重复，规则按最长前缀匹配
     *
     * @param prefix
     * @return
     */
    public BucketLifeCycleRule setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 获得 指定多少天后删除存储空间内的文件
     *
     * @return
     */
    public int getDeleteAfterDays() {
        return this.deleteAfterDays;
    }

    /**
     * 指定存储空间内的文件多少天后删除<br>
     * 0 - 不删除<br>
     * > 0 表示多少天后删除
     *
     * @param deleteAfterDays
     * @return
     */
    public BucketLifeCycleRule setDeleteAfterDays(int deleteAfterDays) {
        this.deleteAfterDays = deleteAfterDays;
        return this;
    }

    /**
     * 获得在多少天后转低频存储
     *
     * @return
     */
    public int getToLineAfterDays() {
        return this.toLineAfterDays;
    }

    /**
     * 在多少天后转低频存储<br>
     * 0  - 表示不转低频<br>
     * < 0 表示上传的文件立即使用低频存储<br>
     * > 0 表示转低频的天数
     *
     * @param toLineAfterDays
     * @return
     */
    public BucketLifeCycleRule setToLineAfterDays(int toLineAfterDays) {
        this.toLineAfterDays = toLineAfterDays;
        return this;
    }

    /**
     * 编码成query参数格式
     */
    public String asQueryString() {
        return String.format("name=%s&prefix=%s&delete_after_days=%d&to_line_after_days=%d",
                null == name ? "" : name,
                null == prefix ? "" : prefix,
                deleteAfterDays,
                toLineAfterDays
        );
    }

}
