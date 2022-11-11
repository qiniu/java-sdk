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
 * 七牛云 rtc java sdk
 * qrtc java sdk api
 *
 * @author QRTC TEAM
 * @version 8.0
 */
public class QRTC {
    //版本号
    public static final String VERSION = "8.0";

    private QRTC() {

    }

    private static volatile QRTCClient client = null;

    private static final Map<String, QRTCClient> holder = new ConcurrentHashMap<>(16);

    /**
     * 初始化QRTCClient
     *
     * @param accessKey accessKey
     * @param secretKey secretKey
     * @param appId     appId
     * @return QRTCClient
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
     * @param appParam  APP创建参数
     * @param accessKey accessKey
     * @param secretKey secretKey
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public static QRTCResult<AppResult> createApp(AppParam appParam, String accessKey, String secretKey) throws QiniuException {
        Response response = null;
        try {
            response = fetchCreateApp(appParam, accessKey, secretKey);
            return formatCreateAppResult(response);
        } finally {
            // 释放资源
            if (response != null) response.close();
        }
    }

    /**
     * 结果格式化
     *
     * @param response
     * @return
     * @throws QiniuException 异常
     */
    private static QRTCResult<AppResult> formatCreateAppResult(Response response) throws QiniuException {
        if (null == response || StringUtils.isNullOrEmpty(response.bodyString())) {
            return QRTCResult.fail(-1, "result is null");
        }
        AppResult t = Json.decode(response.bodyString(), AppResult.class);
        return QRTCResult.success(response.statusCode, t);
    }

    /**
     * 创建请求处理
     *
     * @param appParam
     * @param accessKey
     * @param secretKey
     * @return
     * @throws QiniuException 异常
     */
    private static Response fetchCreateApp(AppParam appParam, String accessKey, String secretKey) throws QiniuException {
        AppService appService = new AppService(Auth.create(accessKey, secretKey));
        return appService.createApp(appParam);
    }


    /**
     * 根据appId 获取当前的client
     *
     * @param appId appId
     * @return QRTCClient
     */
    public static QRTCClient getClient(String appId) {
        return holder.get(appId);
    }
}
