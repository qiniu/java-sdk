package com.qiniu.qvs.model;

public class DynamicLiveRoute {
    private String PublishIP;    // 推流端对外IP地址
    private String PlayIP;   // 拉流端对外IP地址
    private int UrlExpireSec; // 地址过期时间,urlExpireSec:100代表100秒后过期;  默认urlExpireSec:0,永不过期.

    public DynamicLiveRoute(String publishIP, String playIP) {
        PublishIP = publishIP;
        PlayIP = playIP;
    }

    public DynamicLiveRoute(String publishIP, String playIP, int urlExpireSec) {
        PublishIP = publishIP;
        PlayIP = playIP;
        UrlExpireSec = urlExpireSec;
    }

    public String getPublishIP() {
        return PublishIP;
    }

    public void setPublishIP(String publishIP) {
        PublishIP = publishIP;
    }

    public String getPlayIP() {
        return PlayIP;
    }

    public void setPlayIP(String playIP) {
        PlayIP = playIP;
    }

    public int getUrlExpireSec() {
        return UrlExpireSec;
    }

    public void setUrlExpireSec(int urlExpireSec) {
        UrlExpireSec = urlExpireSec;
    }
}
