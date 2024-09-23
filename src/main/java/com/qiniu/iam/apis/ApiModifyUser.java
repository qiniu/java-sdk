package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 修改 IAM 子账号
 */
public class ApiModifyUser extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiModifyUser(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiModifyUser(Client client, Config config) {
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
         * 修改 IAM 子账号参数
         */
        private ModifiedIamUserParam data;
    
        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param alias 子账号别名 【必须】
         * @param data 修改 IAM 子账号参数 【必须】
         */
        public Request(String urlPrefix, String alias, ModifiedIamUserParam data) {
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
            addPathSegment("iam/v1/users");
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
          * 修改 IAM 子账号参数
          */
        public static final class ModifiedIamUserParam {
        
            /**
             * 子账号是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;
        
            /**
             * 子账号密码
             */
            @SerializedName("password")
            private String password;
        
            /**
             * 设置变量值
             *
             * @param enabled 子账号是否启用
             * @return Request
             */
            public ModifiedIamUserParam setEnabled(Boolean enabled) {
                this.enabled = enabled;
                return this;
            }
        
            /**
             * 设置变量值
             *
             * @param password 子账号密码
             * @return Request
             */
            public ModifiedIamUserParam setPassword(String password) {
                this.password = password;
                return this;
            }
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {
    
        /**
         * 返回的 IAM 子账号响应
         */
        private ModifiedIamUserResp data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), ModifiedIamUserResp.class);
        }
    
        /**
         * 响应信息
         *
         * @return ModifiedIamUserResp
         */
        public ModifiedIamUserResp getData() {
            return this.data;
        }
            
        /**
          * 返回的 IAM 子账号信息
          */
        public static final class ModifiedIamUserData {
        
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
             * 子账号 uid
             */
            @SerializedName("iuid")
            private Integer iuid;
        
            /**
             * 子账号别名
             */
            @SerializedName("alias")
            private String alias;
        
            /**
             * 子账号创建时间
             */
            @SerializedName("created_at")
            private String createdAt;
        
            /**
             * 子账号上次更新时间
             */
            @SerializedName("updated_at")
            private String updatedAt;
        
            /**
             * 子账号上次更新时间
             */
            @SerializedName("last_login_time")
            private String lastLoginTime;
        
            /**
             * 子账号是否启用
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
             * 根用户 uid
             *
             * @return rootUid
             */
            public Integer getRootUid() {
                return this.rootUid;
            }
        
            /**
             * 获取变量值
             * 子账号 uid
             *
             * @return iuid
             */
            public Integer getIuid() {
                return this.iuid;
            }
        
            /**
             * 获取变量值
             * 子账号别名
             *
             * @return alias
             */
            public String getAlias() {
                return this.alias;
            }
        
            /**
             * 获取变量值
             * 子账号创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }
        
            /**
             * 获取变量值
             * 子账号上次更新时间
             *
             * @return updatedAt
             */
            public String getUpdatedAt() {
                return this.updatedAt;
            }
        
            /**
             * 获取变量值
             * 子账号上次更新时间
             *
             * @return lastLoginTime
             */
            public String getLastLoginTime() {
                return this.lastLoginTime;
            }
        
            /**
             * 获取变量值
             * 子账号是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }
        }    
        /**
          * 返回的 IAM 子账号响应
          */
        public static final class ModifiedIamUserResp {
        
            /**
             * IAM 子账号信息
             */
            @SerializedName("data")
            private ModifiedIamUserData data;
        
            /**
             * 获取变量值
             * IAM 子账号信息
             *
             * @return data
             */
            public ModifiedIamUserData getData() {
                return this.data;
            }
        }
    }
}