package com.qiniu.qvs.model;

public class Stream {
    private String streamID; // 流名称, 流名称在空间中唯一，可包含 字母、数字、中划线、下划线；1 ~ 100 个字符长；创建后将不可修改
    private String desc; // 关于流的描述信息

    private String namespaceId; // 所属的空间ID
    private String namespace; // 所属的空间名称

    private String recordTemplateId; // 录制模版ID，配置流维度的录制模板
    private String snapShotTemplateId; // 截图模版ID，配置流维度的截图模板

    private boolean status; // 设备是否在线
    private boolean disabled; // 流是否被禁用
    private int lastPushedAt; // 最后一次推流时间,0:表示没有推流

    private int createdAt; // 流创建时间
    private int updatedAt; // 流更新时间

    // 以下字段只有在设备在线是才会出现
    private int userCount; // 在线观看人数
    private String clientIp; // 推流端IP
    private int audioFrameRate; // 直播流的实时音频帧率
    private int bitrate;    // 直播流的实时码率
    private int videoFrameRate; // 直播流的实时视频帧率

    public Stream(String streamID) {
        this.streamID = streamID;
    }

    public Stream(String streamID, String desc, String recordTemplateId, String snapShotTemplateId) {
        this.streamID = streamID;
        this.desc = desc;
        this.recordTemplateId = recordTemplateId;
        this.snapShotTemplateId = snapShotTemplateId;
    }

    public String getStreamID() {
        return streamID;
    }

    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getLastPushedAt() {
        return lastPushedAt;
    }

    public void setLastPushedAt(int lastPushedAt) {
        this.lastPushedAt = lastPushedAt;
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

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public int getAudioFrameRate() {
        return audioFrameRate;
    }

    public void setAudioFrameRate(int audioFrameRate) {
        this.audioFrameRate = audioFrameRate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getVideoFrameRate() {
        return videoFrameRate;
    }

    public void setVideoFrameRate(int videoFrameRate) {
        this.videoFrameRate = videoFrameRate;
    }
}
