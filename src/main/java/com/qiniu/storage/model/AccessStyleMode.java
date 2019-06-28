package com.qiniu.storage.model;

/**
 * 原图保护模式
 */
public enum AccessStyleMode {

    /**
     * 原图保护关闭
     */
    CLOSE(0),
    /**
     * 原图保护开启
     */
    OPEN(1);

    private int type = 0;

    AccessStyleMode(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }
}
