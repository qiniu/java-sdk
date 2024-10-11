package com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;


/**
 * 禁用 IAM 子账号密钥
 */
public class ApiDisableUserKeypair extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiDisableUserKeypair(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiDisableUserKeypair(Client client, Config config) {
        super(client, config);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestWithInterceptor(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {

        /**
         * 子账号别名
         */
        private String alias;

        /**
         * IAM 子账号 Access Key
         */
        private String accessKey;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias     子账号别名 【必须】
         * @param accessKey IAM 子账号 Access Key 【必须】
         */
        public Request(String urlPrefix, String alias, String accessKey) {
            super(urlPrefix);
            this.setMethod(MethodType.POST);
            this.setAuthType(AuthTypeQiniu);
            this.alias = alias;
            this.accessKey = accessKey;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.alias == null) {
                throw new QiniuException(new NullPointerException("alias can't empty"));
            }
            if (this.accessKey == null) {
                throw new QiniuException(new NullPointerException("accessKey can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/users");
            addPathSegment(this.alias);
            addPathSegment("keypairs");
            addPathSegment(this.accessKey);
            addPathSegment("disable");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

            super.buildQuery();
        }

        @Override
        protected void buildHeader() throws QiniuException {

            super.buildHeader();
        }

        @Override
        protected void buildBodyInfo() throws QiniuException {

            super.buildBodyInfo();
        }

    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

    }
}
