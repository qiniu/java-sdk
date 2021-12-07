package com.qiniu.rtc.service;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.ForwardParam;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;

/**
 * 流媒体相关处理服务的api
 */
public class ForwardService extends AbstractService {

    /**
     * 初始化，暴露次级接口，客户也可以直接初始化
     *
     * @param auth 授权对象
     * @throws IllegalArgumentException 参数不合法异常
     */
    public ForwardService(Auth auth) {
        super(auth);
    }

    /**
     * 创建单路转推任务
     *
     * @param appId  应用ID
     * @param roomId 房间ID
     * @param param  id和playerId不能为空
     * @return 回应信息
     * @throws QiniuException           未知异常
     * @throws IllegalArgumentException 参数不合法异常
     */
    public Response createForwardJob(String appId, String roomId, ForwardParam param) throws QiniuException,
            IllegalArgumentException {
        checkParams(roomId, param);
        String urlPattern = "/v3/apps/%s/rooms/%s/forward_job";
        return postCall(param, urlPattern, appId, roomId);
    }

    /**
     * 停止单路转推任务
     *
     * @param appId  应用ID
     * @param roomId 房间ID
     * @param param  id和playerId不能为空
     * @return 回应信息
     * @throws QiniuException           未知异常
     * @throws IllegalArgumentException 参数不合法异常
     */
    public Response stopForwardJob(String appId, String roomId, ForwardParam param) throws QiniuException,
            IllegalArgumentException {
        checkParams(roomId, param);
        String urlPattern = "/v3/apps/%s/rooms/%s/forward_job/delete";
        return postCall(param, urlPattern, appId, roomId);
    }

    /**
     * 参数校验
     *
     * @param roomId 房间ID
     * @param param 转推参数对象
     * @throws IllegalArgumentException 参数不合法异常
     */
    private void checkParams(String roomId, ForwardParam param) throws IllegalArgumentException {
        if (null == param || null == roomId
                || StringUtils.isNullOrEmpty(param.getId()) || StringUtils.isNullOrEmpty(param.getPlayerId())) {
            throw new IllegalArgumentException("");
        }
    }
}
