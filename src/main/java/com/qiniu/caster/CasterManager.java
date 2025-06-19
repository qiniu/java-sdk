package com.qiniu.caster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.qiniu.caster.model.CasterParams;
import com.qiniu.caster.model.Overlay;
import com.qiniu.caster.model.Text;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;


public class CasterManager {
    private final Auth auth;
    private final String server;
    private final Client client;

    public CasterManager(Auth auth) {
        this(auth, "http://pili-caster.qiniuapi.com");
    }

    private CasterManager(Auth auth, String server) {
        this.auth = auth;
        this.server = server;
        this.client = new Client();
    }

    public CasterManager(Auth auth, String server, Client client) {
        this.auth = auth;
        this.server = server;
        this.client = client;
    }

    /**
     *
     * 创建云导播
     * @param name 导播台name
     * @param casterParams 导播台参数
     * @return
     */
    public Response createCaster(String name, CasterParams casterParams) {
        String url = server + "/v1/casters";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.toJsonTree(casterParams);
        jsonElement.getAsJsonObject().addProperty("name", name);
        byte[] body = gson.toJson(jsonElement).getBytes(Constants.UTF_8);
        String a = gson.toJson(jsonElement);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 云导播列表
     * @param name 云导播名称前缀匹配
     * @param marker 如果上一次返回的结果超过了单次查询限制，则会返回marker表示上一次读取到哪条记录；这一次请求带上marker后，继续从该marker后开始读取,设置为null为从头开始
     * @param limit 返回的最大数量，设置为-1默认为50
     * @return
     */
    public Response findCasters(String name, String marker, int limit) {
        StringMap map = new StringMap().putNotEmpty("marker", marker)
                .putNotEmpty("name", name).putWhen("limit", limit, limit > 0);
        String queryString = map.formString();
        if (map.size() > 0) {
            queryString = "?" + queryString;
        }
        String url = String.format("%s/v1/casters/%s", server, queryString);
        StringMap headers = auth.authorizationV2(url);
        Response response = null;
        try {
            response = client.get(url, headers);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 云导播信息
     * @param casterId 导播台id
     * @return
     */
    public Response getCasterInfo(String casterId) {
        String url = String.format("%s/v1/casters/%s", server, casterId);
        StringMap headers = auth.authorizationV2(url);
        Response response = null;
        try {
            response = client.get(url, headers);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 开启云导播
     * @param casterId 导播台id
     * @param start 开启时间，设置为-1立即启动；导播台已开启时，禁止设置开启时间，规格为时间戳（秒级）
     * @param hour 开启时长，设置为-1为默认为1小时；导播台已开启时，表示延长开启时长 [1.~]
     * @return
     */
    public Response startCaster(String casterId, int start, int hour) {
        String url = String.format("%s/v1/casters/%s/start", server, casterId);
        StringMap map = new StringMap().putWhen("start", start, start > 0)
                .putWhen("hour", hour, hour > 0);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 关闭云导播
     * @param casterId 导播台id
     * @return
     */
    public Response stopCaster(String casterId) {
        String url = String.format("%s/v1/casters/%s/stop", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterId", casterId);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 删除云导播
     * @param casterId 导播台id
     * @return
     */
    public Response deleteCaster(String casterId) {
        String url = String.format("%s/v1/casters/%s", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterId", casterId);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "DELETE", body, Client.JsonMime);
        Response response = null;
        try {
            response = client.delete(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新云导播
     * @param casterId 导播台id
     * @param casterParams 导播台更新参数
     * @return
     */
    public Response update(String casterId, CasterParams casterParams) {
        String url = String.format("%s/v1/casters/%s", server, casterId);
        byte[] body = Json.encode(casterParams).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新PVW监视器频道
     * @param casterId 导播台id
     * @param channel 监视器频道 [0,7]
     * @param staticKey 静态密钥
     * @return
     */
    public Response updatePvw(String casterId, int channel, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/pvw", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putWhen("channel", channel, channel >= 0);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新PGM监视器频道
     * @param casterId 导播台id
     * @param channel 监视器频道 [0,7]
     * @param staticKey 静态密钥
     * @return
     */
    public Response updatePgm(String casterId, int channel, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/pgm", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putWhen("channel", channel, channel >= 0);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * PVW切换至PGM
     * @param casterId 导播台id
     * @param switchVol 是否同步切换音频
     * @param staticKey 静态密钥
     * @return
     */
    public Response pvwSwitchToPgm(String casterId, boolean switchVol, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/switch", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putNotNull("switchVol", switchVol);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新监视器配置
     * @param casterId 导播台id
     * @param channel 监视器频道 [0,7]
     * @param newUrl 监视器源流地址，支持直播流、静态视频、图片等
     * @param vol 音量[0,300]
     * @param muted 是否静音
     * @param staticKey 静态密钥
     * @return
     */
    public Response updateMonitors(String casterId, int channel, String newUrl, int vol, boolean muted, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/monitors", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putWhen("channel", channel, channel >= 0)
                .putNotNull("url", newUrl).putWhen("vol", vol, vol >= 0).putNotNull("muted", muted);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新输出配置
     * @param casterId 导播台id
     * @param newUrl PGM推流地址
     * @param ab [64,512]  音频码率(Kbps)，默认值为64，-1为默认值
     * @param vb [300,10240] 视频码率(Kbps)，默认值为1000，-1为默认值
     * @param closed 关闭推流
     * @param emergencyMode 紧急模式开关
     * @param emergencyChannel 紧急模式监视器频道，取值范围是[0,7]，默认使用0频道 [0,7]
     * @param delay [0，180] 延时播放，单位: 秒
     * @param staticKey 静态密钥
     * @return
     */
    public Response updatePublish(String casterId, String newUrl, int ab, int vb, boolean closed, boolean emergencyMode, int emergencyChannel, int delay, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/publish", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putNotNull("url", newUrl).putWhen("vb", vb, vb >= 0)
                .putWhen("ab", ab, ab >= 0)
                .putNotNull("closed", closed).putNotNull("emergencyMode", emergencyMode)
                .putWhen("emergencyChannel", emergencyChannel, emergencyChannel >= 0)
                .putWhen("delay", delay, delay >= 0);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新PVW布局ID
     * @param casterId 导播台id
     * @param layout 布局ID，-1表示不使用布局
     * @param staticKey 静态密钥
     * @return
     */
    public Response changeLayout(String casterId, int layout, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/pvw/layouts", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putWhen("layout", layout, layout >= 0);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * 更新布局配置
     * @param casterId 导播台ID
     * @param layout [0,7] 布局ID
     * @param title 布局标题，可留空
     * @param overlay 画中画配置，key为对应的监视器频道，详细参数见画中画配置(overlay)详细参数
     * @param text 文字水印配置，key为文字水印标题，详细参数见文字水印配置(text)详细参数
     * @param staticKey 静态密钥
     * @return
     */
    public Response updateLayout(String casterId, int layout, String title, Overlay overlay, Text text, String staticKey) {
        String url = String.format("%s/v1/static/casters/%s/layouts", server, casterId);
        StringMap map = new StringMap().putNotNull("CasterID", casterId)
                .putWhen("layout", layout, layout >= 0)
                .putNotNull("title", title).putNotNull("overlay", overlay).putNotNull("text", text);
        byte[] body = Json.encode(map).getBytes(Constants.UTF_8);
        StringMap headers = new StringMap().put("Authorization", staticKey);
        Response response = null;
        try {
            response = client.post(url, body, headers, Client.JsonMime);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return response;
    }
}
