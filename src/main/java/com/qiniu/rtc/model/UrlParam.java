// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlParam {
    /**
     * 应用ID
     */
    String appId;
    /**
     * 房间名称
     */
    String roomName;
    /**
     * 游标
     */
    int offset;
    /**
     * 数量
     */
    int limit;

    String jobId;
}
