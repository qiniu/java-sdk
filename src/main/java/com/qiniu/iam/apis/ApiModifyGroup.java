package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 更新用户分组信息
 */
public class ApiModifyGroup extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiModifyGroup(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiModifyGroup(Client client, Config config) {
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
         * 修改用户分组参数
         */
        private ModifyGroupParam data;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias     子账号别名 【必须】
         * @param data      修改用户分组参数 【必须】
         */
        public Request(String urlPrefix, String alias, ModifyGroupParam data) {
            super(urlPrefix);
            this.setMethod(MethodType.PATCH);
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
         * 修改用户分组参数
         */
        public static final class ModifyGroupParam {

            /**
             * 新的用户分组别名，由 `A-Za-z0-9` 组成
             */
            @SerializedName("alias")
            private String newAlias;

            /**
             * 用户分组描述
             */
            @SerializedName("description")
            private String description;

            /**
             * 设置变量值
             *
             * @param newAlias 新的用户分组别名，由 `A-Za-z0-9` 组成
             * @return Request
             */
            public ModifyGroupParam setNewAlias(String newAlias) {
                this.newAlias = newAlias;
                return this;
            }

            /**
             * 设置变量值
             *
             * @param description 用户分组描述
             * @return Request
             */
            public ModifyGroupParam setDescription(String description) {
                this.description = description;
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