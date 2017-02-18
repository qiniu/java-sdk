package com.qiniu.storage.model;

import com.qiniu.util.StringUtils;

/**
 * 该类封装了文件列举请求回复
 */
public final class FileListing {
    /**
     * 文件对象列表
     */
    public FileInfo[] items;
    /**
     * 下一次列举的marker
     */
    public String marker;
    /**
     * 通用前缀
     */
    public String[] commonPrefixes;

    /**
     * 列举操作是否已到所有文件列表结尾，如果为true表示无需再发送列举请求
     */
    public boolean isEOF() {
        return StringUtils.isNullOrEmpty(marker);
    }
}
