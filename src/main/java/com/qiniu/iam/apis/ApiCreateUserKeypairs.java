package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 创建 IAM 子账号密钥
 */
public class ApiCreateUserKeypairs extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiCreateUserKeypairs(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiCreateUserKeypairs(Client client, Config config) {
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
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias     子账号别名 【必须】
         */
        public Request(String urlPrefix, String alias) {
            super(urlPrefix);
            this.setMethod(MethodType.POST);
            this.setAuthType(AuthTypeQiniu);
            this.alias = alias;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.alias == null) {
                throw new QiniuException(new NullPointerException("alias can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/users");
            addPathSegment(this.alias);
            addPathSegment("keypairs");
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

        /**
         * 返回的 IAM 子账号密钥响应
         */
        private CreatedIamUserKeyPairResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), CreatedIamUserKeyPairResp.class);
        }

        /**
         * 响应信息
         *
         * @return CreatedIamUserKeyPairResp
         */
        public CreatedIamUserKeyPairResp getData() {
            return this.data;
        }

        /**
         * 返回的 IAM 子账号密钥信息
         */
        public static final class CreatedIamUserKeyPairData {

            /**
             * 记录 ID
             */
            @SerializedName("id")
            private String id;

            /**
             * IAM 子账号 Access Key
             */
            @SerializedName("access_key")
            private String accessKey;

            /**
             * IAM 子账号 Secret Key
             */
            @SerializedName("secret_key")
            private String secretKey;

            /**
             * 关联用户的记录 ID
             */
            @SerializedName("user_id")
            private String userId;

            /**
             * 密钥创建时间
             */
            @SerializedName("created_at")
            private String createdAt;

            /**
             * 密钥是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;

            /**
             * 获取变量值
             * 记录 ID
             *
             * @return id
             */
            public String getId() {
                return this.id;
            }

            /**
             * 获取变量值
             * IAM 子账号 Access Key
             *
             * @return accessKey
             */
            public String getAccessKey() {
                return this.accessKey;
            }

            /**
             * 获取变量值
             * IAM 子账号 Secret Key
             *
             * @return secretKey
             */
            public String getSecretKey() {
                return this.secretKey;
            }

            /**
             * 获取变量值
             * 关联用户的记录 ID
             *
             * @return userId
             */
            public String getUserId() {
                return this.userId;
            }

            /**
             * 获取变量值
             * 密钥创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }

            /**
             * 获取变量值
             * 密钥是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }
        }

        /**
         * 返回的 IAM 子账号密钥响应
         */
        public static final class CreatedIamUserKeyPairResp {

            /**
             * IAM 子账号密钥信息
             */
            @SerializedName("data")
            private CreatedIamUserKeyPairData data;

            /**
             * 获取变量值
             * IAM 子账号密钥信息
             *
             * @return data
             */
            public CreatedIamUserKeyPairData getData() {
                return this.data;
            }
        }
    }
}