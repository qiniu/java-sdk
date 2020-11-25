package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.qvs.model.NameSpace;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class NameSpaceManager {
    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public NameSpaceManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public NameSpaceManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public NameSpaceManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    /*
        创建空间API

        请求参数Body:
        name必填
        accessType必填
        rtmpUrlType当accessType为"rtmp"时必填
        domains当rtmpUrlType为1时必填
    */
    public Response createNameSpace(NameSpace nameSpace) throws QiniuException {
        StringMap params = new StringMap();
        params.putNotNull("name", nameSpace.getName());
        params.putNotNull("desc", nameSpace.getDesc());
        params.putNotNull("accessType", nameSpace.getAccessType());
        params.putNotNull("rtmpUrlType", nameSpace.getRtmpUrlType());
        params.putNotNull("domains", nameSpace.getDomains());
        params.putNotNull("callBack", nameSpace.getCallback());
        params.putNotNull("recordTemplateId", nameSpace.getRecordTemplateId());
        params.putNotNull("snapshotTemplateId", nameSpace.getSnapShotTemplateId());
        params.putNotNull("recordTemplateApplyAll", nameSpace.isRecordTemplateApplyAll());
        params.putNotNull("snapshotTemplateApplyAll", nameSpace.isSnapTemplateApplyAll());
        params.putNotNull("urlMode", nameSpace.getUrlMode());
        params.putNotNull("zone", nameSpace.getZone());
        params.putNotNull("hlsLowLatency", nameSpace.isHlsLowLatency());
        params.putNotNull("onDemandPull", nameSpace.isOnDemandPull());

        String url = String.format("%s/v1/namespaces", apiServer);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
        删除空间API
    */
    public Response deleteNameSpace(String namespaceId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s", apiServer, namespaceId);
        return QvsResponse.delete(url, client, auth);
    }

    /*
        更新空间API
        可编辑参数: name/desc/callBack/recordTemplateId/snapshotTemplateId/recordTemplateApplyAll/snapshotTemplateApplyAll
    */
    public Response updateNameSpace(String namespaceId, PatchOperation[] patchOperation) throws QiniuException {
        StringMap params = new StringMap().put("operations", patchOperation);
        String url = String.format("%s/v1/namespaces/%s", apiServer, namespaceId);
        return QvsResponse.patch(url, params, client, auth);
    }

    /*
        查询空间信息API
    */
    public Response queryNameSpace(String namespaceId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s", apiServer, namespaceId);
        return QvsResponse.get(url, client, auth);
    }

    /*
        列出空间API
    */
    public Response listNameSpace(int offset, int line, String sortBy) throws QiniuException {
        String requestUrl = String.format("%s/v1/namespaces", apiServer);
        StringMap map = new StringMap().putNotNull("offset", offset).
                putNotNull("line", line).putNotEmpty("sortBy", sortBy);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }

    /*
        禁用空间API
    */
    public Response disableNameSpace(String namespaceId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/disabled", apiServer, namespaceId);
        return QvsResponse.post(url, new StringMap(), client, auth);
    }

    /*
        启用空间API
    */
    public Response enableNameSpace(String namespaceId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/enabled", apiServer, namespaceId);
        return QvsResponse.post(url, new StringMap(), client, auth);
    }


}
