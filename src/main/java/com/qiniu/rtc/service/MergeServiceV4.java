package com.qiniu.rtc.service;

import com.qiniu.rtc.model.MergeJob;
import com.qiniu.util.Auth;
import com.qiniu.http.Response;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MergeServiceV4 extends AbstractService {
    /**
     * 初始化
     *
     * @param auth auth
     */
    public MergeServiceV4(Auth auth) {
        super(auth);
    }

    /**
     * 创建合流任务
     *
     * @param job      任务信息
     * @param appId    appId
     * @param roomName roomName
     * @return Response
     * @throws QiniuException 异常
     */
    public Response createMergeJob(MergeJob job, String appId, String roomName) throws QiniuException {
        String urlPattern = "/v4/apps/%s/rooms/%s/jobs";
        return postCall(job, urlPattern, appId, roomName);
    }

    /**
     * 更新合流任务
     *
     * @param job      任务信息
     * @param appId    appId
     * @param roomName roomName
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateMergeJob(MergeJob job, String appId, String roomName) throws QiniuException {
        if (job == null || StringUtils.isNullOrEmpty(job.getId())) {
            throw new IllegalArgumentException("");
        }
        String urlPattern = "/v4/apps/%s/rooms/%s/jobs/update";
        return postCall(job, urlPattern, appId, roomName);
    }

    /**
     * 删除合流任务
     *
     * @param jobId    合流任务ID
     * @param appId    appId
     * @param roomName roomName
     * @return Response
     * @throws QiniuException 异常
     */
    public Response stopMergeJob(String jobId, String appId, String roomName) throws QiniuException {
        if (StringUtils.isNullOrEmpty(jobId)) {
            throw new IllegalArgumentException("");
        }
        String urlPattern = "/v4/apps/%s/rooms/%s/jobs/stop";
        Map<String, String> params = new HashMap<>();
        params.put("id", jobId);
        return postCall(params, urlPattern, appId, roomName);
    }
}
