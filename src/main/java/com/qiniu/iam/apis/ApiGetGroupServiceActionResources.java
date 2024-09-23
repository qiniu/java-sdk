package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 列举用户分组指定服务操作下的可访问资源
 */
public class ApiGetGroupServiceActionResources extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetGroupServiceActionResources(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetGroupServiceActionResources(Client client, Config config) {
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
        private String groupAlias;
    
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
         * @param groupAlias 用户分组别名 【必须】
         * @param service 资源操作关联的服务 【必须】
         * @param actionAlias 资源操作别名 【必须】
         */
        public Request(String urlPrefix, String groupAlias, String service, String actionAlias) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
            this.groupAlias = groupAlias;
            this.service = service;
            this.actionAlias = actionAlias;
        }
    
        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.groupAlias == null) {
                throw new QiniuException(new NullPointerException("groupAlias can't empty"));
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
            addPathSegment("iam/v1/groups");
            addPathSegment(this.groupAlias);
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
         * 返回的用户分组指定服务操作下的可访问资源列表响应
         */
        private GetGroupServiceActionResourcesResp data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), GetGroupServiceActionResourcesResp.class);
        }
    
        /**
         * 响应信息
         *
         * @return GetGroupServiceActionResourcesResp
         */
        public GetGroupServiceActionResourcesResp getData() {
            return this.data;
        }
            
        /**
          * 返回的用户分组指定服务操作下的可访问资源列表信息
          */
        public static final class GetGroupServiceActionResources {
        
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
          * 返回的用户分组指定服务操作下的可访问资源列表响应
          */
        public static final class GetGroupServiceActionResourcesResp {
        
            /**
             * 用户分组指定服务操作下的可访问资源列表信息
             */
            @SerializedName("data")
            private GetGroupServiceActionResources data;
        
            /**
             * 获取变量值
             * 用户分组指定服务操作下的可访问资源列表信息
             *
             * @return data
             */
            public GetGroupServiceActionResources getData() {
                return this.data;
            }
        }
    }
}