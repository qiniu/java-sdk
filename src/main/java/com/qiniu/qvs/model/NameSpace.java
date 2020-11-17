package com.qiniu.qvs.model;

public class NameSpace {
    public static final int Static = 1;
    public static final int Dynamic = 2;
    private String id;
    private String name; // 空间名称(格式"^[a-zA-Z0-9_-]{1,100}$")
    private String desc;  // 空间描述
    private String accessType;  // 接入类型"gb28181"或者“rtmp”
    private int rtmpUrlType;    // accessType为“rtmp”时，推拉流地址计算方式，1:static, 2:dynamic
    private String[] domains;   // 直播域名
    private String callback;   // 后台服务器回调URL
    private boolean disabled;   // 流是否被启用, false:启用,true:禁用
    private String recordTemplateId; // 录制模版id
    private String snapShotTemplateId; // 截图模版id
    private boolean recordTemplateApplyAll;  // 空间模版是否应用到全局
    private boolean snapTemplateApplyAll; // 截图模版是否应用到全局
    private int createdAt; // 空间创建时间
    private int updatedAt; // 空间更新时间
    private int devicesCount;  // 设备数量
    private int streamCount;  // 流数量
    private int onlineStreamCount;  // 在线流数量
    private int disabledStreamCount;  // 禁用流数量

    private int urlMode; // 推拉流地址计算方式，1:static, 2:dynamic
    private String zone; // 存储区域
    private boolean hlsLowLatency; // hls低延迟开关
    private boolean onDemandPull; // 按需拉流开关

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public int getRtmpUrlType() {
        return rtmpUrlType;
    }

    public void setRtmpUrlType(int rtmpUrlType) {
        this.rtmpUrlType = rtmpUrlType;
    }

    public String[] getDomains() {
        return domains;
    }

    public void setDomains(String[] domains) {
        this.domains = domains;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getRecordTemplateId() {
        return recordTemplateId;
    }

    public void setRecordTemplateId(String recordTemplateId) {
        this.recordTemplateId = recordTemplateId;
    }

    public String getSnapShotTemplateId() {
        return snapShotTemplateId;
    }

    public void setSnapShotTemplateId(String snapShotTemplateId) {
        this.snapShotTemplateId = snapShotTemplateId;
    }

    public boolean isRecordTemplateApplyAll() {
        return recordTemplateApplyAll;
    }

    public void setRecordTemplateApplyAll(boolean recordTemplateApplyAll) {
        this.recordTemplateApplyAll = recordTemplateApplyAll;
    }

    public boolean isSnapTemplateApplyAll() {
        return snapTemplateApplyAll;
    }

    public void setSnapTemplateApplyAll(boolean snapTemplateApplyAll) {
        this.snapTemplateApplyAll = snapTemplateApplyAll;
    }

    public int getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(int createdAt) {
        this.createdAt = createdAt;
    }

    public int getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(int updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getDevicesCount() {
        return devicesCount;
    }

    public void setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
    }

    public int getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(int streamCount) {
        this.streamCount = streamCount;
    }

    public int getOnlineStreamCount() {
        return onlineStreamCount;
    }

    public void setOnlineStreamCount(int onlineStreamCount) {
        this.onlineStreamCount = onlineStreamCount;
    }

    public int getDisabledStreamCount() {
        return disabledStreamCount;
    }

    public void setDisabledStreamCount(int disabledStreamCount) {
        this.disabledStreamCount = disabledStreamCount;
    }

    public int getUrlMode() {
        return urlMode;
    }

    public void setUrlMode(int urlMode) {
        this.urlMode = urlMode;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isHlsLowLatency() {
        return hlsLowLatency;
    }

    public void setHlsLowLatency(boolean hlsLowLatency) {
        this.hlsLowLatency = hlsLowLatency;
    }

    public boolean isOnDemandPull() {
        return onDemandPull;
    }

    public void setOnDemandPull(boolean onDemandPull) {
        this.onDemandPull = onDemandPull;
    }

}
