// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;

@Data
public class MergeTrackParam {
    private int mode = ModeEnum.INCREMENT.getVal();
    private List<MergeTrack> add;
    private List<MergeTrack> remove;
    private List<MergeTrack> all;

    @Data
    public static class MergeTrack {
        private String trackID;
        private int x;
        private int y;
        private int z;
        private int w;
        private int h;

        /**
         * @see com.qiniu.rtc.model.StretchModeEnum
         */
        private String stretchMode;
        private boolean supportSei = false;
    }

    public enum ModeEnum {
        //增量，指定add和remove
        INCREMENT(0),
        //全量,替换替换原有数据
        FULL(1);

        private int val;

        ModeEnum(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }
}
