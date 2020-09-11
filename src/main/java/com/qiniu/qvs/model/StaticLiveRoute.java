package com.qiniu.qvs.model;

import com.qiniu.common.Constants;
import com.qiniu.util.Md5;
import com.qiniu.util.UrlSafeBase64;

public class StaticLiveRoute {

    private String domain; // 域名
    private String domainType;   // 域名类型  publishRtmp | liveRtmp | liveHls | liveHdl
    private int urlExpireSec; // 地址过期时间,urlExpireSec:100代表100秒后过期;  默认urlExpireSec:0,永不过期.

    public StaticLiveRoute(String domain, String domainType) {
        this.domain = domain;
        this.domainType = domainType;
    }

    public StaticLiveRoute(String domain, String domainType, int urlExpireSec) {
        this.domain = domain;
        this.domainType = domainType;
        this.urlExpireSec = urlExpireSec;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public int getUrlExpireSec() {
        return urlExpireSec;
    }

    public void setUrlExpireSec(int urlExpireSec) {
        this.urlExpireSec = urlExpireSec;
    }

    public String genStaticHLSFLVDomain(String nsId, String streamId, String key, boolean useHttps) {
        String path = "/" + nsId + "/" + streamId;
        String scheme = useHttps ? "https" : "http";
        String host = "";
        if ("liveHls".equals(domainType)) {
            host = domain + ":1370";
            path += ".m3u8";
        } else {
            host = domain + ":1360";
            path += ".flv";
        }
        long expireTime = System.currentTimeMillis() + urlExpireSec * 1000;
        String token = signToken(key, path, expireTime);
        return String.format("%s://%s%s?e=%d&token=%s", scheme, host, path, expireTime, token);
    }

    public String genStaticRtmpDomain(String nsId, String streamId, String key) {
        String path = "/" + nsId + "/" + streamId;
        String scheme = "rtmp";
        String host = domain + ":2045";
        long expireTime = System.currentTimeMillis() + urlExpireSec * 1000;
        String token = signToken(key, path, expireTime);
        return String.format("%s://%s%s?e=%d&token=%s", scheme, host, path, expireTime, token);
    }

    private String signToken(String key, String path, long expireTime) {
        String encode_path = UrlSafeBase64.encodeToString(path);
        String tempS = key + encode_path + Long.toHexString(expireTime);
        return Md5.md5(tempS.getBytes(Constants.UTF_8));
    }
}
