package com.qiniu.common;

import com.qiniu.util.Json;
import com.qiniu.util.UrlSafeBase64;

/**
 * Created by Simon on 23/12/2016.
 */
public class ZoneReqInfo {
    public final String ak;
    public final String bucket;
    public ZoneReqInfo(String token) {
        // http://developer.qiniu.com/article/developer/security/upload-token.html
        // http://developer.qiniu.com/article/developer/security/put-policy.html
        String[] strings = token.split(":");
        ak = strings[0];
        String policy = new String(UrlSafeBase64.decode(strings[2]), Constants.UTF_8);
        bucket = Json.decode(policy).get("scope").toString().split(":")[0];
    }

    public ZoneReqInfo(String ak, String bucket) {
        this.ak = ak;
        this.bucket = bucket;
    }
}
