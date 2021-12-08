package com.qiniu.rtc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 七牛云rtc java sdk
 * qrtc java sdk api
 *
 * @QRTC TEAM
 * @Version 8.0
 */
public class QRTC {
    //版本号
    public static final String VERSION = "8.0";

    private QRTC() {

    }

    private volatile static QRTCClient client = null;

    private static final Map<String, QRTCClient> holder = new ConcurrentHashMap<>(16);

    /**
     * 初始化QRTCClient
     *
     * @return
     */
    public static QRTCClient init(String accessKey, String secretKey, String appId) {
        if (null == appId || null == accessKey || null == secretKey) {
            throw new IllegalArgumentException("params cannot be null...");
        }
        if (holder.containsKey(appId)) {
            return holder.get(appId);
        }
        client = new QRTCClient(accessKey, secretKey, appId);
        holder.put(appId, client);
        return holder.get(appId);
    }

    /**
     * 根据appId 获取当前的client
     *
     * @param appId
     * @return
     */
    public static QRTCClient getClient(String appId) {
        return holder.get(appId);
    }
}
