package com.qiniu.storage.model;

/**
 * 该类封装了上传策略
 * 参考文档：<a href="https://developer.qiniu.com/kodo/manual/put-policy">上传策略</a>
 */
public final class UploadPolicy {

    public final String scope;
    public final long deadline;
    public int insertOnly;

    public String endUser;

    public String returnUrl;
    public String returnBody;

    public String callbackUrl;
    public String callbackHost;
    public String callbackBody;
    public String callbackBodyType;
    public int callbackFetchKey;

    public String persistentOps;
    public String persistentNotifyUrl;
    public String persistentPipeline;
    public String saveKey;

    public long fsizeMin;
    public long fsizeLimit;

    public int detectMime;

    public String mimeLimit;

    public int deleteAfterDays;

    public UploadPolicy(String bucket, String key, long expired) {
        this.scope = key == null ? bucket : bucket + "" + key;
        this.deadline = System.currentTimeMillis() / 1000 + expired;
    }
}
