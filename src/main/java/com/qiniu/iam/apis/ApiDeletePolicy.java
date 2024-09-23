package com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;


/**
  * 删除指定的授权策略
 */
public class ApiDeletePolicy extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiDeletePolicy(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiDeletePolicy(Client client, Config config) {
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
            this.setMethod(MethodType.DELETE);
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
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }
        
    }
}