package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 查询 IAM 的操作
 */
public class ApiGetActions extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetActions(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetActions(Client client, Config config) {
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
         * 操作对应的服务别名
         */
        private String service = null;

        /**
         * 分页页号，从 1 开始，默认 1
         */
        private Integer page = null;

        /**
         * 分页大小，默认 20，最大 2000
         */
        private Integer pageSize = null;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         */
        public Request(String urlPrefix) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
        }

        /**
         * 设置参数【可选】
         *
         * @param service 操作对应的服务别名
         * @return Request
         */
        public Request setService(String service) {
            this.service = service;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param page 分页页号，从 1 开始，默认 1
         * @return Request
         */
        public Request setPage(Integer page) {
            this.page = page;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param pageSize 分页大小，默认 20，最大 2000
         * @return Request
         */
        public Request setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/actions");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

            if (this.service != null) {
                addQueryPair("service", this.service);
            }
            if (this.page != null) {
                addQueryPair("page", this.page);
            }
            if (this.pageSize != null) {
                addQueryPair("page_size", this.pageSize);
            }

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
         * 返回的接口操作列表响应
         */
        private GetActionsResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), GetActionsResp.class);
        }

        /**
         * 响应信息
         *
         * @return GetActionsResp
         */
        public GetActionsResp getData() {
            return this.data;
        }

        /**
         * 返回的接口操作
         */
        public static final class GetAction {

            /**
             * 记录 ID
             */
            @SerializedName("id")
            private String id;

            /**
             * 接口操作名称
             */
            @SerializedName("name")
            private String name;

            /**
             * 接口操作别名
             */
            @SerializedName("alias")
            private String alias;

            /**
             * 接口操作对应的服务
             */
            @SerializedName("service")
            private String service;

            /**
             * 接口操作权限粒度，0: 操作级，不限制资源，1: 资源级，只能访问特定资源
             */
            @SerializedName("scope")
            private Integer scope;

            /**
             * 接口操作是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;

            /**
             * 接口操作创建时间
             */
            @SerializedName("created_at")
            private String createdAt;

            /**
             * 接口操作上次更新时间
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
             * 接口操作名称
             *
             * @return name
             */
            public String getName() {
                return this.name;
            }

            /**
             * 获取变量值
             * 接口操作别名
             *
             * @return alias
             */
            public String getAlias() {
                return this.alias;
            }

            /**
             * 获取变量值
             * 接口操作对应的服务
             *
             * @return service
             */
            public String getService() {
                return this.service;
            }

            /**
             * 获取变量值
             * 接口操作权限粒度，0: 操作级，不限制资源，1: 资源级，只能访问特定资源
             *
             * @return scope
             */
            public Integer getScope() {
                return this.scope;
            }

            /**
             * 获取变量值
             * 接口操作是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }

            /**
             * 获取变量值
             * 接口操作创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }

            /**
             * 获取变量值
             * 接口操作上次更新时间
             *
             * @return updatedAt
             */
            public String getUpdatedAt() {
                return this.updatedAt;
            }
        }

        /**
         * 返回的接口操作列表信息
         */
        public static final class GetActionsData {

            /**
             * 接口操作数量
             */
            @SerializedName("count")
            private Integer count;

            /**
             * 接口操作列表
             */
            @SerializedName("list")
            private GetAction[] list;

            /**
             * 获取变量值
             * 接口操作数量
             *
             * @return count
             */
            public Integer getCount() {
                return this.count;
            }

            /**
             * 获取变量值
             * 接口操作列表
             *
             * @return list
             */
            public GetAction[] getList() {
                return this.list;
            }
        }

        /**
         * 返回的接口操作列表响应
         */
        public static final class GetActionsResp {

            /**
             * 接口操作信息
             */
            @SerializedName("data")
            private GetActionsData data;

            /**
             * 获取变量值
             * 接口操作信息
             *
             * @return data
             */
            public GetActionsData getData() {
                return this.data;
            }
        }
    }
}