package com.qiniu.linking.model;


import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("device")
    private String deviceName;
    @SerializedName("loginAt")
    private long loginAt;
    @SerializedName("remoteIp")
    private String remoteIp;

    // -1 继承app配置
    // 0 遵循设备端配置
    // 1 强制持续上传
    // 2 强制关闭上传
    @SerializedName("uploadMode")
    private int uploadMode;

    // 0 不录制
    // -1 永久
    // -2 继承app配置
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
    // device type 0:normal type, 1:gateway
    @SerializedName("type")
    private int type;
    // max channel of gateway [1,64]
    @SerializedName("maxChannel")
    private int maxChannel;
    @SerializedName("channels")
    private Channel[] channels;
    @SerializedName("meta")
    private byte[] meta;

    // 0: 最大存多少天(default)
    // 1: 最大能占用多少内存
    @SerializedName("sdcardRotatePolicy")
    private int sdcardRotatePolicy;

    // 上面policy 对应的值(默认存7天)
    @SerializedName("sdcardRotateValue")
    private int sdcardRotateValue;

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

    public int getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(int uploadMode) {
        this.uploadMode = uploadMode;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxChannel() {
        return maxChannel;
    }

    public void setMaxChannel(int maxChannel) {
        this.maxChannel = maxChannel;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public void setChannels(Channel[] channels) {
        this.channels = channels;
    }

    public byte[] getMeta() {
        return meta;
    }

    public void setMeta(byte[] meta) {
        this.meta = meta;
    }

    public int getSdcardRotatePolicy() {
        return sdcardRotatePolicy;
    }

    public void setSdcardRotatePolicy(int sdcardRotatePolicy) {
        this.sdcardRotatePolicy = sdcardRotatePolicy;
    }

    public int getSdcardRotateValue() {
        return sdcardRotateValue;
    }

    public void setSdcardRotateValue(int sdcardRotateValue) {
        this.sdcardRotateValue = sdcardRotateValue;
    }
}

