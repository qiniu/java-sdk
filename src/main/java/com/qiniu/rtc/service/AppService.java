package com.qiniu.rtc.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.AppParam;
import com.qiniu.util.Auth;

public class AppService extends AbstractService {
    /**
     * 初始化
     *
     * @param auth auth
     */
    public AppService(Auth auth) {
        super(auth);
    }

    /**
     * @param appParam hub            绑定的直播 hub，可选，使用此 hub 的资源进行推流等业务功能，hub 与 app 必须属于同一个七牛账户
     *                 title          app 的名称，可选，注意，Title 不是唯一标识，重复 create 动作将生成多个 app
     *                 maxUsers       int 类型，可选，连麦房间支持的最大在线人数。
     *                 noAutoKickUser bool 类型，可选，禁止自动踢人（抢流）。默认为 false ，即同一个身份的 client (app/room/user) ，新的连
     *                 麦请求可以成功，旧连接被关闭。
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException 异常
     */
    public Response createApp(AppParam appParam) throws QiniuException {
        String urlPattern = "/v3/apps";
        return postCall(appParam, urlPattern);
    }

    /**
     * 获取房间信息
     *
     * @param appId 房间所属帐号的 app
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException 异常
     */
    public Response getApp(String appId) throws QiniuException {
        String urlPattern = "/v3/apps/%s";
        return getCall(urlPattern, appId);
    }

    /**
     * 删除app
     *
     * @param appId appId
     * @return Response
     * @throws QiniuException 异常
     */
    public Response deleteApp(String appId) throws QiniuException {
        String urlPattern = "/v3/apps/%s";
        return deleteCall(null, urlPattern, appId);
    }

    /**
     * 更新app信息
     * 注意！调用这个接口后仅对调用后新创建的房间有效，已经存在的房间需要等待被关闭重新创建后生效
     *
     * @param appParam appParam
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateApp(AppParam appParam) throws QiniuException {
        String urlPattern = "/v3/apps/%s";
        return postCall(appParam, urlPattern, appParam.getAppId());
    }
}
