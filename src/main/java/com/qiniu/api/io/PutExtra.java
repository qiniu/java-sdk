package com.qiniu.api.io;

import java.util.Map;

public class PutExtra {
    /** 用户自定义参数，key必须以 "x:" 开头 */
    public Map<String, String> params;
    // 可选
    public String mimeType;

    public long crc32;

    public int checkCrc;
}

