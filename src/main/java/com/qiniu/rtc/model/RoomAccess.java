package com.qiniu.rtc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jemy on 2018/4/18.
 */
public class RoomAccess {
    @SerializedName("appId")
    private String appId;
    @SerializedName("roomName")
    private String roomName;
    @SerializedName("userId")
    private String userId;
    @SerializedName("expireAt")
    private long expireAt;
    @SerializedName("permission")
    private String permission;

    public RoomAccess(String appId, String roomName, String userId, long expireAt, String permission) {
        this.appId = appId;
        this.roomName = roomName;
        this.userId = userId;
        this.expireAt = expireAt;
        this.permission = permission;
    }
}
