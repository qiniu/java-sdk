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
     * @param auth
     * @throws IllegalArgumentException
     */
    public ForwardService(Auth auth) {
        super(auth);
    }

    /**
     * 创建单路转推任务
     *
     * @return
     */
    public Response createForwardJob(String appId, String roomId, ForwardParam param) throws QiniuException,
            IllegalArgumentException {
        if (null == param || null == roomId
                || StringUtils.isNullOrEmpty(param.getId()) || StringUtils.isNullOrEmpty(param.getPlayerId())) {
            throw new IllegalArgumentException("");
        }
        String urlPattern = "/v3/apps/%s/rooms/%s/forward_job";
        return postCall(param, urlPattern, appId, roomId);
    }

    /**
     * 停止单路转推任务
     *
     * @param appId
     * @param roomId
     * @param param  id和playerId不能为空
     * @return
     * @throws QiniuException
     * @throws IllegalArgumentException
     */
    public Response stopForwardJob(String appId, String roomId, ForwardParam param) throws QiniuException,
            IllegalArgumentException {
        if (null == param || null == roomId
                || StringUtils.isNullOrEmpty(param.getId()) || StringUtils.isNullOrEmpty(param.getPlayerId())) {
            throw new IllegalArgumentException("");
        }
        String urlPattern =  "/v3/apps/%s/rooms/%s/forward_job/delete";
        return postCall(param, urlPattern, appId, roomId);
    }
}
