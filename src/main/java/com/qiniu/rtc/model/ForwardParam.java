package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 转推的参数
 */
@Data
public class ForwardParam {
    private String id;
    private String publishUrl;
    private String playerId;
    private List<Map<String, String>> tracks;

    public ForwardParam(String id, String publishUrl, String playerId) {
        this.id = id;
        this.publishUrl = publishUrl;
        this.playerId = playerId;
    }

    public ForwardParam() {
    }
}
