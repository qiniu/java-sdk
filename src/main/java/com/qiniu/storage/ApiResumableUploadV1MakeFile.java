package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.Map;

public class ApiResumableUploadV1MakeFile extends Api {
    public ApiResumableUploadV1MakeFile(Client client) {
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
        private Long fileSize;
        private String fileMimeType;
        private String[] blockContexts;
        private Map<String, Object> params;
        private Map<String, Object> metaDataParam;

        public Request(String host, String token, Long fileSize, String[] blockContexts) {
            super(host);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
            this.fileSize = fileSize;
            this.blockContexts = blockContexts;
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
            if (fileSize == null) {
                throwInvalidRequestParamException("file size");
            }

            addPathSegment("mkfile");
            addPathSegment(fileSize + "");

            if (!StringUtils.isNullOrEmpty(fileMimeType)) {
                addPathSegment("mimeType");
                addPathSegment(UrlSafeBase64.encodeToString(fileMimeType));
            }

            if (!StringUtils.isNullOrEmpty(fileName)) {
                addPathSegment("fname");
                addPathSegment(UrlSafeBase64.encodeToString(fileName));
            }

            if (key != null) {
                addPathSegment("key");
                addPathSegment(UrlSafeBase64.encodeToString(key));
            }

            if (params != null && params.size() > 0) {
                for (String key : params.keySet()) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + params.get(key)));
                }
            }

            if (metaDataParam != null && metaDataParam.size() > 0) {
                for (String key : metaDataParam.keySet()) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + metaDataParam.get(key)));
                }
            }

            super.buildPath();
        }

        @Override
        void buildBodyInfo() throws QiniuException {
            if (blockContexts == null) {
                throwInvalidRequestParamException("contexts");
            }

            String s = StringUtils.join(blockContexts, ",");
            byte[] body = StringUtils.utf8Bytes(s);
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
