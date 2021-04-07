package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
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
 * https://developer.qiniu.com/kodo/6368/complete-multipart-upload
 */
public class ApiUploadV2CompleteUpload extends Api {

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
    public static class Request extends Api.Request {
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
         * @param token     请求凭证 【必须】
         * @param uploadId  在服务端申请的 MultipartUpload 任务 id; 服务端处理 completeMultipartUpload 请求成功后，该 UploadId
         *                  就会变成无效，再次请求与该 UploadId 相关操作都会失败。【必须】
         * @param partsInfo 已经上传 Part 列表 （ 包括 PartNumber （ int ）和调用 uploadPart API 服务端返回的 Etag （ string ）） 【必须】
         *                  eg：[{ "etag": "<Etag>", "partNumber": <PartNumber> }, ...]
         *                  用户提交的 Part 列表中，Part 号码可以不连续，但必须是升序;
         */
        public Request(String urlPrefix, String token, String uploadId, List<Map<String, Object>> partsInfo) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
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
         * 注：CustomVarKey 必须增加前缀 x:, 如 {"x:foo", "foo"}
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
         * 注：自定义 meta data 的 key 需要增加前缀 x-qn-meta-, 如 {"x-qn-meta-key", "foo"}
         *
         * @param params meta data
         * @return Request
         */
        public Request setCustomMetaParam(Map<String, Object> params) {
            this.metaDataParam = params;
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

        @Override
        void buildBodyInfo() throws QiniuException {
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
    public static class Response extends Api.Response {

        Response(com.qiniu.http.Response response) throws QiniuException {
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
