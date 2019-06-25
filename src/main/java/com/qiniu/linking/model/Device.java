package com.qiniu.linking.model;


import com.google.gson.annotations.SerializedName;

public class Device {


    @SerializedName("device")
    private String deviceName;
    @SerializedName("loginAt")
    private long loginAt;
    @SerializedName("remoteIp")
    private String remoteIp;
    @SerializedName("uploadMode")
    private int uploadMode;
    @SerializedName("segmentExpireDays")
    private int segmentExpireDays;
    @SerializedName("state")
    private int state;
    @SerializedName("activedAt")
    private long activedAt;
    @SerializedName("createdAt")
    private long createdAt;
    @SerializedName("updatedAt")
    private long updatedAt;


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public long getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(long loginAt) {
        this.loginAt = loginAt;
    }

    public int getSegmentExpireDays() {
        return segmentExpireDays;
    }

    public void setSegmentExpireDays(int segmentExpireDays) {
        this.segmentExpireDays = segmentExpireDays;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getActivedAt() {
        return activedAt;
    }

    public void setActivedAt(long activedAt) {
        this.activedAt = activedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

}

