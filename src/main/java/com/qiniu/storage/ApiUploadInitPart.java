package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

/**
 * 分片上传 v2 版 api: 初始化任务
 * <p>
 * https://developer.qiniu.com/kodo/6365/initialize-multipartupload
 */
public class ApiUploadInitPart extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadInitPart(Client client) {
        super(client);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        private String key;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host【必须】
         * @param token     请求凭证【必须】
         */
        public Request(String urlPrefix, String token) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
        }

        /**
         * 设置资源在七牛云保存的名称【可选】
         *
         * @param key 保存的名称
         * @return Request
         */
        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            UploadToken token = getUploadToken();
            if (token == null || !token.isValid()) {
                ApiUtils.throwInvalidRequestParamException("token");
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

        Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        /**
         * 初始化文件生成的 id
         *
         * @return uploadId
         */
        public String getUploadId() {
            return getStringValueFromDataMap("uploadId");
        }

        /**
         * UploadId 的过期时间 Unix 时间戳，过期之后 UploadId 不可用，固定 7 天有效期
         *
         * @return expireAt
         */
        public Long getExpireAt() {
            return getLongValueFromDataMap("expireAt");
        }
    }
}
