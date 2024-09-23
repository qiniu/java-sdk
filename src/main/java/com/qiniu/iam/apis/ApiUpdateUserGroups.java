package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 为用户重新分配分组
 */
public class ApiUpdateUserGroups extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiUpdateUserGroups(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiUpdateUserGroups(Client client, Config config) {
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
         * 为用户重新分配分组参数
         */
        private UpdatedIamUserGroupsParam data;
    
        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias 子账号别名 【必须】
         * @param data 为用户重新分配分组参数 【必须】
         */
        public Request(String urlPrefix, String alias, UpdatedIamUserGroupsParam data) {
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
            addPathSegment("iam/v1/users");
            addPathSegment(this.alias);
            addPathSegment("groups");
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
          * 为用户重新分配分组参数
          */
        public static final class UpdatedIamUserGroupsParam {
        
            /**
             * 分组别名集合
             */
            @SerializedName("group_aliases")
            private String[] groupAliases;
        
            /**
             * 设置变量值
             *
             * @param groupAliases 分组别名集合
             * @return Request
             */
            public UpdatedIamUserGroupsParam setGroupAliases(String[] groupAliases) {
                this.groupAliases = groupAliases;
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