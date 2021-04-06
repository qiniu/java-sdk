package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;

/**
 * 分片上传 v2 版 api: 终止上传
 * 该接口根据 UploadId 终止 Multipart Upload 。
 * <p>
 * https://developer.qiniu.com/kodo/6367/abort-multipart-upload
 */
public class ApiUploadV2AbortUpload extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiUploadV2AbortUpload(Client client) {
        super(client);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象 【必须】
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
        private String uploadId;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【必须】
         * @param token     请求凭证 【必须】
         * @param uploadId  在服务端申请的 MultipartUpload 任务 id; 服务端处理 ApiUploadV2CompleteUpload 请求成功后，该 UploadId
         *                  就会变成无效，再次请求与该 UploadId 相关操作都会失败。【必须】
         */
        public Request(String urlPrefix, String token, String uploadId) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_DELETE);
            this.uploadId = uploadId;
        }

        /**
         * 设置资源在七牛云保存的名称【可选】
         * 若未指定，则使用saveKey；若未指定 saveKey，则使用资源内容的 SHA1 值作为资源名。
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
            if (StringUtils.isNullOrEmpty(uploadId)) {
                ApiUtils.throwInvalidRequestParamException("uploadId");
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
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }
    }
}
