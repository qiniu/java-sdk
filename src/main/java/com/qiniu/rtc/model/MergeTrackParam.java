// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;

@Data
public class MergeTrackParam {

    private List<MergeTrack> add;
    private List<MergeTrack> remove;

    @Data
    public static class MergeTrack {
        private String trackID;
        private int x;
        private int y;
        private int w;
        private int h;
        private String stretchMode;
        private boolean supportSei = false;
    }
}
