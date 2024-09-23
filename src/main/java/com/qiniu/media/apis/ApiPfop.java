package com.qiniu.media.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;


/**
  * 触发持久化数据处理命令
 */
public class ApiPfop extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiPfop(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiPfop(Client client, Config config) {
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
         * 空间名称
         */
        private String bucketName;
    
        /**
         * 对象名称
         */
        private String objectName;
    
        /**
         * 数据处理命令列表，以 `;` 分隔，可以指定多个数据处理命令
         */
        private String fops;
    
        /**
         * 处理结果通知接收 URL
         */
        private String notifyUrl = null;
    
        /**
         * 强制执行数据处理，设为 `1`，则可强制执行数据处理并覆盖原结果
         */
        private Integer force = null;
    
        /**
         * 任务类型，支持 `0` 表示普通任务，`1` 表示闲时任务
         */
        private Integer type = null;
    
        /**
         * 对列名，仅适用于普通任务
         */
        private String pipeline = null;
    
        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param bucketName 空间名称 【必须】
         * @param objectName 对象名称 【必须】
         * @param fops 数据处理命令列表，以 `;` 分隔，可以指定多个数据处理命令 【必须】
         */
        public Request(String urlPrefix, String bucketName, String objectName, String fops) {
            super(urlPrefix);
            this.setMethod(MethodType.POST);
            this.setAuthType(AuthTypeQiniu);
            this.bucketName = bucketName;
            this.objectName = objectName;
            this.fops = fops;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param notifyUrl 处理结果通知接收 URL
         * @return Request
         */
        public Request setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param force 强制执行数据处理，设为 `1`，则可强制执行数据处理并覆盖原结果
         * @return Request
         */
        public Request setForce(Integer force) {
            this.force = force;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param type 任务类型，支持 `0` 表示普通任务，`1` 表示闲时任务
         * @return Request
         */
        public Request setType(Integer type) {
            this.type = type;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param pipeline 对列名，仅适用于普通任务
         * @return Request
         */
        public Request setPipeline(String pipeline) {
            this.pipeline = pipeline;
            return this;
        }
    
        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.bucketName == null) {
                throw new QiniuException(new NullPointerException("bucketName can't empty"));
            }
            if (this.objectName == null) {
                throw new QiniuException(new NullPointerException("objectName can't empty"));
            }
            if (this.fops == null) {
                throw new QiniuException(new NullPointerException("fops can't empty"));
            }
    
            super.prepareToRequest();
        }
    
        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("pfop/");
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
            StringMap fields = new StringMap();
            fields.put("bucket", this.bucketName);
            fields.put("key", this.objectName);
            fields.put("fops", this.fops);
            if (this.notifyUrl != null) {
                fields.put("notifyURL", this.notifyUrl);
            }
            if (this.force != null) {
                fields.put("force", this.force);
            }
            if (this.type != null) {
                fields.put("type", this.type);
            }
            if (this.pipeline != null) {
                fields.put("pipeline", this.pipeline);
            }
            this.setFormBody(fields);
    
            super.buildBodyInfo();
        }
        
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {
    
        /**
         * 返回的持久化数据处理任务 ID
         */
        private PfopId data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), PfopId.class);
        }
    
        /**
         * 响应信息
         *
         * @return PfopId
         */
        public PfopId getData() {
            return this.data;
        }
            
        /**
          * 返回的持久化数据处理任务 ID
          */
        public static final class PfopId {
        
            /**
             * 持久化数据处理任务 ID
             */
            @SerializedName("persistentId")
            private String persistentId;
        
            /**
             * 获取变量值
             * 持久化数据处理任务 ID
             *
             * @return persistentId
             */
            public String getPersistentId() {
                return this.persistentId;
            }
        }
    }
}