package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 新建用户分组
 */
public class ApiCreateGroup extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiCreateGroup(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiCreateGroup(Client client, Config config) {
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
         * 创建用户分组参数
         */
        private CreateGroupParam data;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param data      创建用户分组参数 【必须】
         */
        public Request(String urlPrefix, CreateGroupParam data) {
            super(urlPrefix);
            this.setMethod(MethodType.POST);
            this.setAuthType(AuthTypeQiniu);
            this.data = data;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.data == null) {
                throw new QiniuException(new NullPointerException("data can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/groups");
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
         * 创建用户分组参数
         */
        public static final class CreateGroupParam {

            /**
             * 用户分组别名，由 `A-Za-z0-9` 组成
             */
            @SerializedName("alias")
            private String alias;

            /**
             * 用户分组描述
             */
            @SerializedName("description")
            private String description;

            /**
             * 设置变量值
             *
             * @param alias 用户分组别名，由 `A-Za-z0-9` 组成
             * @return Request
             */
            public CreateGroupParam setAlias(String alias) {
                this.alias = alias;
                return this;
            }

            /**
             * 设置变量值
             *
             * @param description 用户分组描述
             * @return Request
             */
            public CreateGroupParam setDescription(String description) {
                this.description = description;
                return this;
            }
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        /**
         * 返回的用户分组响应
         */
        private CreatedGroupResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), CreatedGroupResp.class);
        }

        /**
         * 响应信息
         *
         * @return CreatedGroupResp
         */
        public CreatedGroupResp getData() {
            return this.data;
        }

        /**
         * 返回的用户分组信息
         */
        public static final class CreatedGroupData {

            /**
             * 记录 ID
             */
            @SerializedName("id")
            private String id;

            /**
             * 根用户 uid
             */
            @SerializedName("root_uid")
            private Integer rootUid;

            /**
             * 用户分组别名
             */
            @SerializedName("alias")
            private String alias;

            /**
             * 用户分组描述
             */
            @SerializedName("description")
            private String description;

            /**
             * 用户分组是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;

            /**
             * 用户分组创建时间
             */
            @SerializedName("created_at")
            private String createdAt;

            /**
             * 用户分组上次更新时间
             */
            @SerializedName("updated_at")
            private String updatedAt;

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
             * 根用户 uid
             *
             * @return rootUid
             */
            public Integer getRootUid() {
                return this.rootUid;
            }

            /**
             * 获取变量值
             * 用户分组别名
             *
             * @return alias
             */
            public String getAlias() {
                return this.alias;
            }

            /**
             * 获取变量值
             * 用户分组描述
             *
             * @return description
             */
            public String getDescription() {
                return this.description;
            }

            /**
             * 获取变量值
             * 用户分组是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }

            /**
             * 获取变量值
             * 用户分组创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }

            /**
             * 获取变量值
             * 用户分组上次更新时间
             *
             * @return updatedAt
             */
            public String getUpdatedAt() {
                return this.updatedAt;
            }
        }

        /**
         * 返回的用户分组响应
         */
        public static final class CreatedGroupResp {

            /**
             * 用户分组信息
             */
            @SerializedName("data")
            private CreatedGroupData data;

            /**
             * 获取变量值
             * 用户分组信息
             *
             * @return data
             */
            public CreatedGroupData getData() {
                return this.data;
            }
        }
    }
}
