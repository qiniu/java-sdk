package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.qvs.model.Template;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class TemplateManager {

    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public TemplateManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public TemplateManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public TemplateManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    /*
        创建模板
     */
    public Response createTemplate(Template template) throws QiniuException {
        StringMap params = new StringMap();
        params.putNotNull("name", template.getName());
        params.putNotNull("desc", template.getDesc());
        params.putNotNull("bucket", template.getBucket());
        params.putNotNull("deleteAfterDays", template.getDeleteAfterDays());
        params.putNotNull("fileType", template.getFileType());
        params.putNotNull("recordFileFormat", template.getRecordFileFormat());
        params.putNotNull("templateType", template.getTemplateType());
        params.putNotNull("recordInterval", template.getRecordInterval());
        params.putNotNull("snapInterval", template.getSnapInterval());
        params.putNotNull("recordType", template.getRecordType());
        params.putNotNull("jpgOverwriteStatus", template.isJpgOverwriteStatus());
        params.putNotNull("jpgSequenceStatus", template.isJpgSequenceStatus());
        params.putNotNull("jpgOnDemandStatus", template.isJpgOnDemandStatus());

        String url = String.format("%s/v1/templates", apiServer);
        return QvsResponse.post(url, params, client, auth);
    }

    /*
        删除模板API
    */
    public Response deleteTemplate(String templateId) throws QiniuException {
        String url = String.format("%s/v1/templates/%s", apiServer, templateId);
        return QvsResponse.delete(url, client, auth);
    }

    /*
        更新模板API
    */
    public Response updateTemplate(String templateId, PatchOperation[] patchOperation) throws QiniuException {
        StringMap params = new StringMap().put("operations", patchOperation);
        String url = String.format("%s/v1/templates/%s", apiServer, templateId);
        return QvsResponse.patch(url, params, client, auth);
    }

    /*
        查询模板信息API
    */
    public Response queryTemplate(String templateId) throws QiniuException {
        String url = String.format("%s/v1/templates/%s", apiServer, templateId);
        return QvsResponse.get(url, client, auth);
    }

    /*
        获取模板列表
    */
    public Response listTemplate(int offset, int line, int templateType, String match) throws QiniuException {
        String requestUrl = String.format("%s/v1/templates", apiServer);
        StringMap map = new StringMap().put("offset", offset).put("line", line).
                put("templateType", templateType).putNotEmpty("match", match);
        requestUrl = UrlUtils.composeUrlWithQueries(requestUrl, map);
        return QvsResponse.get(requestUrl, client, auth);
    }

}
