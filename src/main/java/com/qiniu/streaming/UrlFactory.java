package com.qiniu.streaming;

import com.qiniu.util.Auth;

/**
 * Created by bailong on 16/9/22.
 */
public final class UrlFactory {

    private final String hub;
    private final Auth auth;
    private final String rtmpPubDomain;
    private final String rtmpPlayDomain;
    private final String hlsDomain;
    private final String hdlDomain;
    private final String snapDomain;

    public UrlFactory(String hub, Auth auth, String rtmpPubDomain, String rtmpPlayDomain) {
        this(hub, auth, rtmpPubDomain, rtmpPlayDomain, null, null, null);
    }

    public UrlFactory(String hub, Auth auth, String rtmpPubDomain,
                      String rtmpPlayDomain, String hlsDomain, String hdlDomain, String snapDomain) {
        this.hub = hub;
        this.auth = auth;
        this.rtmpPubDomain = rtmpPubDomain;
        this.rtmpPlayDomain = rtmpPlayDomain;
        this.hlsDomain = hlsDomain;
        this.hdlDomain = hdlDomain;
        this.snapDomain = snapDomain;
    }

    public String rtmpPublishUrl(String streamKey) {
        return String.format("rtmp://%s/%s/%s", rtmpPubDomain, hub, streamKey);
    }

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
        return String.format("rtmp://%s%s&token=%s", rtmpPubDomain, path, token);
    }

    /*
        RTMPPlayURL generates RTMP play URL
     */
    public String rtmpPlayUrl(String streamKey) {
        return String.format("rtmp://%s/%s/%s", rtmpPlayDomain, hub, streamKey);
    }

    /*
        HLSPlayURL generates HLS play URL
     */
    public String hlsPlayUrl(String streamKey) {
        return String.format("http://%s/%s/%s.m3u8", hlsDomain, hub, streamKey);
    }

    /*
        HDLPlayURL generates HDL play URL
     */
    public String hdlPlayUrl(String streamKey) {
        return String.format("http://%s/%s/%s.flv", hdlDomain, hub, streamKey);
    }

    /*
        SnapshotPlayURL generates snapshot URL
     */
    public String snapshotUrl(String streamKey) {
        return String.format("http://%s/%s/%s.jpg", snapDomain, hub, streamKey);
    }
}
