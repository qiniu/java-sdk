package com.qiniu.storage.model;

/**
 * Created by bailong on 16/9/21.
 */

// 这只是个例子，可以自己创建policy 类使用
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
