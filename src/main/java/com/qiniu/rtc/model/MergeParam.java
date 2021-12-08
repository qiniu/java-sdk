// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;

/**
 * 合流业务请求参数
 */
@Data
public class MergeParam {
    private String id;
    private boolean audioOnly;
    private int width;
    private int height;
    private int fps;
    private int kbps;
    private int minRate;
    private int maxRate;
    private String stretchMode;
    private String publishUrl;
    private MergeBackGround background;
    private List<MergeWaterMarks> waterMarks;
    private boolean holdLastFrame;
    private String template;
    private List<MergeUserInfo> userInfos;


    /**
     * 合流背景参数
     */
    @Data
    public static class MergeBackGround {
        private String url;
        private int x;
        private int y;
        private int w;
        private int h;
        private String stretchMode;
    }

    @Data
    public static class MergeWaterMarks {
        private String url;
        private int x;
        private int y;
        private int w;
        private int h;
        private String stretchMode;
    }

    @Data
    public static class MergeUserInfo {
        private String userId;
        private String backgroundUrl;
        private String stretchMode;
        private int sequence;
    }


}
