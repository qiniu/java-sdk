package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.qvs.model.Device;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.qvs.model.PlayContral;
import com.qiniu.qvs.model.VoiceChat;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class DeviceManager {
    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public DeviceManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public DeviceManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public DeviceManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }


    /*
     * 创建设备
     */
    public Response createDevice(String namespaceId, Device device) throws QiniuException {
        StringMap params = device.transferPostParam();
        String url = String.format("%s/v1/namespaces/%s/devices", apiServer, namespaceId);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 删除设备
     */
    public Response deleteDevice(String namespaceId, String gbId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s", apiServer, namespaceId, gbId);
        return QvsResponse.delete(url, client, auth);
    }

    /*
     * 更新设备
     */
    public Response updateDevice(String namespaceId, String gbId, PatchOperation[] patchOperation)
            throws QiniuException {
        StringMap params = new StringMap().put("operations", patchOperation);
        String url = String.format("%s/v1/namespaces/%s/devices/%s", apiServer, namespaceId, gbId);
        return QvsResponse.patch(url, params, client, auth);
    }

    /*
     * 查询设备
     */
    public Response queryDevice(String namespaceId, String gbId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s", apiServer, namespaceId, gbId);
        return QvsResponse.get(url, client, auth);
    }

    /*
     * 获取设备列表
     */
    public Response listDevice(String namespaceId, int offset, int line, String prefix, String state, int qtype)
            throws QiniuException {
        StringMap map = new StringMap().put("offset", offset).put("line", line).put("qtype", qtype)
                .put("prefix", prefix).put("state", state);
        String requestUrl = String.format("%s/v1/namespaces/%s/devices?%s", apiServer, namespaceId, map.formString());
        return QvsResponse.get(requestUrl, client, auth);
    }

    /*
     * 获取通道列表
     */
    public Response listChannels(String namespaceId, String gbId, String prefix) throws QiniuException {
        StringMap map = new StringMap().put("prefix", prefix);
        String requestUrl = String.format("%s/v1/namespaces/%s/devices/%s/channels?%s", apiServer, namespaceId, gbId, map.formString());
        return QvsResponse.get(requestUrl, client, auth);
    }

    /*
     * 启动设备拉流
     */
    public Response startDevice(String namespaceId, String gbId, String[] channels) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/start", apiServer, namespaceId, gbId);
        StringMap params = new StringMap().put("channels", channels);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 停止设备拉流
     */
    public Response stopDevice(String namespaceId, String gbId, String[] channels) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/stop", apiServer, namespaceId, gbId);
        StringMap params = new StringMap().put("channels", channels);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 同步设备通道
     */
    public Response fetchCatalog(String namespaceId, String gbId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/catalog/fetch", apiServer, namespaceId, gbId);
        return QvsResponse.post(url, new StringMap(), client, auth);
    }

    /*
     * 查询通道详情
     */
    public Response queryChannel(String namespaceId, String gbId, String channelId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/channels/%s", apiServer, namespaceId, gbId, channelId);
        return QvsResponse.get(url, client, auth);
    }

    /*
     * 删除通道
     */
    public Response deleteChannel(String namespaceId, String gbId, String channelId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/channels/%s", apiServer, namespaceId, gbId, channelId);
        return QvsResponse.delete(url, client, auth);
    }

    /*
     * 查询本地录像列表
     * 普通设备chId可以忽略, 置为空即可
     */
    public Response queryGBRecordHistories(String namespaceId, String gbId, String channelId, int start, int end) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/recordhistories", apiServer, namespaceId, gbId);
        StringMap map = new StringMap().put("start", start).put("end", end).putNotNull("chId", channelId);
        url = UrlUtils.composeUrlWithQueries(url, map);
        return QvsResponse.get(url, client, auth);
    }

    public Response getVoiceChatUrl(String namespaceId, String gbId, VoiceChat voiceChat) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/devices/%s/talk", apiServer, namespaceId, gbId);
        StringMap params = QvsMap.getVoiceChatMap(voiceChat);
        return com.qiniu.qvs.QvsResponse.post(url, params, client, auth);
    }

    public Response sendVoiceChatData(String url, String base64_pcm) throws QiniuException {
        StringMap params = new StringMap().putNotNull("base64_pcm", base64_pcm);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 本地录像回放控制
     * streamId 流ID可以从查询本地录像列表接口queryGBRecordHistories获取的streamId
     */
    public Response controlGBRecord(String namespaceId, String streamId, PlayContral playContral) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/playback/control", apiServer, namespaceId, streamId);
        StringMap params = QvsMap.getPlayContralMap(playContral);
        return QvsResponse.post(url, params, client, auth);
    }
}
