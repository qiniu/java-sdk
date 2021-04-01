package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;

/**
 * 分片上传 v2 版 api: 分块上传数据
 * <p>
 * https://developer.qiniu.com/kodo/6366/upload-part
 */
public class ApiUploadUploadPart extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadUploadPart(Client client) {
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
        private String uploadId;
        private Integer partNumber;

        /**
         * 请求构造函数
         *
         * @param urlPrefix  请求 scheme + host【必须】
         * @param token      请求凭证【必须】
         * @param uploadId   在服务端申请的 MultipartUpload 任务 id; 【必须】
         *                   服务端处理 completeMultipartUpload 请求成功后，该 UploadId
         *                   就会变成无效，再次请求与该 UploadId 相关操作都会失败。
         * @param partNumber 每一个上传的 Part 都有一个标识它的号码
         */
        public Request(String urlPrefix, String token, String uploadId, Integer partNumber) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_PUT);
            this.uploadId = uploadId;
            this.partNumber = partNumber;
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

        /**
         * 配置上传块数据【必须】
         * 块数据：在 data 中，从 offset 开始的 size 大小的数据
         *
         * @param data        块数据源
         * @param offset      块数据在 data 中的偏移量
         * @param size        块数据大小
         * @param contentType 块数据类型
         * @return Request
         */
        public Request setUploadData(byte[] data, int offset, int size, String contentType) {
            super.setBody(data, offset, size, contentType);
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
            if (partNumber == null) {
                ApiUtils.throwInvalidRequestParamException("partNumber");
            }

            String bucket = getUploadToken().getBucket();
            addPathSegment("buckets");
            addPathSegment(bucket);
            addPathSegment("objects");
            addPathSegment(ApiUtils.resumeV2EncodeKey(key));
            addPathSegment("uploads");
            addPathSegment(uploadId);
            addPathSegment(partNumber + "");
            super.buildPath();
        }

        @Override
        void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                ApiUtils.throwInvalidRequestParamException("block data");
            }
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
         * 上传块内容的 md5
         *
         * @return md5
         */
        public String getMd5() {
            return getStringValueFromDataMap("md5");
        }

        /**
         * 上传块内容的 etag ，用来标识块，completeMultipartUpload API 调用的时候作为参数进行文件合成
         *
         * @return etag
         */
        public String getEtag() {
            return getStringValueFromDataMap("etag");
        }
    }
}
