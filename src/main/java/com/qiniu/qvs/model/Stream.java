package com.qiniu.qvs.model;

public class Stream {
    private String StreamID;// 流名称, 流名称在空间中唯一，可包含 字母、数字、中划线、下划线；1 ~ 100 个字符长；创建后将不可修改
    private String Desc; // 关于流的描述信息

    private String NamespaceId; // 所属的空间ID
    private String Namespace; // 所属的空间名称

    private String RecordTemplateId; // 录制模版ID，配置流维度的录制模板
    private String SnapShotTemplateId; // 截图模版ID，配置流维度的截图模板

    private boolean Status; // 设备是否在线
    private boolean Disabled; // 流是否被禁用
    private int LastPushedAt;// 最后一次推流时间,0:表示没有推流

    private int CreatedAt; // 流创建时间
    private int UpdatedAt; // 流更新时间

    // 以下字段只有在设备在线是才会出现
    private int UserCount; // 在线观看人数
    private String ClientIp; // 推流端IP
    private int AudioFrameRate; // 直播流的实时音频帧率
    private int BitRate;    // 直播流的实时码率
    private int VideoFrameRate; // 直播流的实时视频帧率
    
    public Stream(String streamID) {
        StreamID = streamID;
    }

    public Stream(String streamID, String desc, String recordTemplateId, String snapShotTemplateId) {
        StreamID = streamID;
        Desc = desc;
        RecordTemplateId = recordTemplateId;
        SnapShotTemplateId = snapShotTemplateId;
    }

    public String getStreamID() {
        return StreamID;
    }

    public void setStreamID(String streamID) {
        StreamID = streamID;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getNamespaceId() {
        return NamespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        NamespaceId = namespaceId;
    }

    public String getNamespace() {
        return Namespace;
    }

    public void setNamespace(String namespace) {
        Namespace = namespace;
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

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public boolean isDisabled() {
        return Disabled;
    }

    public void setDisabled(boolean disabled) {
        Disabled = disabled;
    }

    public int getLastPushedAt() {
        return LastPushedAt;
    }

    public void setLastPushedAt(int lastPushedAt) {
        LastPushedAt = lastPushedAt;
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

    public int getUserCount() {
        return UserCount;
    }

    public void setUserCount(int userCount) {
        UserCount = userCount;
    }

    public String getClientIp() {
        return ClientIp;
    }

    public void setClientIp(String clientIp) {
        ClientIp = clientIp;
    }

    public int getAudioFrameRate() {
        return AudioFrameRate;
    }

    public void setAudioFrameRate(int audioFrameRate) {
        AudioFrameRate = audioFrameRate;
    }

    public int getBitRate() {
        return BitRate;
    }

    public void setBitRate(int bitRate) {
        BitRate = bitRate;
    }

    public int getVideoFrameRate() {
        return VideoFrameRate;
    }

    public void setVideoFrameRate(int videoFrameRate) {
        VideoFrameRate = videoFrameRate;
    }
}
