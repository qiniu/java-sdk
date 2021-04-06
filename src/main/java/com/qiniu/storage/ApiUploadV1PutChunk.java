package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class ApiUploadV1PutChunk extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadV1PutChunk(Client client) {
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
        private String blockLastContext;
        private Integer chunkOffset;

        /**
         * 请求构造函数
         *
         * @param urlPrefix        请求 scheme + host 【必须】
         * @param token            请求凭证【必须】
         * @param blockLastContext 该分块上传上次返回的context【必须】
         *                         包括 ApiUploadMakeBlock 返回的 context 和 该接口返回的 context
         * @param chunkOffset      分片在该块中的偏移量
         */
        public Request(String urlPrefix, String token, String blockLastContext, Integer chunkOffset) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
            this.blockLastContext = blockLastContext;
            this.chunkOffset = chunkOffset;
        }

        /**
         * 配置上传块数据【必须】
         * 块数据：在 data 中，从 offset 开始的 size 大小的数据
         *
         * @param data        分片数据源
         * @param offset      分片数据在 data 中的偏移量
         * @param size        分片数据大小
         * @param contentType 分片数据类型
         * @return Request
         */
        public Request setBlockData(byte[] data, int offset, int size, String contentType) {
            super.setBody(data, offset, size, contentType);
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            if (chunkOffset == null) {
                ApiUtils.throwInvalidRequestParamException("chunk offset");
            }
            if (blockLastContext == null) {
                ApiUtils.throwInvalidRequestParamException("block last context");
            }

            addPathSegment("bput");
            addPathSegment(blockLastContext);
            addPathSegment(chunkOffset + "");
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
         * 本次上传成功后的块级上传控制信息，用于后续上传片(bput)及创建文件(mkfile)。本字段是只能被七牛服务器解读使用的不透明字段，
         * 上传端不应修改其内容。每次返回的 ctx 都只对应紧随其后的下一个上传数据片，上传非对应数据片会返回 701 状态码。
         * 例如"ctx":"U1nAe4qJVwz4dYNslBCNNg...E5SEJJQQ=="
         *
         * @return ctx
         */
        public String getCtx() {
            return getStringValueFromDataMap("ctx");
        }

        /**
         * 上传块 sha1，使用URL安全的Base64编码，客户可通过此字段对上传块的完整性进行校验。
         * 例如"checksum":"wQ-csvpBHkZrhihcytio7HXizco="
         *
         * @return checksum
         */
        public String getChecksum() {
            return getStringValueFromDataMap("checksum");
        }

        /**
         * 下一个上传块在切割块中的偏移。
         * 例如"offset":4194304
         *
         * @return offset
         */
        public Long getOffset() {
            return getLongValueFromDataMap("offset");
        }

        /**
         * 后续上传接收地址。
         * 例如"host":"http://upload.qiniup.com"
         *
         * @return host
         */
        public String getHost() {
            return getStringValueFromDataMap("host");
        }

        /**
         * 上传块 crc32，客户可通过此字段对上传块的完整性进行校验。例如"crc32":659036110
         *
         * @return crc32
         */
        public Long getCrc32() {
            return getLongValueFromDataMap("crc32");
        }

        /**
         * ctx 过期时间。
         * 例如"expired_at":1514446175。
         *
         * @return expired_at
         */
        public Long getExpiredAt() {
            return getLongValueFromDataMap("expired_at");
        }
    }
}
