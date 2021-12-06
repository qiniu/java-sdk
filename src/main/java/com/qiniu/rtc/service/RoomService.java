package com.qiniu.rtc.service;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.RoomAccess;
import com.qiniu.util.Auth;

/**
 * 房间相关处理的api
 */
public class RoomService extends AbstractService {

    /**
     * 初始化
     *
     * @param auth
     */
    public RoomService(Auth auth) {
        super(auth);
    }


    /**
     * @param appId    房间所属帐号的 app
     * @param roomName 操作所查询的连麦房间
     * @param appId
     * @param roomName
     * @return Response      如果不读取Response的数据，请注意调用Close方法关闭
     * @return
     * @throws QiniuException
     */
    public Response listUser(String appId, String roomName) throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/users";
        return getCall(urlPattern, appId, roomName);
    }

    /**
     * 踢出房间内的某个用户
     *
     * @param appId
     * @param roomName
     * @param userId
     * @return
     * @throws QiniuException
     */
    public Response kickUser(String appId, String roomName, String userId) throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/users/%s";
        return deleteCall(null, urlPattern, appId, roomName, userId);
    }

    /**
     * 获取当前所有活跃的房间
     *
     * @param roomNamePrefix
     * @param offset
     * @param limit
     * @return
     * @throws QiniuException
     */
    public Response listActiveRoom(String appId, String roomNamePrefix, int offset, int limit) throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms?prefix=%s&offset=%d&limit=%d";
        return getCall(urlPattern, appId, roomNamePrefix, offset, limit);
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
