package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;

/**
 * 分片上传 v2 版 api: 初始化任务
 * 使用 Multipart Upload 方式上传数据前，必须先调用 API 来获取一个全局唯一的 UploadId ，后续的块数据通过 uploadPart API 上传，
 * 整个文件完成 completeMultipartUpload API ，已经上传块的删除 abortMultipartUpload API 都依赖该 UploadId 。
 * <p>
 * <p>
 * 一个文件被分成多个 part，上传所有的 part，然后在七牛云根据 part 信息合成文件
 * |----------------------------- file -----------------------------|
 * |------ part ------|------ part ------|------ part ------|...
 * |----- etag01 -----|----- etag02 -----|----- etag03 -----|...
 * allBlockCtx = [{"partNumber":1, "etag", etag01}, {"partNumber":2, "etag", etag02}, {"partNumber":3, "etag", etag03}, ...]
 * <p>
 * 上传过程：
 * 1. 调用 {@link ApiUploadV2InitUpload} api 创建一个 upload 任务，获取 uploadId {@link ApiUploadV2InitUpload.Response#getUploadId()}
 * 2. 重复调用 {@link ApiUploadV2UploadPart} api 直到文件所有的 part 均上传完毕, part 的大小可以不相同
 * 3. 调用 {@link ApiUploadV2CompleteUpload} api 组装 api
 * 选用接口：
 * 1. {@link ApiUploadV2ListParts} 列举已上传的 part 信息
 * 2. {@link ApiUploadV2AbortUpload} 终止上传
 * <p>
 * 注意事项：
 * 1. partNumber 范围是 1 ~ 10000
 * 2. 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB
 * 3. 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖
 * 4. {@link ApiUploadV2InitUpload}、{@link ApiUploadV2UploadPart}、{@link ApiUploadV2CompleteUpload}、{@link ApiUploadV2ListParts}、
 * {@link ApiUploadV2AbortUpload} 分片 V2 API的 key 需要统一（要么有设置且相同，要么均不设置）
 * <p>
 * <p>
 * https://developer.qiniu.com/kodo/6365/initialize-multipartupload
 */
public class ApiUploadV2InitUpload extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadV2InitUpload(Client client) {
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
    public static class Request extends ApiUpload.Request {
        private String key;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host【必须】
         *                  host 参考: https://developer.qiniu.com/kodo/1671/region-endpoint-fq
         *                  注意事项：
         *                  1. token 中签名的 bucket 所在机房必须和 host 的机房一致
         *                  2. 如果不能提前知道机房信息，可调用 {@link ApiQueryRegion} api 获取 region 上传 Hosts
         * @param token     请求凭证【必须】
         */
        public Request(String urlPrefix, String token) {
            super(urlPrefix);
            setToken(token);
            setMethod(MethodType.POST);
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
        protected void buildPath() throws QiniuException {
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
    public static class Response extends ApiUpload.Response {

        protected Response(com.qiniu.http.Response response) throws QiniuException {
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
