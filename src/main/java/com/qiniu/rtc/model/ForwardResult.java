package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class ForwardResult {
    /**
     * 单路转推状态
     */
    private String status;
    /**
     * 单路转推ID
     */
    private String id;
}
