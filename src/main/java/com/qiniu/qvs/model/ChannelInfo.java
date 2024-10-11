package com.qiniu.qvs.model;

public class ChannelInfo {
    private String[] channels;
    private int start;
    private int end;

    public ChannelInfo(String[] channels, int start, int end) {
        this.channels = channels;
        this.start = start;
        this.end = end;
    }

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
