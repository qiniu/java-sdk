package com.qiniu.linking.model;

import com.google.gson.annotations.SerializedName;

public class SaveasReply {
    @SerializedName("frame")
    private String frame;
    @SerializedName("persistentId")
    private String persistentId;
    @SerializedName("duration")
    private int duration;

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public String getPersistentId() {
        return persistentId;
    }

    public void setPersistentId(String persistentId) {
        this.persistentId = persistentId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
