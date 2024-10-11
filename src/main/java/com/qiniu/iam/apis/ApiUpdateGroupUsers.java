package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 为用户分组中重新分配 IAM 子账号
 */
public class ApiUpdateGroupUsers extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiUpdateGroupUsers(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiUpdateGroupUsers(Client client, Config config) {
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
         * 用户分组别名
         */
        private String alias;

        /**
         * 为用户分组重新分配 IAM 子账号参数
         */
        private UpdatedGroupIamUsersParam data;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias     用户分组别名 【必须】
         * @param data      为用户分组重新分配 IAM 子账号参数 【必须】
         */
        public Request(String urlPrefix, String alias, UpdatedGroupIamUsersParam data) {
            super(urlPrefix);
            this.setMethod(MethodType.POST);
            this.setAuthType(AuthTypeQiniu);
            this.alias = alias;
            this.data = data;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.alias == null) {
                throw new QiniuException(new NullPointerException("alias can't empty"));
            }
            if (this.data == null) {
                throw new QiniuException(new NullPointerException("data can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/groups");
            addPathSegment(this.alias);
            addPathSegment("users");
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
            byte[] body = Json.encode(this.data).getBytes(Constants.UTF_8);
            this.setBody(body, 0, body.length, Client.JsonMime);

            super.buildBodyInfo();
        }

        /**
         * 为用户分组重新分配 IAM 子账号参数
         */
        public static final class UpdatedGroupIamUsersParam {

            /**
             * IAM 子账号别名集合
             */
            @SerializedName("user_aliases")
            private String[] userAliases;

            /**
             * 设置变量值
             *
             * @param userAliases IAM 子账号别名集合
             * @return Request
             */
            public UpdatedGroupIamUsersParam setUserAliases(String[] userAliases) {
                this.userAliases = userAliases;
                return this;
            }
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
