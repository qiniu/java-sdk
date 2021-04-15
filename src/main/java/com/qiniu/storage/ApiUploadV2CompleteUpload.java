package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分片上传 v2 版 api: 完成文件上传
 * 在将所有数据 Part 都上传完成后，必须调用 ApiUploadV2CompleteUpload API 来完成整个文件的 Multipart Upload。用户需要提供有效数据的
 * Part 列表（ 包括 PartNumber 和调用 uploadPart API 服务端返回的 Etag ）。服务端收到用户提交的 Part 列表后，会逐一验证每个数据
 * Part 的有效性。当所有的数据 Part 验证通过后，会把这些数据 Part 组合成一个完整的 Object。
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
 * https://developer.qiniu.com/kodo/6368/complete-multipart-upload
 */
public class ApiUploadV2CompleteUpload extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiUploadV2CompleteUpload(Client client) {
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
    public static class Request extends ApiUpload.Request {
        public static final String PART_ETG = "etag";
        public static final String PART_NUMBER = "partNumber";

        private String key;
        private String fileName;
        private String fileMimeType;
        private String uploadId;
        private List<Map<String, Object>> partsInfo;
        private Map<String, Object> params;
        private Map<String, Object> metaDataParam;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【必须】
         *                  host 参考: https://developer.qiniu.com/kodo/1671/region-endpoint-fq
         *                  注意事项：
         *                  1. token 中签名的 bucket 所在机房必须和 host 的机房一致
         *                  2. 如果不能提前知道机房信息，可调用 {@link ApiQueryRegion} api 获取 region 上传 Hosts
         * @param token     请求凭证 【必须】
         * @param uploadId  在服务端申请的 MultipartUpload 任务 id; 服务端处理 completeMultipartUpload 请求成功后，该 UploadId
         *                  就会变成无效，再次请求与该 UploadId 相关操作都会失败。【必须】
         * @param partsInfo 已经上传 Part 列表【必须】
         *                  包括 PartNumber(int) 和调用 uploadPart API 服务端返回的 Etag(string)
         *                  eg：[{ "etag": "<Etag>", "partNumber": <PartNumber> }, ...]
         *                  注意事项：
         *                  1. 每一个上传的 Part 都有一个标识它的号码 PartNumber，范围是 1 - 10000，单个 Part大小范围 1 MB - 1 GB。
         *                  Multipart Upload 要求除最后一个 Part 以外，其他的 Part 大小都要大于等于 1 MB。因不确定是否为最后一个 Part，
         *                  uploadPart API 并不会立即校验上传 Part 的大小，当 completeMultipartUpload API 调用的时候才会校验。
         *                  2. 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖。
         *                  3. 用户提交的 Part 列表中，Part 号码可以不连续，但必须是升序;
         */
        public Request(String urlPrefix, String token, String uploadId, List<Map<String, Object>> partsInfo) {
            super(urlPrefix);
            setToken(token);
            setMethod(MethodType.POST);
            this.uploadId = uploadId;
            this.partsInfo = partsInfo;
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
         * 设置资源的文件名【可选】
         * 若未指定，则魔法变量中无法使用fname, ext, fprefix。
         *
         * @param fileName 保存的名称
         * @return Request
         */
        public Request setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * 设置资源的类型【可选】
         * 若未指定，则根据文件内容自动检测 mimeType。
         *
         * @param fileMimeType 资源的类型
         * @return Request
         */
        public Request setFileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }

        /**
         * 自定义变量【可选】
         * CustomVarKey 和 CustomVarValue 都是 string
         * 注：CustomVarKey 必须增加前缀 x:, 如 {"x:foo", "foo"}, SDK 内部不会检查 key 的格式
         * https://developer.qiniu.com/kodo/1235/vars
         *
         * @param params 自定义变量
         * @return Request
         */
        public Request setCustomParam(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        /**
         * 用户自定义文件 metadata 信息的 key 和 value 【可选】
         * 可以设置多个，MetaKey 和 MetaValue 都是 string，其中 可以由字母、数字、
         * 下划线、减号组成，且长度小于等于 50，单个文件 MetaKey 和 Metavalue 总和大小不能超过 1024 字节
         * 注：自定义 meta data 的 key 需要增加前缀 x-qn-meta-, 如 {"x-qn-meta-key", "foo"}, SDK 内部不会检查 key 的格式
         *
         * @param params meta data
         * @return Request
         */
        public Request setCustomMetaParam(Map<String, Object> params) {
            this.metaDataParam = params;
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

            String bucket = token.getBucket();
            addPathSegment("buckets");
            addPathSegment(bucket);
            addPathSegment("objects");
            addPathSegment(ApiUtils.resumeV2EncodeKey(key));
            addPathSegment("uploads");
            addPathSegment(uploadId);
            super.buildPath();
        }

        @Override
        protected void buildBodyInfo() throws QiniuException {
            if (partsInfo == null) {
                ApiUtils.throwInvalidRequestParamException("partInfo");
            }

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("parts", partsInfo);

            if (fileName != null) {
                bodyMap.put("fname", fileName);
            }
            if (fileMimeType != null) {
                bodyMap.put("mimeType", fileMimeType);
            }
            if (params != null && params.size() > 0) {
                bodyMap.put("customVars", params);
            }
            if (metaDataParam != null && metaDataParam.size() > 0) {
                bodyMap.put("metaData", metaDataParam);
            }

            String bodyString = Json.encode(bodyMap);
            byte[] body = bodyString.getBytes();
            setBody(body, 0, body.length, null);
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
         * 获取资源名称
         *
         * @return 资源名称
         */
        public String getKey() {
            return getStringValueFromDataMap("key");
        }

        /**
         * 获取目标资源的 hash 值，可用于 Etag 头部
         *
         * @return 目标资源的 hash 值
         */
        public String getHash() {
            return getStringValueFromDataMap("hash");
        }
    }
}
