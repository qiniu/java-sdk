package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;

import java.util.List;

/**
 * 分片上传 v2 版 api: 列举已上传分片
 * API 可列举出指定 UploadId 所属任务所有已经上传成功 Part。
 * <p>
 * https://developer.qiniu.com/kodo/6858/listparts
 */
public class ApiUploadV2ListParts extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiUploadV2ListParts(Client client) {
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
        private Integer maxParts;
        private Integer partNumberMarker;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【必须】
         * @param token     请求凭证 【必须】
         * @param uploadId  在服务端申请的 MultipartUpload 任务 id; 服务端处理 completeMultipartUpload 请求成功后，该 UploadId
         *                  就会变成无效，再次请求与该 UploadId 相关操作都会失败。【必须】
         */
        public Request(String urlPrefix, String token, String uploadId) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_GET);
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

        /**
         * 设置响应中的最大 Part 数目。【可选】
         * 默认值 ：1,000，最大值 ：1,000
         *
         * @param maxParts 响应中的最大 Part 数目
         */
        public void setMaxParts(Integer maxParts) {
            this.maxParts = maxParts;
        }

        /**
         * 指定列举的起始位置，只有 PartNumber 值大于该参数的 Part 会被列出。【可选】
         * 默认值 ：无
         *
         * @param partNumberMarker 指定列举的起始位置
         */
        public void setPartNumberMarker(Integer partNumberMarker) {
            this.partNumberMarker = partNumberMarker;
        }

        @Override
        void buildQuery() throws QiniuException {
            if (maxParts != null) {
                addQueryPair("max-parts", maxParts + "");
            }
            if (partNumberMarker != null) {
                addQueryPair("part-number-mark", partNumberMarker + "");
            }
            super.buildQuery();
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

        /**
         * 获取初始化文件生成的 id
         *
         * @return 初始化文件生成的 id
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

        /**
         * 获取下次继续列举的起始位置
         * 表示列举结束，没有更多分片
         *
         * @return 下次继续列举的起始位置
         */
        public Integer getPartNumberMarker() {
            return getIntegerValueFromDataMap("partNumberMarker");
        }

        /**
         * 获取目标资源的 hash 值，可用于 Etag 头部
         *
         * @return 目标资源的 hash 值
         */
        public List getParts() {
            Object value = getValueFromDataMap("parts");
            if (value instanceof List) {
                return (List) value;
            } else {
                return null;
            }
        }
    }
}
