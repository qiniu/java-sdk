package com.qiniu.rtc;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.RoomAccess;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.Objects;

public class RtcRoomManager {
    private final Auth auth;
    private final String host;
    private final Client client;
    private Gson gson;

    public RtcRoomManager(Auth auth) {
        this(auth, "http://rtc.qiniuapi.com");
    }

    public RtcRoomManager(Auth auth, String host) {
        this.auth = auth;
        this.host = host;
        client = new Client();
        gson = new Gson();
    }

    /**
     * @param appId    房间所属帐号的 app
     * @param roomName 操作所查询的连麦房间
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response listUser(String appId, String roomName) throws QiniuException {
        String url = getLink(appId, roomName, "users");
        StringMap headers = auth.authorizationV2(url);
        return client.get(url, headers);
    }

    /**
     * @param appId    房间所属帐号的 app
     * @param roomName 操作房间名
     * @param userId   被踢人员
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response kickUser(String appId, String roomName, String userId) throws QiniuException {
        String urlStr = getLink(appId, roomName, "users/" + userId);
        StringMap headers = auth.authorizationV2(urlStr, "DELETE", null, null);
        return client.delete(urlStr, headers);
    }

    /**
     * @param appId  连麦房间所属的 app
     * @param prefix 所查询房间名的前缀索引，可以为空。
     * @param offset 分页查询的位移标记
     * @param limit  此次查询的最大长度
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @throws QiniuException
     */
    public Response listActiveRooms(String appId, String prefix, int offset, int limit) throws QiniuException {
        String url = getLink(appId, null, "?prefix=" + prefix + "&offset=" + offset + "&limit=" + limit);
        StringMap headers = auth.authorizationV2(url);
        return client.get(url, headers);
    }

    private String getLink(String appId, String roomName, String param) {
        StringBuilder builder = new StringBuilder();
        builder.append(host);
        builder.append("/v3/apps/").append(appId).append("/rooms");
        if (roomName != null) {
            builder.append("/").append(roomName).append("/");
        }
        builder.append(param);
        return builder.toString();
    }

    /**
     *
     * @param appId
     * @param roomName
     * @param start 开始时间：20200317000000
     * @param end 结束时间：20200317200000
     * @param G 查询的粒度，可选(5min, hour, day, month)，默认为day
     * @param total 表示查询房间的总时⻓ int64
     * @param kind 表示类型，Audio/SD/HD/UHD string
     * @return Response
     * @throws QiniuException
     */
    public Response getCalculateForRoom(String appId, String roomName, String start, String end, String G, int total, String kind) throws QiniuException {
        if (appId == null || roomName == null || start == null || end == null) {
            return Response.createInvalidArgument("some request param is null");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(host);
        builder.append("/v3/apps/").append(appId).append("/rooms/").append(roomName).append("/metric?");
        if (start != null) {
            builder.append("start=").append(start).append("&");
        }
        if (end != null) {
            builder.append("end=").append(end);
        }
        if (G != null) {
            builder.append("&granule=").append(G);
        }
        if (total > 0) {
            builder.append("&total=").append(total);
        }
        if (kind != null) {
            builder.append("&kind=").append(kind);
        }
        String url = builder.toString();
        StringMap headers = auth.authorizationV2(builder.toString());
        System.out.println("url："+url);
        return client.get(url, headers);
    }

    /**
     * @param appId      房间所属帐号的 app
     * @param roomName   房间名称，需满足规格 ^[a-zA-Z0-9_-]{3,64}$
     * @param userId     请求加入房间的用户 ID，需满足规格 ^[a-zA-Z0-9_-]{3,50}$
     * @param expireAt   int64 类型，鉴权的有效时间，传入以秒为单位的64位Unix绝对时间，token 将在该时间后失效
     * @param permission 该用户的房间管理权限，"admin" 或 "user"，默认为 "user" 。当权限角色为 "admin" 时，拥有将其他用户移除出房
     *                   间等特权.
     * @return roomToken
     * @throws Exception
     */
    public String getRoomToken(String appId, String roomName, String userId,
                               long expireAt, String permission) throws Exception {
        RoomAccess access = new RoomAccess(appId, roomName, userId, expireAt, permission);
        String json = gson.toJson(access);
        return auth.signRoomToken(json);
    }

}
