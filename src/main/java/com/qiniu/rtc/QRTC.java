package com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.AppParam;
import com.qiniu.rtc.model.AppResult;
import com.qiniu.rtc.model.QRTCResult;
import com.qiniu.rtc.service.AppService;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;

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
     * 创建app
     *
     * @param appParam
     * @return
     * @throws QiniuException
     */
    public QRTCResult<AppResult> createApp(AppParam appParam, String accessKey, String secretKey) throws QiniuException {
        Response response = null;
        try {
            AppService appService = new AppService(Auth.create(accessKey, secretKey));
            response = appService.createApp(appParam);
            if (null == response || StringUtils.isNullOrEmpty(response.bodyString())) {
                return QRTCResult.fail(-1, "result is null");
            }
            AppResult t = Json.decode(response.bodyString(), AppResult.class);
            return QRTCResult.success(response.statusCode, t);
        } finally {
            // 释放资源
            if (response != null) response.close();
        }
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
