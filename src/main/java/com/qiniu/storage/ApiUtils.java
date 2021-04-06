package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

class ApiUtils {

    /**
     * 分片上传 v2 对 key encode
     *
     * @param key key
     * @return encode key
     */
    static String resumeV2EncodeKey(String key) {
        String encodeKey = null;
        if (key == null) {
            encodeKey = "~";
        } else if (key.equals("")) {
            encodeKey = "";
        } else {
            encodeKey = UrlSafeBase64.encodeToString(key);
        }
        return encodeKey;
    }

    /**
     * 抛出参数异常
     *
     * @param paramName 异常参数
     * @throws QiniuException 异常
     */
    static void throwInvalidRequestParamException(String paramName) throws QiniuException {
        if (StringUtils.isNullOrEmpty(paramName)) {
            throw QiniuException.unrecoverable("");
        } else {
            throw QiniuException.unrecoverable(paramName + " is invalid, set before request!");
        }
    }
}
