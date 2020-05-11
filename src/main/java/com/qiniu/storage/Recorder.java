package com.qiniu.storage;

import java.io.File;

/**
 * 定义分片上传时纪录上传进度的接口
 */
public interface Recorder {

    /**
     * 新建或更新文件分片上传的进度
     *
     * @param key  持久化的键
     * @param data 持久化的内容
     */
    void set(String key, byte[] data);

    /**
     * 获取文件分片上传的进度信息
     *
     * @param key 持久化的键
     * @return 对应的信息
     */
    byte[] get(String key);

    /**
     * 删除文件分片上传的进度文件
     *
     * @param key 持久化的键
     */
    void del(String key);

    /**
     * 根据服务器的key和本地文件名生成持久化纪录的key
     *
     * @param key  服务器的key
     * @param file 本地文件名
     * @return 持久化上传纪录的key
     */
    String recorderKeyGenerate(String key, File file);

    /**
     * 根据目标bucket, key和本地文件名生成持久化纪录的key
     *
     * @param bucket          空间名或其变换的值
     * @param key             文件名或其变换的值
     * @param contentDataSUID 上传内容的标识字符串
     * @param uploaderSUID    上传方式的标识字符串
     * @return 持久化上传纪录的key
     */
    String recorderKeyGenerate(String bucket, String key, String contentDataSUID, String uploaderSUID);
}
