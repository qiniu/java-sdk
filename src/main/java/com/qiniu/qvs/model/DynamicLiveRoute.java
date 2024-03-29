package com.qiniu.qvs.model;

public class DynamicLiveRoute {
    private String publishIP;    // 推流端对外IP地址
    private String playIP;   // 拉流端对外IP地址
    private int urlExpireSec; // 地址过期时间,urlExpireSec:100代表100秒后过期;  默认urlExpireSec:0,永不过期.
    private int playExpireSec; // 播放过期时间(单位为秒)

    public DynamicLiveRoute(String publishIP, String playIP) {
        this.publishIP = publishIP;
        this.playIP = playIP;
    }

    public DynamicLiveRoute(String publishIP, String playIP, int urlExpireSec) {
        this.publishIP = publishIP;
        this.playIP = playIP;
        this.urlExpireSec = urlExpireSec;
    }

    public DynamicLiveRoute(String publishIP, String playIP, int urlExpireSec, int playExpireSec) {
        this.publishIP = publishIP;
        this.playIP = playIP;
        this.urlExpireSec = urlExpireSec;
        this.playExpireSec = playExpireSec;
    }

    public String getPublishIP() {
        return publishIP;
    }

    public void setPublishIP(String publishIP) {
        this.publishIP = publishIP;
    }

    public String getPlayIP() {
        return playIP;
    }

    public void setPlayIP(String playIP) {
        this.playIP = playIP;
    }

    public int getUrlExpireSec() {
        return urlExpireSec;
    }

    public void setUrlExpireSec(int urlExpireSec) {
        this.urlExpireSec = urlExpireSec;
    }

    public int getPlayExpireSec() {
        return playExpireSec;
    }

    public void setPlayExpireSec(int playExpireSec) {
        this.playExpireSec = playExpireSec;
    }
}
