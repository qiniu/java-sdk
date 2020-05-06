package com.qiniu.qvs.model;

import com.google.gson.annotations.SerializedName;

public class NameSpace {

    @SerializedName("ID")
    private String ID;
    @SerializedName("Name")
    private String Name; // 空间名称(格式"^[a-zA-Z0-9_-]{1,100}$")
    @SerializedName("Desc")
    private String Desc;  // 空间描述
    @SerializedName("AccessType")
    private String AccessType;  // 接入类型"gb28181"或者“rtmp”
    @SerializedName("RTMPURLType")
    private int RTMPURLType;    // accessType为“rtmp”时，推拉流地址计算方式，1:static, 2:dynamic
    @SerializedName("Domains")
    private String[] Domains;   // 直播域名
    @SerializedName("DomainDetails")
    private DomainInfo[] DomainDetails;
    @SerializedName("Callback")
    private String Callback;   // 后台服务器回调URL
    @SerializedName("Disabled")
    private boolean Disabled;   // 流是否被启用, false:启用,true:禁用
    @SerializedName("RecordTemplateId")
    private String RecordTemplateId; // 录制模版id
    @SerializedName("SnapShotTemplateId")
    private String SnapShotTemplateId; // 截图模版id
    @SerializedName("RecordTemplateApplyAll")
    private boolean RecordTemplateApplyAll;  // 空间模版是否应用到全局
    @SerializedName("SnapTemplateApplyAll")
    private boolean SnapTemplateApplyAll; // 截图模版是否应用到全局
    @SerializedName("CreatedAt")
    private int CreatedAt;// 空间创建时间
    @SerializedName("UpdatedAt")
    private int UpdatedAt; // 空间更新时间
    @SerializedName("DevicesCount")
    private int DevicesCount;  // 设备数量
    @SerializedName("StreamCount")
    private int StreamCount;  // 流数量
    @SerializedName("OnlineStreamCount")
    private int OnlineStreamCount;  // 在线流数量
    @SerializedName("DisabledStreamCount")
    private int DisabledStreamCount;  // 禁用流数量

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getAccessType() {
        return AccessType;
    }

    public void setAccessType(String accessType) {
        AccessType = accessType;
    }

    public int getRTMPURLType() {
        return RTMPURLType;
    }

    public void setRTMPURLType(int RTMPURLType) {
        this.RTMPURLType = RTMPURLType;
    }

    public String[] getDomains() {
        return Domains;
    }

    public void setDomains(String[] domains) {
        Domains = domains;
    }

    public DomainInfo[] getDomainDetails() {
        return DomainDetails;
    }

    public void setDomainDetails(DomainInfo[] domainDetails) {
        DomainDetails = domainDetails;
    }

    public String getCallback() {
        return Callback;
    }

    public void setCallback(String callback) {
        Callback = callback;
    }

    public boolean isDisabled() {
        return Disabled;
    }

    public void setDisabled(boolean disabled) {
        Disabled = disabled;
    }

    public String getRecordTemplateId() {
        return RecordTemplateId;
    }

    public void setRecordTemplateId(String recordTemplateId) {
        RecordTemplateId = recordTemplateId;
    }

    public String getSnapShotTemplateId() {
        return SnapShotTemplateId;
    }

    public void setSnapShotTemplateId(String snapShotTemplateId) {
        SnapShotTemplateId = snapShotTemplateId;
    }

    public boolean isRecordTemplateApplyAll() {
        return RecordTemplateApplyAll;
    }

    public void setRecordTemplateApplyAll(boolean recordTemplateApplyAll) {
        RecordTemplateApplyAll = recordTemplateApplyAll;
    }

    public boolean isSnapTemplateApplyAll() {
        return SnapTemplateApplyAll;
    }

    public void setSnapTemplateApplyAll(boolean snapTemplateApplyAll) {
        SnapTemplateApplyAll = snapTemplateApplyAll;
    }

    public int getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(int createdAt) {
        CreatedAt = createdAt;
    }

    public int getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(int updatedAt) {
        UpdatedAt = updatedAt;
    }

    public int getDevicesCount() {
        return DevicesCount;
    }

    public void setDevicesCount(int devicesCount) {
        DevicesCount = devicesCount;
    }

    public int getStreamCount() {
        return StreamCount;
    }

    public void setStreamCount(int streamCount) {
        StreamCount = streamCount;
    }

    public int getOnlineStreamCount() {
        return OnlineStreamCount;
    }

    public void setOnlineStreamCount(int onlineStreamCount) {
        OnlineStreamCount = onlineStreamCount;
    }

    public int getDisabledStreamCount() {
        return DisabledStreamCount;
    }

    public void setDisabledStreamCount(int disabledStreamCount) {
        DisabledStreamCount = disabledStreamCount;
    }
}
