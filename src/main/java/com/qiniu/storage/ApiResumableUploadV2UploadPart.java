package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;

public class ApiResumableUploadV2UploadPart extends Api {

    public ApiResumableUploadV2UploadPart(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        String key;
        String uploadId;
        Integer partIndex;

        public Request(String host, String token, String uploadId, Integer partIndex) {
            super(host);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_PUT);
            this.uploadId = uploadId;
            this.partIndex = partIndex;
        }

        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        public Request setUploadData(byte[] body, int offset, int size, String contentType) {
            super.setBody(body, offset, size, contentType);
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
            if (partIndex == null) {
                throwInvalidRequestParamException("partIndex");
            }

            String bucket = getUploadToken().getBucket();
            addPathSegment("buckets");
            addPathSegment(bucket);
            addPathSegment("objects");
            addPathSegment(ApiUtils.resumeV2EncodeKey(key));
            addPathSegment("uploads");
            addPathSegment(uploadId);
            addPathSegment(partIndex + "");
            super.buildPath();
        }

        @Override
        void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                throwInvalidRequestParamException("block data");
            }
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        public String getMd5() {
            return getStringValueFromDataMap("md5");
        }

        public String getEtag() {
            return getStringValueFromDataMap("etag");
        }
    }
}
