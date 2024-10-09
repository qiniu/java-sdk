package com.qiniu.processing;

/**
 * 定义了持久化处理状态类
 * 参考文档：<a href="https://developer.qiniu.com/dora/api/persistent-processing-status-query-prefop">持久化处理状态查询</a>
 */
public class OperationStatus {
    /**
     * 持久化处理的进程ID，即 persistentId
     */
    public String id;

    /**
     * 状态码 0 成功，1 等待处理，2 正在处理，3 处理失败，4 通知提交失败
     */
    public int code;

    /**
     * 与状态码相对应的详细描述
     */
    public String desc;

    /**
     * 处理源文件的文件名
     */
    public String inputKey;

    /**
     * 处理源文件所在的空间名
     */
    public String inputBucket;

    /***
     * taskFrom
     * 1. 如果没有 taskFrom, 则表示是通过 api+fops命令 提交的任务, 否则见 2
     * 2. taskFrom 字段规则: <source>: <source_id>，其中 source 当前可选: workflow|trigger
     **/
    public String taskFrom;

    /**
     * 云处理操作的处理队列
     */
    public String pipeline;

    /**
     * 云处理请求的请求id，主要用于七牛技术人员的问题排查
     */
    public String reqid;

    /**
     * 是否是闲时任务
     * 0：非闲时任务
     * 1：显示任务
     */
    public Integer type;

    /**
     * 任务创建时间
     */
    public String creationDate;

    /**
     * 云处理操作列表，包含每个云处理操作的状态信息
     */
    public OperationResult[] items;

    public class OperationResult {
        /**
         * 所执行的云处理操作命令fopN
         */
        public String cmd;
        /**
         * 所执行的云处理操作命令状态码
         */
        public int code;
        /**
         * 所执行的云处理操作命令状态描述
         */
        public String desc;
        /**
         * 如果处理失败，该字段会给出失败的详细原因
         */
        public String error;
        /**
         * 云处理结果保存在目标空间文件的hash值
         */
        public String hash;
        /**
         * 云处理结果保存在目标空间的文件名
         */
        public String key;

        /**
         * 云处理结果保存在目标空间的文件名列表
         */
        public String[] keys;

        /**
         * 默认为0。当用户执行saveas时，如果未加force且指定的bucket：key存在，则返回1 ，告诉用户返回的是旧数据
         */
        public int returnOld;
    }
}
