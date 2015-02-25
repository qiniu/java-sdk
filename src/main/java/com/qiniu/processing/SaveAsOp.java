package com.qiniu.processing;

import com.qiniu.storage.BucketManager;

public final class SaveAsOp implements Operation {
    private String bucket;
    private String key;

    public SaveAsOp(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String build() {
        return "saveas/" + BucketManager.entry(bucket, key);
    }

    @Override
    public boolean onlyPersistent() {
        return false;
    }
}