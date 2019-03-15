package com.qiniu.linking.model;

import com.google.gson.annotations.SerializedName;

public class Segment {
    @SerializedName("from")
    private int from;
    @SerializedName("to")
    private int to;
    @SerializedName("frame")
    private String frame;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }
}
