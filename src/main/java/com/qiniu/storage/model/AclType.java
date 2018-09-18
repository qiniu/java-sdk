package com.qiniu.storage.model;

/**
 * 空间类型：公开空间、私有空间
 */
public enum AclType {
    /**
     * 公开空间
     */
    PUBLIC(0),
    /**
     * 私有空间
     */
    PRIVATE(1);

    private int type = 0;

    AclType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }
}
