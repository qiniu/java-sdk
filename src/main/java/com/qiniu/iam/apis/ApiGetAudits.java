package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
  * 查询审计日志列表
 */
public class ApiGetAudits extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetAudits(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetAudits(Client client, Config config) {
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
         * IAM 子账号 UID
         */
        private Integer iuid = null;
    
        /**
         * 操作对应的服务别名
         */
        private String service = null;
    
        /**
         * 操作别名
         */
        private String action = null;
    
        /**
         * 操作别名
         */
        private String resource = null;
    
        /**
         * 操作开始时间
         */
        private String startTime = null;
    
        /**
         * 操作截止时间
         */
        private String endTime = null;
    
        /**
         * 下页标记
         */
        private String marker = null;
    
        /**
         * 分页大小，默认 20，最大 2000
         */
        private Integer limit = null;
    
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
         * @param iuid IAM 子账号 UID
         * @return Request
         */
        public Request setIuid(Integer iuid) {
            this.iuid = iuid;
            return this;
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
         * @param action 操作别名
         * @return Request
         */
        public Request setAction(String action) {
            this.action = action;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param resource 操作别名
         * @return Request
         */
        public Request setResource(String resource) {
            this.resource = resource;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param startTime 操作开始时间
         * @return Request
         */
        public Request setStartTime(String startTime) {
            this.startTime = startTime;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param endTime 操作截止时间
         * @return Request
         */
        public Request setEndTime(String endTime) {
            this.endTime = endTime;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param marker 下页标记
         * @return Request
         */
        public Request setMarker(String marker) {
            this.marker = marker;
            return this;
        }
    
        /**
         * 设置参数【可选】
         *
         * @param limit 分页大小，默认 20，最大 2000
         * @return Request
         */
        public Request setLimit(Integer limit) {
            this.limit = limit;
            return this;
        }
    
        @Override
        protected void prepareToRequest() throws QiniuException {
    
            super.prepareToRequest();
        }
    
        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/audits");
            super.buildPath();
        }
    
        @Override
        protected void buildQuery() throws QiniuException {
                
            if (this.iuid != null) {
                addQueryPair("iuid", this.iuid);
            }
            if (this.service != null) {
                addQueryPair("service", this.service);
            }
            if (this.action != null) {
                addQueryPair("action", this.action);
            }
            if (this.resource != null) {
                addQueryPair("resource", this.resource);
            }
            if (this.startTime != null) {
                addQueryPair("start_time", this.startTime);
            }
            if (this.endTime != null) {
                addQueryPair("end_time", this.endTime);
            }
            if (this.marker != null) {
                addQueryPair("marker", this.marker);
            }
            if (this.limit != null) {
                addQueryPair("limit", this.limit);
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
         * 返回的审计日志列表响应
         */
        private GetAuditLogsResp data;
    
        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
    
            this.data = Json.decode(response.bodyString(), GetAuditLogsResp.class);
        }
    
        /**
         * 响应信息
         *
         * @return GetAuditLogsResp
         */
        public GetAuditLogsResp getData() {
            return this.data;
        }
            
        /**
          * 返回的审计日志
          */
        public static final class GetAuditLog {
        
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
             * 接口操作对应的服务
             */
            @SerializedName("service")
            private String service;
        
            /**
             * 接口操作别名
             */
            @SerializedName("action")
            private String action;
        
            /**
             * 日志创建时间
             */
            @SerializedName("created_at")
            private String createdAt;
        
            /**
             * 请求发生时间
             */
            @SerializedName("event_time")
            private String eventTime;
        
            /**
             * 请求持续时间，毫秒
             */
            @SerializedName("duration_ms")
            private Integer durationMs;
        
            /**
             * 源 IP
             */
            @SerializedName("source_ip")
            private String sourceIp;
        
            /**
             * 用户代理
             */
            @SerializedName("user_event")
            private String userEvent;
        
            /**
             * 错误码
             */
            @SerializedName("error_code")
            private Integer errorCode;
        
            /**
             * 错误消息
             */
            @SerializedName("error_message")
            private String errorMessage;
        
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
             * 接口操作对应的服务
             *
             * @return service
             */
            public String getService() {
                return this.service;
            }
        
            /**
             * 获取变量值
             * 接口操作别名
             *
             * @return action
             */
            public String getAction() {
                return this.action;
            }
        
            /**
             * 获取变量值
             * 日志创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }
        
            /**
             * 获取变量值
             * 请求发生时间
             *
             * @return eventTime
             */
            public String getEventTime() {
                return this.eventTime;
            }
        
            /**
             * 获取变量值
             * 请求持续时间，毫秒
             *
             * @return durationMs
             */
            public Integer getDurationMs() {
                return this.durationMs;
            }
        
            /**
             * 获取变量值
             * 源 IP
             *
             * @return sourceIp
             */
            public String getSourceIp() {
                return this.sourceIp;
            }
        
            /**
             * 获取变量值
             * 用户代理
             *
             * @return userEvent
             */
            public String getUserEvent() {
                return this.userEvent;
            }
        
            /**
             * 获取变量值
             * 错误码
             *
             * @return errorCode
             */
            public Integer getErrorCode() {
                return this.errorCode;
            }
        
            /**
             * 获取变量值
             * 错误消息
             *
             * @return errorMessage
             */
            public String getErrorMessage() {
                return this.errorMessage;
            }
        }    
        /**
          * 返回的审计日志列表信息
          */
        public static final class GetAuditLogsData {
        
            /**
             * 下页标记
             */
            @SerializedName("marker")
            private String marker;
        
            /**
             * 审计日志列表
             */
            @SerializedName("list")
            private GetAuditLog[] list;
        
            /**
             * 获取变量值
             * 下页标记
             *
             * @return marker
             */
            public String getMarker() {
                return this.marker;
            }
        
            /**
             * 获取变量值
             * 审计日志列表
             *
             * @return list
             */
            public GetAuditLog[] getList() {
                return this.list;
            }
        }    
        /**
          * 返回的审计日志列表响应
          */
        public static final class GetAuditLogsResp {
        
            /**
             * 审计日志信息
             */
            @SerializedName("data")
            private GetAuditLogsData data;
        
            /**
             * 获取变量值
             * 审计日志信息
             *
             * @return data
             */
            public GetAuditLogsData getData() {
                return this.data;
            }
        }
    }
}