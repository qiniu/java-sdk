package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.util.StringUtils;

import java.io.InputStream;

/**
 * 分片上传 v2 版 api: 分块上传数据
 * 初始化一个 Multipart Upload 任务之后，可以根据指定的 EncodedObjectName 和 UploadId 来分 Part 上传数据。
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
 * https://developer.qiniu.com/kodo/6366/upload-part
 */
public class ApiUploadV2UploadPart extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadV2UploadPart(Client client) {
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
        private String uploadId;
        private Integer partNumber;

        /**
         * 请求构造函数
         *
         * @param urlPrefix  请求 scheme + host【必须】
         *                   host 参考: https://developer.qiniu.com/kodo/1671/region-endpoint-fq
         *                   注意事项：
         *                   1. token 中签名的 bucket 所在机房必须和 host 的机房一致
         *                   2. 如果不能提前知道机房信息，可调用 {@link ApiQueryRegion} api 获取 region 上传 Hosts
         * @param token      请求凭证【必须】
         * @param uploadId   在服务端申请的 MultipartUpload 任务 id; 【必须】
         *                   服务端处理 completeMultipartUpload 请求成功后，该 UploadId
         *                   就会变成无效，再次请求与该 UploadId 相关操作都会失败。
         * @param partNumber 每一个上传的 Part 都有一个标识它的号码
         *                   注：1. partNumber 范围是 1 ~ 10000
         *                   2. 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB
         *                   3. 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖
         */
        public Request(String urlPrefix, String token, String uploadId, Integer partNumber) {
            super(urlPrefix);
            setToken(token);
            setMethod(MethodType.PUT);
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
         * 配置上传块数据
         * 块数据：在 data 中，从 offset 开始的 size 大小的数据
         * 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB
         * 注：
         * 必须通过 {@link ApiUploadV2UploadPart.Request#setUploadData(byte[], int, int, String)} 或
         * {@link ApiUploadV2UploadPart.Request#setUploadData(InputStream, String, long)} 配置上传块数据
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

        /**
         * 配置上传块数据
         * 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB
         * 注：
         * 必须通过 {@link ApiUploadV2UploadPart.Request#setUploadData(byte[], int, int, String)} 或
         * {@link ApiUploadV2UploadPart.Request#setUploadData(InputStream, String, long)} 配置上传块数据
         *
         * @param data        块数据源
         * @param contentType 块数据类型
         * @param limitSize   最大读取 data 的大小；data 有多余则被舍弃；data 不足则会上传多有 data；
         *                    如果提前不知道 data 大小，但想上传所有 data，limitSize 设置为 -1 即可；
         * @return Request
         */
        public Request setUploadData(InputStream data, String contentType, long limitSize) {
            super.setBody(data, contentType, limitSize);
            return this;
        }

        @Override
        protected void buildPath() throws QiniuException {
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
        protected void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                ApiUtils.throwInvalidRequestParamException("upload data");
            }
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
