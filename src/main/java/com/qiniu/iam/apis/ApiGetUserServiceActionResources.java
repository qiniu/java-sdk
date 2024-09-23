package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 列举子账号指定服务操作下的可访问资源
 */
public class ApiGetUserServiceActionResources extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetUserServiceActionResources(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetUserServiceActionResources(Client client, Config config) {
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
        private String userAlias;
    
        /**
         * 资源操作关联的服务
         */
        private String service;
    
        /**
         * 资源操作别名
         */
        private String actionAlias;
    
        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param userAlias 子账号别名 【必须】
         * @param service 资源操作关联的服务 【必须】
         * @param actionAlias 资源操作别名 【必须】
         */
        public Request(String urlPrefix, String userAlias, String service, String actionAlias) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
            this.userAlias = userAlias;
            this.service = service;
            this.actionAlias = actionAlias;
        }
    
        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.userAlias == null) {
                throw new QiniuException(new NullPointerException("userAlias can't empty"));
            }
            if (this.service == null) {
                throw new QiniuException(new NullPointerException("service can't empty"));
            }
            if (this.actionAlias == null) {
                throw new QiniuException(new NullPointerException("actionAlias can't empty"));
            }
    
            super.prepareToRequest();
        }
    
        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/users");
            addPathSegment(this.userAlias);
            addPathSegment("services");
            addPathSegment(this.service);
            addPathSegment("actions");
            addPathSegment(this.actionAlias);
            addPathSegment("resources");
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
         * 返回的 IAM 子账号指定服务操作下的可访问资源列表响应
         */
        private GetIamUserServiceActionResourcesResp data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), GetIamUserServiceActionResourcesResp.class);
        }
    
        /**
         * 响应信息
         *
         * @return GetIamUserServiceActionResourcesResp
         */
        public GetIamUserServiceActionResourcesResp getData() {
            return this.data;
        }
            
        /**
          * 返回的 IAM 子账号指定服务操作下的可访问资源列表信息
          */
        public static final class GetIamUserServiceActionResources {
        
            /**
             * 可用资源
             */
            @SerializedName("allow")
            private String[] allowedResources;
        
            /**
             * 禁用资源
             */
            @SerializedName("deny")
            private String[] deniedResources;
        
            /**
             * 获取变量值
             * 可用资源
             *
             * @return allowedResources
             */
            public String[] getAllowedResources() {
                return this.allowedResources;
            }
        
            /**
             * 获取变量值
             * 禁用资源
             *
             * @return deniedResources
             */
            public String[] getDeniedResources() {
                return this.deniedResources;
            }
        }    
        /**
          * 返回的 IAM 子账号指定服务操作下的可访问资源列表响应
          */
        public static final class GetIamUserServiceActionResourcesResp {
        
            /**
             * IAM 子账号指定服务操作下的可访问资源列表信息
             */
            @SerializedName("data")
            private GetIamUserServiceActionResources data;
        
            /**
             * 获取变量值
             * IAM 子账号指定服务操作下的可访问资源列表信息
             *
             * @return data
             */
            public GetIamUserServiceActionResources getData() {
                return this.data;
            }
        }
    }
}