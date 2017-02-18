package com.qiniu.streaming;

import com.qiniu.util.Auth;

/**
 * 该类封装了构建直播相关地址的方法
 */
public final class UrlFactory {
    /**
     * 直播应用名称
     */
    private final String hub;
    /**
     * Auth 对象
     */
    private final Auth auth;
    /**
     * RTMP 推流域名
     */
    private final String rtmpPublishDomain;
    /**
     * RTMP 播放域名
     */
    private final String rtmpPlayDomain;
    /**
     * HLS 播放域名
     */
    private final String hlsPlayDomain;
    /**
     * FLV 播放域名
     */
    private final String hdlPlayDomain;
    /**
     * 截图域名
     */
    private final String snapshotDomain;

    /**
     * 构建一个直播地址生成的UrlFactory对象
     */
    public UrlFactory(String hub, Auth auth, String rtmpPubDomain, String rtmpPlayDomain) {
        this(hub, auth, rtmpPubDomain, rtmpPlayDomain, null, null, null);
    }

    /**
     * 构建一个直播地址生成的UrlFactory对象
     */
    public UrlFactory(String hub, Auth auth, String rtmpPublishDomain, String rtmpPlayDomain,
                      String hlsPlayDomain, String hdlPlayDomain, String snapshotDomain) {
        this.hub = hub;
        this.auth = auth;
        this.rtmpPublishDomain = rtmpPublishDomain;
        this.rtmpPlayDomain = rtmpPlayDomain;
        this.hlsPlayDomain = hlsPlayDomain;
        this.hdlPlayDomain = hdlPlayDomain;
        this.snapshotDomain = snapshotDomain;
    }

    /**
     * 生成无鉴权的RTMP推流地址
     *
     * @param streamKey 流名称
     */
    public String rtmpPublishUrl(String streamKey) {
        return String.format("rtmp://%s/%s/%s", rtmpPublishDomain, hub, streamKey);
    }

    /**
     * 生成带有效期鉴权的RTMP推流地址
     *
     * @param streamKey          流名称
     * @param expireAfterSeconds 流过期时间，单位秒
     */
    public String rtmpPublishUrl(String streamKey, int expireAfterSeconds) {
        long expire = System.currentTimeMillis() / 1000 + expireAfterSeconds;
        String path = String.format("/%s/%s?e=%d", hub, streamKey, expire);
        String token;
        try {
            token = auth.sign(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return String.format("rtmp://%s%s&token=%s", rtmpPublishDomain, path, token);
    }

    /**
     * 构建直播RTMP播放地址
     *
     * @param streamKey 流名称
     */
    public String rtmpPlayUrl(String streamKey) {
        return String.format("rtmp://%s/%s/%s", rtmpPlayDomain, hub, streamKey);
    }

    /**
     * 构建直播HLS播放地址
     *
     * @param streamKey 流名称
     */
    public String hlsPlayUrl(String streamKey) {
        return String.format("http://%s/%s/%s.m3u8", hlsPlayDomain, hub, streamKey);
    }

    /**
     * 构建直播FLV播放地址
     *
     * @param streamKey 流名称
     */
    public String hdlPlayUrl(String streamKey) {
        return String.format("http://%s/%s/%s.flv", hdlPlayDomain, hub, streamKey);
    }

    /**
     * 构建直播截图访问地址
     *
     * @param streamKey 流名称
     */
    public String snapshotUrl(String streamKey) {
        return String.format("http://%s/%s/%s.jpg", snapshotDomain, hub, streamKey);
    }
}
