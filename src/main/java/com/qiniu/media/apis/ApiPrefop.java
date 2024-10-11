package com.qiniu.media.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 查询持久化数据处理命令的执行状态
 */
public class ApiPrefop extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiPrefop(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiPrefop(Client client, Config config) {
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
         * 持久化数据处理任务 ID
         */
        private String persistentId;

        /**
         * 请求构造函数
         *
         * @param urlPrefix    请求 scheme + host 【可选】
         *                     若为空则会直接从 HostProvider 中获取
         * @param persistentId 持久化数据处理任务 ID 【必须】
         */
        public Request(String urlPrefix, String persistentId) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
            this.persistentId = persistentId;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {
            if (this.persistentId == null) {
                throw new QiniuException(new NullPointerException("persistentId can't empty"));
            }

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("status/get/prefop");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

            addQueryPair("id", this.persistentId);

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
         * 返回的持久化数据处理任务信息
         */
        private PfopTask data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), PfopTask.class);
        }

        /**
         * 响应信息
         *
         * @return PfopTask
         */
        public PfopTask getData() {
            return this.data;
        }

        /**
         * 返回的持久化数据处理任务中的云处理操作状态
         */
        public static final class PfopTaskItem {

            /**
             * 云操作命令
             */
            @SerializedName("cmd")
            private String command;

            /**
             * 云操作状态码
             */
            @SerializedName("code")
            private Integer code;

            /**
             * 与状态码相对应的详细描述
             */
            @SerializedName("desc")
            private String description;

            /**
             * 如果处理失败，该字段会给出失败的详细原因
             */
            @SerializedName("error")
            private String error;

            /**
             * 云处理结果保存在服务端的唯一标识
             */
            @SerializedName("hash")
            private String hash;

            /**
             * 云处理结果的外链对象名称
             */
            @SerializedName("key")
            private String objectName;

            /**
             * 是否返回了旧的数据
             */
            @SerializedName("returnOld")
            private Integer returnOld;

            /**
             * 获取变量值
             * 云操作命令
             *
             * @return command
             */
            public String getCommand() {
                return this.command;
            }

            /**
             * 获取变量值
             * 云操作状态码
             *
             * @return code
             */
            public Integer getCode() {
                return this.code;
            }

            /**
             * 获取变量值
             * 与状态码相对应的详细描述
             *
             * @return description
             */
            public String getDescription() {
                return this.description;
            }

            /**
             * 获取变量值
             * 如果处理失败，该字段会给出失败的详细原因
             *
             * @return error
             */
            public String getError() {
                return this.error;
            }

            /**
             * 获取变量值
             * 云处理结果保存在服务端的唯一标识
             *
             * @return hash
             */
            public String getHash() {
                return this.hash;
            }

            /**
             * 获取变量值
             * 云处理结果的外链对象名称
             *
             * @return objectName
             */
            public String getObjectName() {
                return this.objectName;
            }

            /**
             * 获取变量值
             * 是否返回了旧的数据
             *
             * @return returnOld
             */
            public Integer getReturnOld() {
                return this.returnOld;
            }
        }

        /**
         * 返回的持久化数据处理任务信息
         */
        public static final class PfopTask {

            /**
             * 持久化数据处理任务 ID
             */
            @SerializedName("id")
            private String persistentId;

            /**
             * 持久化数据处理任务状态码
             */
            @SerializedName("code")
            private Integer code;

            /**
             * 与状态码相对应的详细描述
             */
            @SerializedName("desc")
            private String description;

            /**
             * 源对象名称
             */
            @SerializedName("inputKey")
            private String objectName;

            /**
             * 源空间名称
             */
            @SerializedName("inputBucket")
            private String bucketName;

            /**
             * 云处理操作的处理队列
             */
            @SerializedName("pipeline")
            private String pipeline;

            /**
             * 如果没有，则表示通过 `api+fops` 命令提交的任务，否则遵循规则 `<source>: <source_id>`，其中 `<source>` 当前可选 `workflow` 或 `trigger`
             */
            @SerializedName("taskFrom")
            private String taskFrom;

            /**
             * 云处理请求的请求 ID
             */
            @SerializedName("reqid")
            private String requestId;

            /**
             * 任务类型，支持 `0` 表示普通任务，`1` 表示闲时任务
             */
            @SerializedName("type")
            private Integer type;

            /**
             * 任务创建时间
             */
            @SerializedName("creationDate")
            private String createdAt;

            /**
             * 云处理操作列表
             */
            @SerializedName("items")
            private PfopTaskItem[] items;

            /**
             * 获取变量值
             * 持久化数据处理任务 ID
             *
             * @return persistentId
             */
            public String getPersistentId() {
                return this.persistentId;
            }

            /**
             * 获取变量值
             * 持久化数据处理任务状态码
             *
             * @return code
             */
            public Integer getCode() {
                return this.code;
            }

            /**
             * 获取变量值
             * 与状态码相对应的详细描述
             *
             * @return description
             */
            public String getDescription() {
                return this.description;
            }

            /**
             * 获取变量值
             * 源对象名称
             *
             * @return objectName
             */
            public String getObjectName() {
                return this.objectName;
            }

            /**
             * 获取变量值
             * 源空间名称
             *
             * @return bucketName
             */
            public String getBucketName() {
                return this.bucketName;
            }

            /**
             * 获取变量值
             * 云处理操作的处理队列
             *
             * @return pipeline
             */
            public String getPipeline() {
                return this.pipeline;
            }

            /**
             * 获取变量值
             * 如果没有，则表示通过 `api+fops` 命令提交的任务，否则遵循规则 `<source>: <source_id>`，其中 `<source>` 当前可选 `workflow` 或 `trigger`
             *
             * @return taskFrom
             */
            public String getTaskFrom() {
                return this.taskFrom;
            }

            /**
             * 获取变量值
             * 云处理请求的请求 ID
             *
             * @return requestId
             */
            public String getRequestId() {
                return this.requestId;
            }

            /**
             * 获取变量值
             * 任务类型，支持 `0` 表示普通任务，`1` 表示闲时任务
             *
             * @return type
             */
            public Integer getType() {
                return this.type;
            }

            /**
             * 获取变量值
             * 任务创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }

            /**
             * 获取变量值
             * 云处理操作列表
             *
             * @return items
             */
            public PfopTaskItem[] getItems() {
                return this.items;
            }
        }
    }
}