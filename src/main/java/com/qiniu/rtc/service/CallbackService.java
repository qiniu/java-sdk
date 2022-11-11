package com.qiniu.rtc.service;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.CallbackParam;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;

/**
 * HTTP回调的api
 */
public class CallbackService extends AbstractService {

    /**
     * 初始化
     *
     * @param auth auth
     */
    public CallbackService(Auth auth) {
        super(auth);
    }

    /**
     * 设置http业务回调
     *
     * @param appId appId
     * @param param param
     * @return Response
     * @throws QiniuException 异常
     */
    public Response setHttpCallback(String appId, CallbackParam param) throws QiniuException {
        if (null == param
                || StringUtils.isNullOrEmpty(param.getEventCbUrl())
                || StringUtils.isNullOrEmpty(param.getEventCbSecret())) {
            throw new IllegalArgumentException("CallbackParam cannot be null...");
        }
        param.setEventCbVersion(1); //only support http
        String urlPattern = "/v3/apps/%s";
        return postCall(param, urlPattern, appId);
    }

}
