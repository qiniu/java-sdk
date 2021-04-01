package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class ApiUploadInitPart extends Api {

    public ApiUploadInitPart(Client client) {
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

        public Request(String host, String token) {
            super(host);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
        }

        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            UploadToken token = getUploadToken();
            if (token == null || !token.isValid()) {
                throwInvalidRequestParamException("token");
            }

            String bucket = getUploadToken().getBucket();
            addPathSegment("buckets");
            addPathSegment(bucket);
            addPathSegment("objects");
            addPathSegment(ApiUtils.resumeV2EncodeKey(key));
            addPathSegment("uploads");
            super.buildPath();
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        public String getUploadId() {
            return getStringValueFromDataMap("uploadId");
        }

        public Long getExpireAt() {
            return getLongValueFromDataMap("expireAt");
        }
    }
}
