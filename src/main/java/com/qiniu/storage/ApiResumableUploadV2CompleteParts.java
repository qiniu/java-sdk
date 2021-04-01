package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiResumableUploadV2CompleteParts extends Api {

    public ApiResumableUploadV2CompleteParts(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        private String key;
        private String fileName;
        private String fileMimeType;
        private String uploadId;
        private List<Map<String, Object>> partInfoArray;
        private Map<String, Object> params;
        private Map<String, Object> metaDataParam;

        public Request(String host, String token, String uploadId, List<Map<String, Object>> partInfoArray) {
            super(host);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
            this.uploadId = uploadId;
            this.partInfoArray = partInfoArray;
        }

        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        public Request setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Request setFileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }


        public Request setCustomParam(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public Request setCustomMetaParam(Map<String, Object> params) {
            this.metaDataParam = params;
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            UploadToken token = getUploadToken();
            if (token == null || !token.isValid()) {
                throwInvalidRequestParamException("token");
            }
            if (StringUtils.isNullOrEmpty(uploadId)) {
                throwInvalidRequestParamException("uploadId");
            }

            String bucket = token.getBucket();
            addPathSegment("buckets");
            addPathSegment(bucket);
            addPathSegment("objects");
            addPathSegment(ApiUtils.resumeV2EncodeKey(key));
            addPathSegment("uploads");
            addPathSegment(uploadId);
            super.buildPath();
        }

        @Override
        void buildBodyInfo() throws QiniuException {
            if (partInfoArray == null) {
                throwInvalidRequestParamException("partInfo");
            }

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("parts", partInfoArray);

            if (fileName != null) {
                bodyMap.put("fname", fileName);
            }
            if (fileMimeType != null) {
                bodyMap.put("mimeType", fileMimeType);
            }
            if (params != null && params.size() > 0) {
                bodyMap.put("customVars", params);
            }
            if (metaDataParam != null && metaDataParam.size() > 0) {
                bodyMap.put("metaData", metaDataParam);
            }

            String bodyString = Json.encode(bodyMap);
            byte[] body = bodyString.getBytes();
            setBody(body, 0, body.length, null);
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

    }
}
