package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.Map;

/**
 * 分片上传 v1 版 api: 创建文件
 * 将上传好的所有数据块按指定顺序合并成一个资源文件。
 * <p>
 * <p>
 * 一个文件被分成多个 block ，一个块可以被分成多个 chunk
 * |----------------------------- file -----------------------------|
 * |------ block ------|------ block ------|------ block ------|...
 * |- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|...
 * |- ctx01 -|- ctx02 -|- ctx10 -|- ctx12 -|- ctx20 -|- ctx22 -|...
 * allBlockCtx = [ctx02, ctx12, ctx22, ...]
 * <p>
 * 上传过程：
 * 1. 把文件分成 block，把块分成 chunk
 * 2. 调用 {@link ApiUploadV1MakeBlock} 创建 block，并附带 block 的第一个 chunk
 * 3. 如果 block 中还有 chunk 未上传，则调用 {@link ApiUploadV1PutChunk} 上传 chunk, 直到该 block 中所有的 chunk 上传完毕
 * 4. 回到【步骤 2】继续上传 block，循环【步骤 2】~【步骤 3】直到所有 block 上传完毕
 * 3. 调用 {@link ApiUploadV1MakeFile} 根据 allBlockCtx 创建文件
 * <p>
 * 注意事项：
 * 1. 除了最后一个 block 外， 其他 block 的大小必须为 4M
 * 2. block 中所有的 chunk size 总和必须和 block size 相同
 * 3. 一个 block 中包含 1个 或多个 chunk
 * 4. 同一个 block 中的块上传需要依赖该块中上一次上传的返回的 ctx, 所以同一个块的上传无法实现并发，
 * 如果想实现并发，可以使一个 block 中仅包含一个 chunk, 也即 chunk size = 4M, make block 接口
 * 不依赖 ctx，可以实现并发；需要注意的一点是 ctx 的顺序必须与 block 在文件中的顺序一致。
 * <p>
 * <p>
 * https://developer.qiniu.com/kodo/1287/mkfile
 */
public class ApiUploadV1MakeFile extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadV1MakeFile(Client client) {
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
        private String fileName;
        private Long fileSize;
        private String fileMimeType;
        private String[] blockContexts;
        private Map<String, Object> params;
        private Map<String, Object> metaDataParam;

        /**
         * 请求构造函数
         *
         * @param urlPrefix     请求 scheme + host 【必须】
         *                      host 参考: https://developer.qiniu.com/kodo/1671/region-endpoint-fq
         *                      注意事项：
         *                      1. token 中签名的 bucket 所在机房必须和 host 的机房一致
         *                      2. 如果不能提前知道机房信息，可调用 {@link ApiQueryRegion} api 获取 region 上传 Hosts
         * @param token         请求凭证【必须】
         * @param fileSize      文件大小，单位字节 【必须】
         * @param blockContexts 所有数据块的 ctx 集合，每个数据块的 ctx 为最后一个数据片上传后得到的 ctx 【必须】
         *                      注: ctx 需按照数据块顺序排列
         */
        public Request(String urlPrefix, String token, Long fileSize, String[] blockContexts) {
            super(urlPrefix);
            setToken(token);
            setMethod(MethodType.POST);
            this.fileSize = fileSize;
            this.blockContexts = blockContexts;
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
         * 用户自定义文件 metadata 信息的 key 和 value【可选】
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
            if (fileSize == null) {
                ApiUtils.throwInvalidRequestParamException("file size");
            }

            addPathSegment("mkfile");
            addPathSegment(fileSize + "");

            if (!StringUtils.isNullOrEmpty(fileMimeType)) {
                addPathSegment("mimeType");
                addPathSegment(UrlSafeBase64.encodeToString(fileMimeType));
            }

            if (!StringUtils.isNullOrEmpty(fileName)) {
                addPathSegment("fname");
                addPathSegment(UrlSafeBase64.encodeToString(fileName));
            }

            if (key != null) {
                addPathSegment("key");
                addPathSegment(UrlSafeBase64.encodeToString(key));
            }

            if (params != null && params.size() > 0) {
                for (String key : params.keySet()) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + params.get(key)));
                }
            }

            if (metaDataParam != null && metaDataParam.size() > 0) {
                for (String key : metaDataParam.keySet()) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + metaDataParam.get(key)));
                }
            }

            super.buildPath();
        }

        @Override
        protected void buildBodyInfo() throws QiniuException {
            if (blockContexts == null) {
                ApiUtils.throwInvalidRequestParamException("blockContexts");
            }

            String s = StringUtils.join(blockContexts, ",");
            byte[] body = StringUtils.utf8Bytes(s);
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
