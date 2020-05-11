package com.qiniu.qvs.model;

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
}
