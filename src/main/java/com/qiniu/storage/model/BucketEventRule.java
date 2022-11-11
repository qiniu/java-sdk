package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

/**
 * BucketEnventRule 定义了存储空间发生事件时候的通知规则<br>
 * 比如调用了存储的"delete"删除接口删除文件， 这个是一个事件；<br>
 * 当这个事件发生的时候， 我们要对哪些文件，做什么处理，是否要作回调，<br>
 * 都可以通过这个结构体配置<br>
 * <br>
 * 注意：事件通知功能会根据文件的前缀和后缀依次匹配，只对第一个匹配成功的事件发送通知。
 */
public class BucketEventRule {

    /**
     * 规则名称， 在设置的bucket中规则名称需要是唯一的
     */
    @SerializedName("name")
    String name;

    /**
     * 可选，以该前缀开头的文件应用此规则
     */
    @SerializedName("prefix")
    String prefix;

    /**
     * 可选，以该后缀结尾的文件应用此规则
     */
    @SerializedName("suffix")
    String suffix;

    /**
     * 事件类型，可以指定多个<br>
     * 包括 put,mkfile,delete,copy,move,append,disable,enable,deleteMarkerCreate
     */
    @SerializedName("events")
    String[] events;

    /**
     * 通知URL，可以指定多个，失败依次重试
     */
    @SerializedName("callback_urls")
    String[] callbackUrls;

    /**
     * 可选，设置的话会对通知请求用对应的ak、sk进行签名
     */
    @SerializedName("access_key")
    String accessKey;

    /**
     * 可选，通知请求的host
     */
    @SerializedName("host")
    String host;

    public BucketEventRule(String name, String[] events, String[] callbackUrls) {
        this.name = name;
        this.events = events;
        this.callbackUrls = callbackUrls;
    }

    /**
     * 获取规则名称，在设置的bucket中规则名称是唯一的
     *
     * @return 规则名称
     */
    public String getName() {
        return name;
    }

    /**
     * 规则名称，在设置的bucket中规则名称需要是唯一的
     *
     * @param name 规则名称
     * @return 规则信息
     */
    public BucketEventRule setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 获取前缀，以该前缀开头的文件应用此规则
     *
     * @return 前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置前缀，以该前缀开头的文件应用此规则
     *
     * @param prefix 前缀
     * @return 规则信息
     */
    public BucketEventRule setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 获取后缀，以该后缀结尾的文件应用此规则
     *
     * @return 后缀
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * 设置后缀，以该后缀结尾的文件应用此规则
     *
     * @param suffix 后缀
     * @return 规则信息
     */
    public BucketEventRule setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * 获取事件类型，可以有多个
     *
     * @return 事件类型
     */
    public String[] getEvents() {
        return events;
    }

    /**
     * 设置事件类型，可以指定多个<br>
     * 包括 put,mkfile,delete,copy,move,append,disable,enable,deleteMarkerCreate
     *
     * @param events 事件类型
     * @return 规则信息
     */
    public BucketEventRule setEvents(String[] events) {
        this.events = events;
        return this;
    }

    /**
     * 获取通知 URL，可以有多个
     *
     * @return 通知 URL
     */
    public String[] getCallbackUrls() {
        return callbackUrls;
    }

    /**
     * 设置通知 URL，可以指定多个，失败依次重试
     *
     * @param callbackUrls 通知 URL
     * @return 规则信息
     */
    public BucketEventRule setCallbackUrls(String[] callbackUrls) {
        this.callbackUrls = callbackUrls;
        return this;
    }

    /**
     * 获取为了签名设置好的 accessKey
     *
     * @return accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * 可选，设置的话会对通知请求用对应的ak、sk进行签名
     *
     * @param accessKey accessKey
     * @return 规则信息
     */
    public BucketEventRule setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    /**
     * 获取通知请求的 host
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * 可选，通知请求的host
     *
     * @param host host
     * @return 规则信息
     */
    public BucketEventRule setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * 编码成 query 参数格式
     *
     * @return query
     */
    public String asQueryString() {
        String query = String.format("name=%s&prefix=%s&suffix=%s&%s&%s",
                null == name ? "" : name,
                null == prefix ? "" : prefix,
                null == suffix ? "" : suffix,
                asQueryString(events, "event"),
                asQueryString(callbackUrls, "callbackURL")
        );
        if (null != accessKey) {
            query += "&access_key=" + accessKey;
        }
        if (null != host) {
            query += "&host=" + host;
        }
        return query;
    }

    private String asQueryString(String[] array, String key) {
        if (null == array) return "";
        else {
            String query = new String();
            for (String s : array) {
                if (!query.isEmpty()) query += "&";
                query += key + "=" + s;
            }
            return query;
        }
    }

}
