package com.qiniu.linking.model;

import com.google.gson.annotations.SerializedName;

public class DeviceKey {

    @SerializedName("accessKey")
    private String accessKey;
    @SerializedName("secretKey")
    private String secretKey;
    @SerializedName("state")
    private int state;
    @SerializedName("createdAt")
    private long createdAt;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
