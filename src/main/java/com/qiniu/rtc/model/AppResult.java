// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class AppResult {
    private String appId;
    private String hub;
    private String title;
    private int maxUsers;
    private boolean noAutoKickUser;
    private String createdAt;
    private String updatedAt;
    private String error;
    private String status;

    @Data
    public static class AppMergeInfo {
        private boolean enable;
        private boolean audioOnly;
        private int height;
        private int width;
        private int fps;
        private int kbps;
        private String url;
        private String streamTitle;
    }
}
