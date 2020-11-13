package com.qiniu.qvs.model;

public class Template {
    public static final int RecordNone = 0;
    public static final int RecordRealtime = 1;
    public static final int RecordByRequired = 2;
    public static final int RecordTsFormat = 0;
    private String id;
    private String name; // 模版名称，格式为 4 ~ 100个字符，可包含小写字母、数字、中划线、汉字)
    private String desc;  // 模版描述
    private String bucket;         // 模版对应的对象存储的bucket
    private int deleteAfterDays; // 存储过期时间,默认永久不过期
    private int templateType;   // 模板类型,取值：0（录制模版）, 1（截图模版）
    private int fileType;        // 文件存储类型,取值：0（普通存储）,1（低频存储）
    private int recordInterval;    // 录制文件时长(单位为秒)
    private int snapInterval;    // 截图间隔(单位为秒)
    private String m3u8FileNameTemplate;    // m3u8文件命名格式
    private String flvFileNameTemplate;     // flv文件命名格式
    private String mp4FileNameTemplate;     // mp4文件命名格式
    private int recordType;      // 录制模式, 0（不录制）,1（实时录制）, 2（按需录制）
    private int recordFileFormat; // 录制文件存储格式(多选), 范围：1(001)～7(111), 从左往右的三位二进制数分别代表MP4, FLV, M3U8; 0代表不选择该格式, 1代表选择;例如：2(010)代表选择FLV格式，6(110)代表选择MP4和FLV格式，1(001)代表选择M3U8格式，7(111)代表三种格式均选择
    //record/ts/${namespaceId}/${streamId}/${startMs}-${endMs}.ts
    private String tsFilenametemplate;
    //record/snap/${namespaceId}/${streamId}/${startMs}.jpg // 录制封面
    private String recordSnapFileNameFmt;

    private boolean jpgOverwriteStatus; // 开启覆盖式截图(一般用于流封面)
    private boolean jpgSequenceStatus; // 开启序列式截图
    private boolean jpgOnDemandStatus; // 开启按需截图

    // 覆盖式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/${streamId}.jpg
    private String jpgOverwriteFileNameTemplate;
    // 序列式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/${startMs}.jpg
    private String jpgSequenceFileNameTemplate;
    // 按需式截图文件命名格式:snapshot/jpg/${namespaceId}/${streamId}/ondemand-${startMs}.jpg
    private String jpgOnDemandFileNameTemplate;

    private int createdAt; // 模板创建时间
    private int updatedAt; // 模板更新时间

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

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getDeleteAfterDays() {
        return deleteAfterDays;
    }

    public void setDeleteAfterDays(int deleteAfterDays) {
        this.deleteAfterDays = deleteAfterDays;
    }

    public int getTemplateType() {
        return templateType;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public int getRecordFileFormat() {
        return recordFileFormat;
    }

    public void setRecordFileFormat(int recordFileFormat) {
        this.recordFileFormat = recordFileFormat;
    }

    public String getTsFilenametemplate() {
        return tsFilenametemplate;
    }

    public void setTsFilenametemplate(String tsFilenametemplate) {
        this.tsFilenametemplate = tsFilenametemplate;
    }

    public String getRecordSnapFileNameFmt() {
        return recordSnapFileNameFmt;
    }

    public void setRecordSnapFileNameFmt(String recordSnapFileNameFmt) {
        this.recordSnapFileNameFmt = recordSnapFileNameFmt;
    }

    public boolean isJpgOverwriteStatus() {
        return jpgOverwriteStatus;
    }

    public void setJpgOverwriteStatus(boolean jpgOverwriteStatus) {
        this.jpgOverwriteStatus = jpgOverwriteStatus;
    }

    public boolean isJpgSequenceStatus() {
        return jpgSequenceStatus;
    }

    public void setJpgSequenceStatus(boolean jpgSequenceStatus) {
        this.jpgSequenceStatus = jpgSequenceStatus;
    }

    public boolean isJpgOnDemandStatus() {
        return jpgOnDemandStatus;
    }

    public void setJpgOnDemandStatus(boolean jpgOnDemandStatus) {
        this.jpgOnDemandStatus = jpgOnDemandStatus;
    }

    public String getJpgOverwriteFileNameTemplate() {
        return jpgOverwriteFileNameTemplate;
    }

    public void setJpgOverwriteFileNameTemplate(String jpgOverwriteFileNameTemplate) {
        this.jpgOverwriteFileNameTemplate = jpgOverwriteFileNameTemplate;
    }

    public String getJpgSequenceFileNameTemplate() {
        return jpgSequenceFileNameTemplate;
    }

    public void setJpgSequenceFileNameTemplate(String jpgSequenceFileNameTemplate) {
        this.jpgSequenceFileNameTemplate = jpgSequenceFileNameTemplate;
    }

    public String getJpgOnDemandFileNameTemplate() {
        return jpgOnDemandFileNameTemplate;
    }

    public void setJpgOnDemandFileNameTemplate(String jpgOnDemandFileNameTemplate) {
        this.jpgOnDemandFileNameTemplate = jpgOnDemandFileNameTemplate;
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

    public String getFlvFileNameTemplate() {
        return flvFileNameTemplate;
    }

    public void setFlvFileNameTemplate(String flvFileNameTemplate) {
        this.flvFileNameTemplate = flvFileNameTemplate;
    }

    public String getMp4FileNameTemplate() {
        return mp4FileNameTemplate;
    }

    public void setMp4FileNameTemplate(String mp4FileNameTemplate) {
        this.mp4FileNameTemplate = mp4FileNameTemplate;
    }

}
