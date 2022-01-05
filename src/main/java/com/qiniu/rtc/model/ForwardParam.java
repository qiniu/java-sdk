package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;

/**
 * 转推的参数
 */
@Data
public class ForwardParam {
    private String id;
    private String publishUrl;
    private String playerId;
    private List<TrackInfo> tracks;

    public ForwardParam(String id, String publishUrl, String playerId) {
        this.id = id;
        this.publishUrl = publishUrl;
        this.playerId = playerId;
    }

    @Data
    public static class TrackInfo {
        private String trackId;

        public TrackInfo(String trackId) {
            this.trackId = trackId;
        }

        public TrackInfo() {
        }
    }

    public ForwardParam() {
    }
}
