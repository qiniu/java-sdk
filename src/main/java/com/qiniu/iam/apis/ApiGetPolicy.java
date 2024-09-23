package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 查询指定授权策略详情
 */
public class ApiGetPolicy extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetPolicy(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetPolicy(Client client, Config config) {
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
         * 授权策略别名
         */
        private String alias;
    
        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias 授权策略别名 【必须】
         */
        public Request(String urlPrefix, String alias) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
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
            addPathSegment("iam/v1/policies");
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
    
            super.buildBodyInfo();
        }
        
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {
    
        /**
         * 返回的授权策略响应
         */
        private GetPolicyResp data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), GetPolicyResp.class);
        }
    
        /**
         * 响应信息
         *
         * @return GetPolicyResp
         */
        public GetPolicyResp getData() {
            return this.data;
        }
            
        /**
          * 授权策略规则
          */
        public static final class Statement {
        
            /**
             * 授权策略规则的操作集合
             */
            @SerializedName("action")
            private String[] actions;
        
            /**
             * 授权策略规则的资源集合
             */
            @SerializedName("resource")
            private String[] resources;
        
            /**
             * 授权策略规则的生效类型，允许访问或拒绝访问
             */
            @SerializedName("effect")
            private String effect;
        
            /**
             * 获取变量值
             * 授权策略规则的操作集合
             *
             * @return actions
             */
            public String[] getActions() {
                return this.actions;
            }
        
            /**
             * 获取变量值
             * 授权策略规则的资源集合
             *
             * @return resources
             */
            public String[] getResources() {
                return this.resources;
            }
        
            /**
             * 获取变量值
             * 授权策略规则的生效类型，允许访问或拒绝访问
             *
             * @return effect
             */
            public String getEffect() {
                return this.effect;
            }
        }    
        /**
          * 返回的授权策略信息
          */
        public static final class GetPolicyData {
        
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
             * 授权策略别名
             */
            @SerializedName("alias")
            private String alias;
        
            /**
             * 授权策略描述
             */
            @SerializedName("description")
            private String description;
        
            /**
             * 授权策略是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;
        
            /**
             * 授权策略创建时间
             */
            @SerializedName("created_at")
            private String createdAt;
        
            /**
             * 授权策略上次更新时间
             */
            @SerializedName("updated_at")
            private String updatedAt;
        
            /**
             * 授权策略规则集合
             */
            @SerializedName("statement")
            private Statement[] statement;
        
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
             * 授权策略别名
             *
             * @return alias
             */
            public String getAlias() {
                return this.alias;
            }
        
            /**
             * 获取变量值
             * 授权策略描述
             *
             * @return description
             */
            public String getDescription() {
                return this.description;
            }
        
            /**
             * 获取变量值
             * 授权策略是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }
        
            /**
             * 获取变量值
             * 授权策略创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }
        
            /**
             * 获取变量值
             * 授权策略上次更新时间
             *
             * @return updatedAt
             */
            public String getUpdatedAt() {
                return this.updatedAt;
            }
        
            /**
             * 获取变量值
             * 授权策略规则集合
             *
             * @return statement
             */
            public Statement[] getStatement() {
                return this.statement;
            }
        }    
        /**
          * 返回的授权策略响应
          */
        public static final class GetPolicyResp {
        
            /**
             * 授权策略信息
             */
            @SerializedName("data")
            private GetPolicyData data;
        
            /**
             * 获取变量值
             * 授权策略信息
             *
             * @return data
             */
            public GetPolicyData getData() {
                return this.data;
            }
        }
    }
}