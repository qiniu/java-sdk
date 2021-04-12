package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

class UploadToken extends RegionReqInfo {

    private final String token;

    UploadToken(String token) throws QiniuException {
        super(token);
        this.token = token;
    }

    String getToken() {
        return token;
    }

    boolean isValid() {
        return !StringUtils.isNullOrEmpty(token) && !StringUtils.isNullOrEmpty(getBucket()) && !StringUtils.isNullOrEmpty(getAccessKey());
    }
}
