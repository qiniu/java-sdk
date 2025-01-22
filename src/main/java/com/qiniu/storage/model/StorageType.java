package com.qiniu.storage.model;

/**
 * 存储类型
 */
public enum StorageType {
    /**
     * 普通存储
     */
    COMMON,

    /**
     * 低频存储
     */
    INFREQUENCY,

    /**
     * 归档存储
     */
    Archive,

    /**
     * 深度归档存储
     */
    DeepArchive,

    /**
     * 归档直读存储
     */
    ArchiveIR,

    /**
     * 智能分层
     */
    IntelligentTiering,
}
