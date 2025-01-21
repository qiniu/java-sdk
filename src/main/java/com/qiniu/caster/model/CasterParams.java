package com.qiniu.caster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasterParams {
    private String staticKey;
    private Map<String, Monitor> monitors;
    private Canvas canvas;
    private PvwOutput pvwOutput;
    private PgmOutput pgmOutput;
    private Map<String, Layout> layouts;
    private Storage storage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Monitor {
        private String url;
        private int vol;
        private boolean muted;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Canvas {
        private String resolution;
        private int height;
        private int width;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PvwOutput {
        private int channel;
        private int layout;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PgmOutput {
        private String publish;
        private boolean closed;
        private int ab;
        private int vb;
        private int channel;
        private Overlay overlay;
        private Text text;
        private boolean emergencyMode;
        private int emergencyChannel;
        private int delay;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Layout {
        private String title;
        private Overlay overlay;
        private Text text;
        private int updateAt;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Storage {
        private String bucket;
        private String domain;

    }

}
