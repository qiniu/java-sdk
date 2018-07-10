package com.qiniu.rtc;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

public class RtcAppManager {

    private final Auth auth;
    private final String host;
    private final Client client;

    private StringMap params;

    public RtcAppManager(Auth auth) {
        this(auth, "http://rtc.qiniuapi.com");
    }

    public RtcAppManager(Auth auth, String host) {
        this.auth = auth;
        this.host = host;
        this.client = new Client();
        this.params = new StringMap();
    }

    public RtcAppManager(Auth auth, String host, Client client) {
        this.auth = auth;
        this.host = host;
        this.client = client;
        this.params = new StringMap();
    }

    /**
     * @param hub            绑定的直播 hub，可选，使用此 hub 的资源进行推流等业务功能，hub 与 app 必须属于同一个七牛账户
     * @param title          app 的名称，可选，注意，Title 不是唯一标识，重复 create 动作将生成多个 app
     * @param maxUsers       int 类型，可选，连麦房间支持的最大在线人数。
     * @param noAutoKickUser bool 类型，可选，禁止自动踢人（抢流）。默认为 false ，即同一个身份的 client (app/room/user) ，新的连
     *                       麦请求可以成功，旧连接被关闭。
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response createApp(String hub, String title, int maxUsers,
                              boolean noAutoKickUser) throws QiniuException {
        if (hub != null) {
            params.put("hub", hub);
        }
        if (title != null) {
            params.put("title", title);
        }
        if (hub != null) {
            params.put("maxUsers", maxUsers);
        }
        params.put("noAutoKickUser", noAutoKickUser);

        String url = String.format("%s%s", host, "/v3/apps");
        byte[] body = Json.encode(params).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * @param appId 房间所属帐号的 app
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response getApp(String appId) throws QiniuException {
        String url = String.format("%s%s%s", host, "/v3/apps/", appId);
        StringMap headers = auth.authorizationV2(url);
        return client.get(url, headers);
    }

    /**
     * @param appId 房间所属帐号的 app
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response deleteApp(String appId) throws QiniuException {
        String urlStr = String.format("%s%s%s", host, "/v3/apps/", appId);
        StringMap headers = auth.authorizationV2(urlStr, "DELETE", null, null);
        return client.delete(urlStr, headers);
    }

    /**
     * @param appId          房间所属帐号的 app
     * @param hub            绑定的直播 hub，可选，使用此 hub 的资源进行推流等业务功能，hub 与 app 必须属于同一个七牛账户
     * @param title          app 的名称，可选，注意，Title 不是唯一标识，重复 create 动作将生成多个 app
     * @param maxUsers       int 类型，可选，连麦房间支持的最大在线人数。
     * @param noAutoKickUser bool 类型，可选，禁止自动踢人（抢流）。默认为 false ，即同一个身份的 client (app/room/user) ，新的连
     *                       麦请求可以成功，旧连接被关闭。
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response updateApp(String appId, String hub, String title, int maxUsers, boolean noAutoKickUser) throws
            QiniuException {
        if (hub != null) {
            params.put("hub", hub);
        }
        if (title != null) {
            params.put("title", title);
        }
        if (hub != null) {
            params.put("maxUsers", maxUsers);
        }
        params.put("noAutoKickUser", noAutoKickUser);

        String url = String.format("%s%s%s", host, "/v3/apps/", appId);
        byte[] body = Json.encode(params).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

}
