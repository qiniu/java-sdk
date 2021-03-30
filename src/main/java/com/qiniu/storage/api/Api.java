package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

/**
 * api 基类
 */
public class Api {

    final Client client;

    public Api(Client client) {
        this.client = client;
    }


    /**
     * api 请求基类
     */
    public static class Request {
        /**
         * 请求的域名
         * URL = host + action
         */
        public final String host;

        /**
         * 请求的 action
         * URL = host + action
         */
        String action;

        /**
         * 请求头
         */
        public final StringMap header = new StringMap();

        /**
         * 请求数据是在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据
         */
        byte[] body = new byte[0];
        int bodySize = 0;
        int bodyOffset = 0;
        String bodyContentType = Client.DefaultMime;

        /**
         * 构造请求对象
         * URL = host + action
         *
         * @param host  请求使用的 host
         * @param token 请求凭证
         */
        public Request(String host, String token) {
            this.host = host;
            this.header.put("Authorization", "UpToken " + token);
        }

        public String getAction() throws QiniuException {
            if (StringUtils.isNullOrEmpty(action)) {
                buildAction();
            }
            return action;
        }

        public void buildAction() throws QiniuException {
        }

        public void setAction(String action) {
            this.action = action;
        }

        /**
         * 获取 URL
         *
         * @return url
         */
        String getUrl() {
            return host + action;
        }

        /**
         * 设置请求体
         * 请求数据：在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据
         *
         * @param body        请求数据源
         * @param size        请求数据大小
         * @param offset      请求数据在 body 中的偏移量
         * @param contentType 请求数据类型
         */
        public void setBody(byte[] body, int size, int offset, String contentType) {
            this.body = body;
            this.bodySize = size;
            this.bodyOffset = offset;
            if (!StringUtils.isNullOrEmpty(contentType)) {
                this.bodyContentType = contentType;
            }
        }
    }


    /**
     * api 响应基类
     */
    public static class Response {

        /**
         * 响应数据
         */
        public final StringMap jsonMap;

        /**
         * 原响应结果
         */
        public final com.qiniu.http.Response response;

        public Response(com.qiniu.http.Response response) throws QiniuException {
            this.jsonMap = response.jsonToMap();
            this.response = response;
        }
    }
}
