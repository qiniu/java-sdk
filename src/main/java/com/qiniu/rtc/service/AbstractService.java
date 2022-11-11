package com.qiniu.rtc.service;

import com.google.gson.Gson;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

public abstract class AbstractService {
    protected Client httpClient;
    protected Auth auth;
    protected final String host = "https://rtc.qiniuapi.com";
    protected Gson gson;

    public static final String JSON_MIME = "application/json";

    /**
     * 初始化
     *
     * @param auth 权限信息
     */
    public AbstractService(Auth auth) {
        if (null == auth) {
            throw new IllegalArgumentException("auth cannot be null...");
        }
        this.httpClient = new Client();
        this.auth = auth;
        this.gson = new Gson();
    }

    /**
     * post json 接口
     *
     * @param param      请求参数
     * @param urlPattern urlPattern
     * @param pt         url 参数
     * @return 请求响应
     * @throws QiniuException 异常
     */
    protected Response postCall(Object param, String urlPattern, Object... pt) throws QiniuException {
        //build url
        String url = String.format(host + urlPattern, pt);
        byte[] body = Json.encode(param).getBytes(Constants.UTF_8);
        StringMap sign = auth.authorizationV2(url, "POST", body, JSON_MIME);
        return httpClient.post(url, body, sign, JSON_MIME);
    }

    /**
     * delete call
     *
     * @param param      请求参数
     * @param urlPattern urlPattern
     * @param pt         url 参数
     * @return 请求响应
     * @throws QiniuException 异常
     */
    protected Response deleteCall(Object param, String urlPattern, Object... pt) throws QiniuException {
        //build url
        String url = String.format(host + urlPattern, pt);
        if (null == param) {
            StringMap sign = auth.authorizationV2(url, "DELETE", null, null);
            return httpClient.delete(url, sign);
        }
        byte[] body = Json.encode(param).getBytes(Constants.UTF_8);
        StringMap sign = auth.authorizationV2(url, "DELETE", body, JSON_MIME);
        return httpClient.delete(url, body, sign, JSON_MIME);
    }

    /**
     * get request
     *
     * @param urlPattern urlPattern
     * @param pt         url 参数
     * @return 请求响应
     * @throws QiniuException 异常
     */
    protected Response getCall(String urlPattern, Object... pt) throws QiniuException {
        //build url
        String url = String.format(host + urlPattern, pt);
        StringMap sign = auth.authorizationV2(url, "GET", null, null);
        return httpClient.get(url, sign);
    }


}
