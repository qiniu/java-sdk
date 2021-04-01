package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class ApiResumableUploadV2InitPart extends Api {

    public ApiResumableUploadV2InitPart(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        com.qiniu.http.Response response = client.post(request.getUrl().toString(), request.body, request.bodyOffset, request.bodySize,
                request.getHeader(), request.bodyContentType);
        return new Response(response);
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        String key;

        public Request(String host, String token) {
            super(host);
            setToken(token);
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
