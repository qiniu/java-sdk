package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class PTZManager {
    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public PTZManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public PTZManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public PTZManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    /*
     * 云台控制, 本接口用于对摄像头进行 转动镜头，如水平、垂直、缩放等操作
     */
    public Response ptzControl(String namespaceId, String gbId, String cmd, int speed, String chId) throws QiniuException {
        StringMap params = new StringMap().put("cmd", cmd).put("speed", speed).putNotEmpty("chId", chId);
        String url = String.format("%s/v1/namespaces/%s/devices/%s/ptz", apiServer, namespaceId, gbId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 变焦控制
     */
    public Response focusControl(String namespaceId, String gbId, String cmd, int speed, String chId) throws QiniuException {
        StringMap params = new StringMap().put("cmd", cmd).put("speed", speed).putNotEmpty("chId", chId);
        String url = String.format("%s/v1/namespaces/%s/devices/%s/focus", apiServer, namespaceId, gbId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 光圈控制
     */
    public Response irisControl(String namespaceId, String gbId, String cmd, int speed, String chId) throws QiniuException {
        StringMap params = new StringMap().put("cmd", cmd).put("speed", speed).putNotEmpty("chId", chId);
        String url = String.format("%s/v1/namespaces/%s/devices/%s/iris", apiServer, namespaceId, gbId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 预置位控制
     */
    public Response presetsControl(String namespaceId, String gbId, String cmd, String name, int presetId, String chId) throws QiniuException {
        StringMap params = new StringMap().put("cmd", cmd).putNotEmpty("name", name).putNotEmpty("chId", chId).putNotNull("presetId", presetId);
        String url = String.format("%s/v1/namespaces/%s/devices/%s/presets", apiServer, namespaceId, gbId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 获取预置位列表
     */
    public Response listPresets(String namespaceId, String gbId, String chId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/presets", apiServer, namespaceId, gbId);
        StringMap map = new StringMap().putNotEmpty("chId", chId);
        url = UrlUtils.composeUrlWithQueries(url, map);
        return QvsResponse.get(url, client, auth);
    }
}
