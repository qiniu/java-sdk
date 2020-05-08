package com.qiniu.qvs.model;

public class Template {
    private String ID;
    private String Name; // 模版名称，格式为 4 ~ 100个字符，可包含小写字母、数字、中划线、汉字)
    private String Desc;  // 模版描述
    private String Bucket;         // 模版对应的对象存储的bucket
    private int DeleteAfterDays; // 存储过期时间,默认永久不过期
    private int TemplateType;   // 模板类型,取值：0（录制模版）, 1（截图模版）
    private int FileType;        // 文件存储类型,取值：0（普通存储）,1（低频存储）
    private int recordInterval;	// 录制文件时长(单位为秒)
    private int snapInterval;	// 截图间隔(单位为秒)
    private String	m3u8FileNameTemplate;	// m3u8文件命名格式

    private int RecordType;      // 录制模式, 0（不录制）,1（实时录制）, 2（按需录制）
    private int RecordFileFormat; // 录制文件存储格式,取值：0（ts格式存储）

    //record/ts/${namespaceId}/${streamId}/${startMs}-${endMs}.ts
    private String TSFileNameTemplate;
    //record/snap/${namespaceId}/${streamId}/${startMs}.jpg // 录制封面
    private String RecordSnapFileNameFmt;

    private boolean JpgOverwriteStatus; // 开启覆盖式截图(一般用于流封面)
    private boolean JpgSequenceStatus; // 开启序列式截图
    private boolean JpgOnDemandStatus; // 开启按需截图

    // 覆盖式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/${streamId}.jpg
    private String JpgOverwriteFileNameTemplate;
    // 序列式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/${startMs}.jpg
    private String JpgSequenceFileNameTemplate;
    // 按需式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/ondemand-${startMs}.jpg
    private String JpgOnDemandFileNameTemplate;

    private int CreatedAt; // 模板创建时间
    private int UpdatedAt; // 模板更新时间

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

    public String getBucket() {
        return Bucket;
    }

    public void setBucket(String bucket) {
        Bucket = bucket;
    }

    public int getDeleteAfterDays() {
        return DeleteAfterDays;
    }

    public void setDeleteAfterDays(int deleteAfterDays) {
        DeleteAfterDays = deleteAfterDays;
    }

    public int getTemplateType() {
        return TemplateType;
    }

    public void setTemplateType(int templateType) {
        TemplateType = templateType;
    }

    public int getFileType() {
        return FileType;
    }

    public void setFileType(int fileType) {
        FileType = fileType;
    }

    public int getRecordType() {
        return RecordType;
    }

    public void setRecordType(int recordType) {
        RecordType = recordType;
    }

    public int getRecordFileFormat() {
        return RecordFileFormat;
    }

    public void setRecordFileFormat(int recordFileFormat) {
        RecordFileFormat = recordFileFormat;
    }

    public String getTSFileNameTemplate() {
        return TSFileNameTemplate;
    }

    public void setTSFileNameTemplate(String TSFileNameTemplate) {
        this.TSFileNameTemplate = TSFileNameTemplate;
    }

    public String getRecordSnapFileNameFmt() {
        return RecordSnapFileNameFmt;
    }

    public void setRecordSnapFileNameFmt(String recordSnapFileNameFmt) {
        RecordSnapFileNameFmt = recordSnapFileNameFmt;
    }

    public boolean isJpgOverwriteStatus() {
        return JpgOverwriteStatus;
    }

    public void setJpgOverwriteStatus(boolean jpgOverwriteStatus) {
        JpgOverwriteStatus = jpgOverwriteStatus;
    }

    public boolean isJpgSequenceStatus() {
        return JpgSequenceStatus;
    }

    public void setJpgSequenceStatus(boolean jpgSequenceStatus) {
        JpgSequenceStatus = jpgSequenceStatus;
    }

    public boolean isJpgOnDemandStatus() {
        return JpgOnDemandStatus;
    }

    public void setJpgOnDemandStatus(boolean jpgOnDemandStatus) {
        JpgOnDemandStatus = jpgOnDemandStatus;
    }

    public String getJpgOverwriteFileNameTemplate() {
        return JpgOverwriteFileNameTemplate;
    }

    public void setJpgOverwriteFileNameTemplate(String jpgOverwriteFileNameTemplate) {
        JpgOverwriteFileNameTemplate = jpgOverwriteFileNameTemplate;
    }

    public String getJpgSequenceFileNameTemplate() {
        return JpgSequenceFileNameTemplate;
    }

    public void setJpgSequenceFileNameTemplate(String jpgSequenceFileNameTemplate) {
        JpgSequenceFileNameTemplate = jpgSequenceFileNameTemplate;
    }

    public String getJpgOnDemandFileNameTemplate() {
        return JpgOnDemandFileNameTemplate;
    }

    public void setJpgOnDemandFileNameTemplate(String jpgOnDemandFileNameTemplate) {
        JpgOnDemandFileNameTemplate = jpgOnDemandFileNameTemplate;
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

    public int getRecordInterval() {
        return recordInterval;
    }

    public void setRecordInterval(int recordInterval) {
        this.recordInterval = recordInterval;
    }

    public int getSnapInterval() {
        return snapInterval;
    }

    public void setSnapInterval(int snapInterval) {
        this.snapInterval = snapInterval;
    }

    public String getM3u8FileNameTemplate() {
        return m3u8FileNameTemplate;
    }

    public void setM3u8FileNameTemplate(String m3u8FileNameTemplate) {
        this.m3u8FileNameTemplate = m3u8FileNameTemplate;
    }
}
