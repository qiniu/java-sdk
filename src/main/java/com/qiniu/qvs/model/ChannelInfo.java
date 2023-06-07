package com.qiniu.qvs.model;

public class ChannelInfo {
    private final String[] channels;
    private final String channelId;
    private final int start;
    private final int end;

    public ChannelInfo(String[] channels, String channelId, int start, int end) {
        this.channels = channels;
        this.channelId = channelId;
        this.start = start;
        this.end = end;
    }

    public String[] getChannels() {
        return channels;
    }

    public String getChannelId() {
        return channelId;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
