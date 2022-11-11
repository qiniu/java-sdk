// codebeat:disable[TOO_MANY_IVARS,TOO_MANY_FUNCTIONS,TOTAL_COMPLEXITY,ARITY]
package com.qiniu.rtc;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.*;
import com.qiniu.rtc.service.*;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;

import java.util.Map;

/**
 * QRTC core api
 */
public class QRTCClient {

    private final String accessKey;
    private final String secretKey;

    //rtc 房间服务接口
    private RoomService roomService;
    private ForwardService forwardService;
    private CallbackService callbackService;
    private MergeService mergeService;
    private MergeServiceV4 mergeServiceV4;

    private AppService appService;
    // 应用ID
    private final String appId;

    //初始化的时候就把auth做了
    public QRTCClient(String accessKey, String secretKey, String appId) {
        // 变量赋值
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.appId = appId;
        //auth 本class使用，不对外
        Auth auth = auth();
        initService(auth);
    }

    /**
     * 初始化服务
     *
     * @param auth 授权对象
     */
    private void initService(Auth auth) {
        forwardService = new ForwardService(auth);
        roomService = new RoomService(auth);
        callbackService = new CallbackService(auth);
        mergeService = new MergeService(auth);
        appService = new AppService(auth);
        mergeServiceV4 = new MergeServiceV4(auth);
    }

    /////////////////////////app service//////////////////////////////////////


    /**
     * 获取当前绑定的client的app信息
     *
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<AppResult> getApp() throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return appService.getApp(appId);
            }
        };
        return buildResult(func, AppResult.class);
    }

    /**
     * 删除当前的app
     *
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<AppResult> deleteApp() throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return appService.deleteApp(appId);
            }
        };
        return buildResult(func, AppResult.class);
    }

    /**
     * 更新app信息
     *
     * @param appParam appParam
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<AppResult> updateApp(final AppParam appParam) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return appService.updateApp(appParam);
            }
        };
        return buildResult(func, AppResult.class);
    }

    /////////////////////////room service//////////////////////////////////////

    /**
     * 创建房间
     *
     * @param roomParam roomParam
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<RoomResult> createRoom(final RoomParam roomParam) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return roomService.createRoom(appId, roomParam);
            }
        };
        return buildResult(func, RoomResult.class);
    }

    /**
     * 删除房间
     *
     * @param roomName roomName
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<RoomResult> deleteRoom(final String roomName) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return roomService.deleteRoom(appId, roomName);
            }
        };
        return buildResult(func, RoomResult.class);
    }

    /**
     * 获取房间内的所有用户
     *
     * @param roomName roomName
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<RoomResult> listUser(final String roomName) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return roomService.listUser(appId, roomName);
            }
        };
        return buildResult(func, RoomResult.class);
    }

    /**
     * 指定一个用户踢出房间
     *
     * @param roomName roomName
     * @param userId userId
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<RoomResult> kickUser(final String roomName, final String userId) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return roomService.kickUser(appId, roomName, userId);
            }
        };
        return buildResult(func, RoomResult.class);
    }

    /**
     * 获取当前所有活跃的房间
     *
     * @param roomNamePrefix roomNamePrefix
     * @param offset offset
     * @param limit limit
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<RoomResult> listActiveRoom(final String roomNamePrefix, final int offset, final int limit)
            throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                UrlParam pageParam = UrlParam.builder().offset(offset).limit(limit).build();
                return roomService.listActiveRoom(appId, roomNamePrefix, pageParam);
            }
        };
        return buildResult(func, RoomResult.class);
    }

    /**
     * 获取房间token
     *
     * @param roomName   房间名称，需满足规格 ^[a-zA-Z0-9_-]{3,64}$
     * @param userId     请求加入房间的用户 ID，需满足规格 ^[a-zA-Z0-9_-]{3,50}$
     * @param expireAt   int64 类型，鉴权的有效时间，传入以秒为单位的64位Unix绝对时间，token 将在该时间后失效
     * @param permission 该用户的房间管理权限，"admin" 或 "user"，默认为 "user" 。当权限角色为 "admin" 时，拥有将其他用户移除出房
     *                   间等特权.
     * @return roomToken 房间TOKEN
     * @throws Exception 异常
     */
    public String getRoomToken(String roomName, String userId,
                               long expireAt, String permission) throws Exception {
        RoomAccess access = new RoomAccess(appId, roomName, userId, expireAt, permission);
        return roomService.getRoomToken(access);
    }

    /////////////////////////track service//////////////////////////////////////

