package com.qiniu.streaming.model;

/**
 * Created by bailong on 16/9/22.
 */
public final class StreamStatus {
    public long startAt;
    public String clientIP;
    public long bps;

    public Fps fps;

    public static class Fps {
        public int audio;
        public int video;
        public int data;
    }
}
