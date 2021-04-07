package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;

import java.util.List;

public class ApiQueryRegion extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiQueryRegion(Client client) {
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
        private static final String DEFAULT_URL_PREFIX = "https://uc.qbox.me";

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则使用默认，默认：Request.DEFAULT_URL_PREFIX
         * @param token     请求凭证 【必须】
         */
        public Request(String urlPrefix, String token) {
            super(StringUtils.isNullOrEmpty(urlPrefix) ? DEFAULT_URL_PREFIX : urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_GET);
        }

        @Override
        void buildQuery() throws QiniuException {
            UploadToken token = getUploadToken();
            addQueryPair("ak", token.getAccessKey());
            addQueryPair("bucket", token.getBucket());
            super.buildQuery();
        }

        @Override
        public void buildPath() throws QiniuException {
            addPathSegment("v2");
            addPathSegment("query");
            super.buildPath();
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends ApiUpload.Response {

        Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        /**
         * 获取目标资源的 hash 值，可用于 Etag 头部
         * eg:
         * [
         * { "size": 2097152, "etag": "FqlKj-XMsZumHEwIc9OR6YeYL7vT", "partNumber": 1, "putTime": 1590725018},
         * { "size": 2097152, "etag": "FqvtxHpe3j-rEzkImMUWDsmvu27D", "partNumber": 2, "putTime": 1590725019}
         * ]
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
