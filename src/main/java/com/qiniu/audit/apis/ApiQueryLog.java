package com.qiniu.audit.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 审计日志查询
 */
public class ApiQueryLog extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiQueryLog(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiQueryLog(Client client, Config config) {
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
         * 检索日志的开始时间，日期格式按照 ISO8601 标准，并使用 UTC 时间
         */
        private String startTime;

        /**
         * 检索日志的结束时间，日期格式按照 ISO8601 标准，并使用 UTC 时间
         */
        private String endTime;

        /**
         * 服务名称，参考 https://developer.qiniu.com/af/12434/audit-log-events
         */
        private String serviceName = null;

        /**
         * 事件名称集合，参考 https://developer.qiniu.com/af/12434/audit-log-events
         */
        private String eventNames = null;

        /**
         * 请求者的 ID，参考 https://developer.qiniu.com/af/manual/12433/audit-log-object
         */
        private String principalId = null;

        /**
         * 请求身份所属的 AccessKey ID
         */
        private String accessKeyId = null;

        /**
         * 允许返回的最大结果数目，取值范围：1~50，不传值默认为：20
         */
        private Integer limit = null;

        /**
         * 用于请求下一页检索的结果
         */
        private String nextMark = null;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         * @param startTime 检索日志的开始时间，日期格式按照 ISO8601 标准，并使用 UTC 时间 【必须】
         * @param endTime   检索日志的结束时间，日期格式按照 ISO8601 标准，并使用 UTC 时间 【必须】
         */
        public Request(String urlPrefix, String startTime, String endTime) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * 设置参数【可选】
         *
         * @param serviceName 服务名称，参考 https://developer.qiniu.com/af/12434/audit-log-events
         * @return Request
         */
        public Request setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param eventNames 事件名称集合，参考 https://developer.qiniu.com/af/12434/audit-log-events
         * @return Request
         */
        public Request setEventNames(String eventNames) {
            this.eventNames = eventNames;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param principalId 请求者的 ID，参考 https://developer.qiniu.com/af/manual/12433/audit-log-object
         * @return Request
         */
        public Request setPrincipalId(String principalId) {
            this.principalId = principalId;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param accessKeyId 请求身份所属的 AccessKey ID
         * @return Request
         */
        public Request setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param limit 允许返回的最大结果数目，取值范围：1~50，不传值默认为：20
         * @return Request
         */
        public Request setLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param nextMark 用于请求下一页检索的结果
         * @return Request
         */
        public Request setNextMark(String nextMark) {
            this.nextMark = nextMark;
            return this;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.startTime == null) {
                throw new QiniuException(new NullPointerException("startTime can't empty"));
            }
            if (this.endTime == null) {
                throw new QiniuException(new NullPointerException("endTime can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("audit/log-query");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

            addQueryPair("start_time", this.startTime);
            addQueryPair("end_time", this.endTime);
            if (this.serviceName != null) {
                addQueryPair("service_name", this.serviceName);
            }
            if (this.eventNames != null) {
                addQueryPair("event_names", this.eventNames);
            }
            if (this.principalId != null) {
                addQueryPair("principal_id", this.principalId);
            }
            if (this.accessKeyId != null) {
                addQueryPair("access_key_id", this.accessKeyId);
            }
            if (this.limit != null) {
                addQueryPair("limit", this.limit);
            }
            if (this.nextMark != null) {
                addQueryPair("next_mark", this.nextMark);
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
         *
         */
        private QueryLogResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), QueryLogResp.class);
        }

        /**
         * 响应信息
         *
         * @return QueryLogResp
         */
        public QueryLogResp getData() {
            return this.data;
        }

        /**
         * 返回的请求者的身份信息
         */
        public static final class UserIdentify {

            /**
             * 当前请求所属的七牛云账号 ID
             */
            @SerializedName("account_id")
            private String accountId;

            /**
             * 当前请求者的 ID，需结合 principal_type 来确认请求者身份
             */
            @SerializedName("principal_id")
            private String principalId;

            /**
             * 请求者身份类型，仅支持 UNKNOWN 表示未知，ROOT_USER 表示七牛云账户 ID，IAM_USER 表示 IAM 子账户 ID，QINIU_ACCOUNT 表示当七牛云账号跨账号操作时，记录七牛云账号 ID
             */
            @SerializedName("principal_type")
            private String principalType;

            /**
             * 当前请求身份所属的 AccessKey ID
             */
            @SerializedName("access_key_id")
            private String accessKeyId;

            /**
             * 获取变量值
             * 当前请求所属的七牛云账号 ID
             *
             * @return accountId
             */
            public String getAccountId() {
                return this.accountId;
            }

            /**
             * 获取变量值
             * 当前请求者的 ID，需结合 principal_type 来确认请求者身份
             *
             * @return principalId
             */
            public String getPrincipalId() {
                return this.principalId;
            }

            /**
             * 获取变量值
             * 请求者身份类型，仅支持 UNKNOWN 表示未知，ROOT_USER 表示七牛云账户 ID，IAM_USER 表示 IAM 子账户 ID，QINIU_ACCOUNT 表示当七牛云账号跨账号操作时，记录七牛云账号 ID
             *
             * @return principalType
             */
            public String getPrincipalType() {
                return this.principalType;
            }

            /**
             * 获取变量值
             * 当前请求身份所属的 AccessKey ID
             *
             * @return accessKeyId
             */
            public String getAccessKeyId() {
                return this.accessKeyId;
            }
        }

        /**
         * 返回的审计日志
         */
        public static final class LogInfo {

            /**
             * 日志 ID
             */
            @SerializedName("event_id")
            private String eventId;

            /**
             * 事件类型，仅支持 UNKNOWN 表示未知，CONSOLE 表示控制台事件，API 表示 API 事件
             */
            @SerializedName("event_type")
            private String eventType;

            /**
             * 事件发生时间（UTC 格式）
             */
            @SerializedName("event_time")
            private String eventTime;

            /**
             * 请求者的身份信息
             */
            @SerializedName("user_identity")
            private UserIdentify userIdentity;

            /**
             * 读写类型，仅支持 UNKNOWN 表示未知，READ 表示读，WRITE 表示写
             */
            @SerializedName("event_rw")
            private String eventRw;

            /**
             * 服务名称
             */
            @SerializedName("service_name")
            private String serviceName;

            /**
             * 事件名称
             */
            @SerializedName("event_name")
            private String eventName;

            /**
             * 源 IP 地址
             */
            @SerializedName("source_ip")
            private String sourceIp;

            /**
             * 客户端代理
             */
            @SerializedName("user_agent")
            private String userAgent;

            /**
             * 操作的资源名称列表
             */
            @SerializedName("resource_names")
            private String[] resourceNames;

            /**
             * 请求 ID
             */
            @SerializedName("request_id")
            private String requestId;

            /**
             * 请求 URL
             */
            @SerializedName("request_url")
            private String requestUrl;

            /**
             * 请求的输入参数
             */
            @SerializedName("request_params")
            private String requestParams;

            /**
             * 请求的返回数据
             */
            @SerializedName("response_data")
            private String responseData;

            /**
             * 请求的返回码
             */
            @SerializedName("response_code")
            private Integer responseCode;

            /**
             * 请求的返回信息
             */
            @SerializedName("response_message")
            private String responseMessage;

            /**
             * 额外备注信息
             */
            @SerializedName("additional_event_data")
            private String additionalEventData;

            /**
             * 获取变量值
             * 日志 ID
             *
             * @return eventId
             */
            public String getEventId() {
                return this.eventId;
            }

            /**
             * 获取变量值
             * 事件类型，仅支持 UNKNOWN 表示未知，CONSOLE 表示控制台事件，API 表示 API 事件
             *
             * @return eventType
             */
            public String getEventType() {
                return this.eventType;
            }

            /**
             * 获取变量值
             * 事件发生时间（UTC 格式）
             *
             * @return eventTime
             */
            public String getEventTime() {
                return this.eventTime;
            }

            /**
             * 获取变量值
             * 请求者的身份信息
             *
             * @return userIdentity
             */
            public UserIdentify getUserIdentity() {
                return this.userIdentity;
            }

            /**
             * 获取变量值
             * 读写类型，仅支持 UNKNOWN 表示未知，READ 表示读，WRITE 表示写
             *
             * @return eventRw
             */
            public String getEventRw() {
                return this.eventRw;
            }

            /**
             * 获取变量值
             * 服务名称
             *
             * @return serviceName
             */
            public String getServiceName() {
                return this.serviceName;
            }

            /**
             * 获取变量值
             * 事件名称
             *
             * @return eventName
             */
            public String getEventName() {
                return this.eventName;
            }

            /**
             * 获取变量值
             * 源 IP 地址
             *
             * @return sourceIp
             */
            public String getSourceIp() {
                return this.sourceIp;
            }

            /**
             * 获取变量值
             * 客户端代理
             *
             * @return userAgent
             */
            public String getUserAgent() {
                return this.userAgent;
            }

            /**
             * 获取变量值
             * 操作的资源名称列表
             *
             * @return resourceNames
             */
            public String[] getResourceNames() {
                return this.resourceNames;
            }

            /**
             * 获取变量值
             * 请求 ID
             *
             * @return requestId
             */
            public String getRequestId() {
                return this.requestId;
            }

            /**
             * 获取变量值
             * 请求 URL
             *
             * @return requestUrl
             */
            public String getRequestUrl() {
                return this.requestUrl;
            }

            /**
             * 获取变量值
             * 请求的输入参数
             *
             * @return requestParams
             */
            public String getRequestParams() {
                return this.requestParams;
            }

            /**
             * 获取变量值
             * 请求的返回数据
             *
             * @return responseData
             */
            public String getResponseData() {
                return this.responseData;
            }

            /**
             * 获取变量值
             * 请求的返回码
             *
             * @return responseCode
             */
            public Integer getResponseCode() {
                return this.responseCode;
            }

            /**
             * 获取变量值
             * 请求的返回信息
             *
             * @return responseMessage
             */
            public String getResponseMessage() {
                return this.responseMessage;
            }

            /**
             * 获取变量值
             * 额外备注信息
             *
             * @return additionalEventData
             */
            public String getAdditionalEventData() {
                return this.additionalEventData;
            }
        }

        /**
         *
         */
        public static final class QueryLogResp {

            /**
             * 用于请求下一页检索的结果
             */
            @SerializedName("next_mark")
            private String nextMark;

            /**
             * 日志集合
             */
            @SerializedName("audit_log_infos")
            private LogInfo[] auditLogInfos;

            /**
             * 获取变量值
             * 用于请求下一页检索的结果
             *
             * @return nextMark
             */
            public String getNextMark() {
                return this.nextMark;
            }

            /**
             * 获取变量值
             * 日志集合
             *
             * @return auditLogInfos
             */
            public LogInfo[] getAuditLogInfos() {
                return this.auditLogInfos;
            }
        }
    }
}