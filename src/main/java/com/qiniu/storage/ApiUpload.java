package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;

public class ApiUpload extends Api {

    public ApiUpload(Client client) {
        super(client);
    }

    /**
     * upload api 请求基类
     */
    public static class Request extends Api.Request {

        /**
         * 上传凭证
         */
        private String token;

        /**
         * 构造请求对象
         *
         * @param urlPrefix 请求的 urlPrefix， scheme + host
         */
        public Request(String urlPrefix) {
            super(urlPrefix);
        }

        /**
         * 获取请求头信息
         *
         * @return 请求头信息
         */
        public StringMap getHeader() throws QiniuException {
            if (token == null || !getUploadToken().isValid()) {
                ApiUtils.throwInvalidRequestParamException("token");
            }

            addHeaderField("Authorization", "UpToken " + token);
            addHeaderField("Host", getHost());
            return super.getHeader();
        }

        /**
         * 设置上传凭证
         *
         * @param token 上传凭证
         */
        protected void setToken(String token) {
            this.token = token;
        }

        /**
         * 获取上传凭证
         *
         * @return 上传凭证
         */
        protected UploadToken getUploadToken() throws QiniuException {
            return new UploadToken(token);
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (token == null || !getUploadToken().isValid()) {
                ApiUtils.throwInvalidRequestParamException("token");
            }

            super.prepareToRequest();
        }
    }


    /**
     * api 响应基类
     */
    public static class Response extends Api.Response {

        /**
         * 构建 Response
         *
         * @param response com.qiniu.http.Response
         * @throws QiniuException 解析 data 异常
         */
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }
    }
}
