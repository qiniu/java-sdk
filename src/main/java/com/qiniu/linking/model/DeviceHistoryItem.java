package com.qiniu.linking.model;

import com.google.gson.annotations.SerializedName;

public class DeviceHistoryItem {

    @SerializedName("loginAt")
    private long loginAt;
    @SerializedName("logoutAt")
    private long logoutAt;
    @SerializedName("remoteIp")
    private String remoteIp;
    @SerializedName("logoutReason")
    private String logoutReason;

    public long getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(long loginAt) {
        this.loginAt = loginAt;
    }

    public long getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(long logoutAt) {
        this.logoutAt = logoutAt;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getLogoutReason() {
        return logoutReason;
    }

    public void setLogoutReason(String logoutReason) {
        this.logoutReason = logoutReason;
    }
}
