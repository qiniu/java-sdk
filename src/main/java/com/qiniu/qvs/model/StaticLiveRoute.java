package com.qiniu.qvs.model;

public class StaticLiveRoute {

    private String Domain; // 域名
    private String DomainType;   // 域名类型  publishRtmp | liveRtmp | liveHls | liveHdl
    private int UrlExpireSec; // 地址过期时间,urlExpireSec:100代表100秒后过期;  默认urlExpireSec:0,永不过期.

    public StaticLiveRoute(String domain, String domainType) {
        Domain = domain;
        DomainType = domainType;
    }

    public StaticLiveRoute(String domain, String domainType, int urlExpireSec) {
        Domain = domain;
        DomainType = domainType;
        UrlExpireSec = urlExpireSec;
    }

    public String getDomain() {
        return Domain;
    }

    public void setDomain(String domain) {
        Domain = domain;
    }

    public String getDomainType() {
        return DomainType;
    }

    public void setDomainType(String domainType) {
        DomainType = domainType;
    }

    public int getUrlExpireSec() {
        return UrlExpireSec;
    }

    public void setUrlExpireSec(int urlExpireSec) {
        UrlExpireSec = urlExpireSec;
    }
}
