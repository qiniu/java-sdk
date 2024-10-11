package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 查询 IAM 的服务列表
 */
public class ApiGetServices extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetServices(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetServices(Client client, Config config) {
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
            addPathSegment("iam/v1/services");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

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
         * 返回的服务列表响应
         */
        private GetServicesResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), GetServicesResp.class);
        }

        /**
         * 响应信息
         *
         * @return GetServicesResp
         */
        public GetServicesResp getData() {
            return this.data;
        }

        /**
         * 返回的服务列表响应
         */
        public static final class GetServicesResp {

            /**
             * 服务列表信息
             */
            @SerializedName("data")
            private String[] data;

            /**
             * 获取变量值
             * 服务列表信息
             *
             * @return data
             */
            public String[] getData() {
                return this.data;
            }
        }
    }
}
