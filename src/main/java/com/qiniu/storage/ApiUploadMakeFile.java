package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.Map;

/**
 * 分片上传 v1 版 api: 创建文件
 * 将上传好的所有数据块按指定顺序合并成一个资源文件。
 * <p>
 * https://developer.qiniu.com/kodo/1287/mkfile
 */
public class ApiUploadMakeFile extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadMakeFile(Client client) {
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
         * @param token         请求凭证【必须】
         * @param fileSize      文件大小，单位字节 【必须】
         * @param blockContexts 所有数据块的 ctx 集合，每个数据块的 ctx 为最后一个数据片上传后得到的 ctx 【必须】
         *                      注: ctx 需按照数据块顺序排列
         */
        public Request(String urlPrefix, String token, Long fileSize, String[] blockContexts) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
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
         * 用户自定义文件 metadata 信息的 key 和 value【可选】
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
        void buildBodyInfo() throws QiniuException {
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
