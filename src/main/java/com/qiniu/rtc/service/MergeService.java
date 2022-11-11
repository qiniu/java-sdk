package com.qiniu.rtc.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.rtc.model.MergeParam;
import com.qiniu.rtc.model.MergeTrackParam;
import com.qiniu.rtc.model.UrlParam;
import com.qiniu.rtc.model.WatermarksParam;
import com.qiniu.util.Auth;

@Deprecated
public class MergeService extends AbstractService {
    /**
     * 初始化
     *
     * @param auth auth
     */
    public MergeService(Auth auth) {
        super(auth);
    }

    /**
     * 创建合流任务
     *
     * @param mergeParam 合流业务请求参数
     * @param appId      appId
     * @param roomName   roomName
     * @return Response
     * @throws QiniuException 异常
     */
    public Response createMergeJob(MergeParam mergeParam, String appId, String roomName) throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/merge_job";
        return postCall(mergeParam, urlPattern, appId, roomName);
    }

    /**
     * 更新合流track 信息
     *
     * @param mergeTrackParam MergeTrackParam
     * @param param           UrlParam
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateMergeTrack(MergeTrackParam mergeTrackParam, UrlParam param)
            throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/merge_job/%s/tracks";
        return postCall(mergeTrackParam, urlPattern, param.getAppId(), param.getRoomName(), param.getJobId());
    }


    /**
     * 更新合流水印
     *
     * @param watermarksParam WatermarksParam
     * @param urlParam        UrlParam
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateMergeWatermarks(WatermarksParam watermarksParam, UrlParam urlParam)
            throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/merge_job/%s/watermarks";
        return postCall(watermarksParam, urlPattern, urlParam.getAppId(), urlParam.getRoomName(), urlParam.getJobId());
    }

    /**
     * 停止合流任务
     *
     * @param appId    appId
     * @param roomName roomName
     * @param jobId    jobId
     * @return Response
     * @throws QiniuException 异常
     */
    public Response stopMergeJob(String appId, String roomName, String jobId) throws QiniuException {
        String urlPattern = "/v3/apps/%s/rooms/%s/merge_job/%s";
        return deleteCall(null, urlPattern, appId, roomName, jobId);
    }

}
