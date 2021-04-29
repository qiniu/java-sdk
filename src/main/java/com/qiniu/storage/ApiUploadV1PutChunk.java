package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;

import java.io.InputStream;

/**
 * 分片上传 v1 版 api: 上传片
 * 上传指定块的一片数据，具体数据量可根据现场环境调整。同一块的每片数据必须串行上传。
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
 * https://developer.qiniu.com/kodo/1251/bput
 */
public class ApiUploadV1PutChunk extends ApiUpload {

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
    public static class Request extends ApiUpload.Request {
        private String blockLastContext;
        private Integer chunkOffset;

        /**
         * 请求构造函数
         *
         * @param urlPrefix        请求 scheme + host 【必须】
         *                         host 参考: https://developer.qiniu.com/kodo/1671/region-endpoint-fq
         *                         注意事项：
         *                         1. token 中签名的 bucket 所在机房必须和 host 的机房一致
         *                         2. 如果不能提前知道机房信息，可调用 {@link ApiQueryRegion} api 获取 region 上传 Hosts
         * @param token            请求凭证【必须】
         * @param blockLastContext 该分块上传上次返回的context【必须】
         *                         包括 ApiUploadMakeBlock 返回的 context 和 该接口返回的 context
         * @param chunkOffset      分片在该块中的偏移量
         */
        public Request(String urlPrefix, String token, String blockLastContext, Integer chunkOffset) {
            super(urlPrefix);
            setToken(token);
            setMethod(MethodType.POST);
            this.blockLastContext = blockLastContext;
            this.chunkOffset = chunkOffset;
        }

        /**
         * 配置块中上传片数据
         * 块数据 size 必须不大于 4M，block 中所有 chunk 的 size 总和必须为 4M, SDK 内部不做 block/chunk size 检测
         * 块数据：在 data 中，从 offset 开始的 size 大小的数据
         * 注：
         * 必须通过 {@link ApiUploadV1PutChunk.Request#setChunkData(byte[], int, int, String)} 或
         * {@link ApiUploadV1PutChunk.Request#setChunkData(InputStream, String, long)} 配置块中上传片数据
         *
         * @param data        分片数据源
         * @param offset      分片数据在 data 中的偏移量
         * @param size        分片数据大小
         * @param contentType 分片数据类型
         * @return Request
         */
        public Request setChunkData(byte[] data, int offset, int size, String contentType) {
            super.setBody(data, offset, size, contentType);
            return this;
        }

        /**
         * 配置块中上传片数据
         * 块数据 size 必须不大于 4M，block 中所有 chunk 的 size 总和必须为 4M, SDK 内部不做 block/chunk size 检测
         * 注：
         * 必须通过 {@link ApiUploadV1PutChunk.Request#setChunkData(byte[], int, int, String)} 或
         * {@link ApiUploadV1PutChunk.Request#setChunkData(InputStream, String, long)} 配置块中上传片数据
         *
         * @param data        块数据源
         * @param contentType 块数据类型
         * @param limitSize   最大读取 data 的大小；data 有多余则被舍弃；data 不足则会上传多有 data；
         *                    如果提前不知道 data 大小，但想上传所有 data，limitSize 设置为 -1 即可；
         * @return Request
         */
        public Request setChunkData(InputStream data, String contentType, long limitSize) {
            super.setBody(data, contentType, limitSize);
            return this;
        }

        @Override
        protected void buildPath() throws QiniuException {
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
        protected void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                ApiUtils.throwInvalidRequestParamException("block chunk data");
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