    /**
     * 创建单路转推任务
     *
     * @param roomId roomId
     * @param param param
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<ForwardResult> createForwardJob(final String roomId, final ForwardParam param) throws QiniuException {
        ServiceCallFunc createForwardJobFunc = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return forwardService.createForwardJob(appId, roomId, param);
            }
        };
        return buildResult(createForwardJobFunc, ForwardResult.class);
    }

    /**
     * 停止单路转推的能力
     *
     * @param roomId roomId
     * @param param param
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<ForwardResult> stopForwardJob(final String roomId, final ForwardParam param) throws QiniuException {
        ServiceCallFunc stopForwardJobFunc = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return forwardService.stopForwardJob(appId, roomId, param);
            }
        };
        return buildResult(stopForwardJobFunc, ForwardResult.class);
    }

    /////////////////////////http callback service//////////////////////////////////////

    /**
     * 设置服务端回调接口
     *
     * @param param param
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    public QRTCResult<Map> setHttpCallback(final CallbackParam param) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return callbackService.setHttpCallback(appId, param);
            }
        };
        return buildResult(func, Map.class);
    }

    /////////////////////////merge service//////////////////////////////////////

    /**
     * 创建合流任务
     *
     * @param roomName roomName
     * @param mergeParam mergeParam
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    @Deprecated
    public QRTCResult<MergeResult> createMergeJob(final String roomName, final MergeParam mergeParam) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return mergeService.createMergeJob(mergeParam, appId, roomName);
            }
        };
        return buildResult(func, MergeResult.class);
    }

    /**
     * 更新合流track信息
     *
     * @param mergeTrackParam mergeTrackParam
     * @param roomName roomName
     * @param jobId jobId
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    @Deprecated
    public QRTCResult<MergeResult> updateMergeTrack(final MergeTrackParam mergeTrackParam, final String roomName, final String jobId)
            throws QiniuException {
        ServiceCallFunc updateMergeTrackFunc = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                UrlParam mergeTrackUrlParam = UrlParam.builder().appId(appId).roomName(roomName).jobId(jobId).build();
                return mergeService.updateMergeTrack(mergeTrackParam, mergeTrackUrlParam);
            }
        };
        return buildResult(updateMergeTrackFunc, MergeResult.class);
    }

    /**
     * 更新合理水印信息
     *
     * @param watermarksParam watermarksParam
     * @param roomName roomName
     * @param jobId jobId
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    @Deprecated
    public QRTCResult<MergeResult> updateMergeWatermarks(final WatermarksParam watermarksParam, final String roomName, final String jobId)
            throws QiniuException {
        ServiceCallFunc updateMergeWatermarksFunc = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                UrlParam mergeWatermarksUrlParam = UrlParam.builder().appId(appId).roomName(roomName).jobId(jobId).build();
                return mergeService.updateMergeWatermarks(watermarksParam, mergeWatermarksUrlParam);
            }
        };
        return buildResult(updateMergeWatermarksFunc, MergeResult.class);
    }

    /**
     * 停止合流任务
     *
     * @param roomName 房间名
     * @param jobId    合流任务ID
     * @return QRTCResult
     * @throws QiniuException 异常
     */
    @Deprecated
    public QRTCResult<MergeResult> stopMergeJob(final String roomName, final String jobId) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return mergeService.stopMergeJob(appId, roomName, jobId);
            }
        };
        return buildResult(func, MergeResult.class);
    }

    public QRTCResult<MergeResult> createMergeJob(final String roomName, final MergeJob job) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return mergeServiceV4.createMergeJob(job, appId, roomName);
            }
        };
        return buildResult(func, MergeResult.class);
    }

    public QRTCResult<MergeResult> updateMergeJob(final String roomName, final MergeJob job) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return mergeServiceV4.updateMergeJob(job, appId, roomName);
            }
        };
        return buildResult(func, MergeResult.class);
    }

    public QRTCResult<MergeResult> stopMergeJobById(final String roomName, final String jobId) throws QiniuException {
        ServiceCallFunc func = new ServiceCallFunc() {
            @Override
            public Response call() throws QiniuException {
                return mergeServiceV4.stopMergeJob(jobId, appId, roomName);
            }
        };
        return buildResult(func, MergeResult.class);
    }

    /**
     * 构建最后的返回结果
     *
     * @param func   请求函数
     * @param tClass 预期类型
     * @param <T>    预期类型泛型
     * @return 创建参数
     * @throws QiniuException 未知异常
     */
    private <T> QRTCResult<T> buildResult(ServiceCallFunc func, Class<T> tClass) throws QiniuException {
        Response response = null;
        try {
            response = func.call();
            return fetchResponse(tClass, response);
        } catch (QiniuException e) {
            return QRTCResult.fail(e.code(), e.getMessage());
        } finally {
            // 释放资源
            if (response != null) response.close();
        }
    }

    /**
     * 获取最终结果
     *
     * @param tClass   预期类型
     * @param response 返回结果
     * @param <T>      预期类型泛型
     * @return 格式化结果
     * @throws QiniuException 未知异常
     */
    private <T> QRTCResult<T> fetchResponse(Class<T> tClass, Response response) throws QiniuException {
        if (null == response || StringUtils.isNullOrEmpty(response.bodyString())) {
            return QRTCResult.fail(-1, "response is null");
        }
        // 返回格式化结果
        return formatResult(tClass, response);
    }

    /**
     * 格式化结果
     *
     * @param tClass   预期类型
     * @param response 返回结果
     * @param <T>      预期类型泛型
     * @return 返回结果
     * @throws QiniuException 未知异常
     */
    private <T> QRTCResult<T> formatResult(Class<T> tClass, Response response) throws QiniuException {
        T t = Json.decode(response.bodyString(), tClass);
        QRTCResult<T> result = QRTCResult.success(response.statusCode, t);
        return result;
    }

    private Auth auth() {
        return Auth.create(this.accessKey, this.secretKey);
    }

}
