package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
         * 请求 url 的 scheme + host
         * eg: https://upload.qiniu.com
         */
        public final String host;

        /**
         * 请求 url 的 path
         */
        private String path;

        /**
         * 请求 url 的 信息，最终会被按顺序拼接作为 path /item0/item1
         */
        public final List<String> pathList = new ArrayList<>();

        /**
         * 请求 url 的 query
         * 由 queryInfo 拼接
         */
        private String query;

        /**
         * 请求 url 的 query 信息，最终会被拼接作为 query key0=value0&key1=value1
         */
        public final StringMap queryInfo = new StringMap();

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

        /**
         * 获取 query 字符串
         *
         * @return query 字符串
         * @throws QiniuException 组装 query 时的异常，一般为缺失必要参数的异常
         */
        public String getQuery() throws QiniuException {
            if (StringUtils.isNullOrEmpty(query)) {
                buildQuery();
            }
            return query;
        }

        /**
         * 直接设置 query 字符串
         *
         * @param query query 字符串
         */
        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * 根据 queryInfo 组装 query 字符串
         *
         * @throws QiniuException 组装 query 时的异常，一般为缺失必要参数的异常
         */
        public void buildQuery() throws QiniuException {
            StringBuilder builder = new StringBuilder();
            for (String key : queryInfo.map().keySet()) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                builder.append(key);
                builder.append("=");
                builder.append(queryInfo.get(key));
            }
            query = builder.toString();
        }

        /**
         * 获取 url 的 path 信息
         *
         * @return path 信息
         */
        public String getPath() throws QiniuException {
            if (StringUtils.isNullOrEmpty(path)) {
                buildPath();
            }
            return path;
        }

        /**
         * 配置 url 的 path 信息
         *
         * @param path 信息
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * 根据 queryInfo 组装 query 字符串
         *
         * @throws QiniuException 组装 query 时的异常，一般为缺失必要参数的异常
         */
        public void buildPath() throws QiniuException {
            StringBuilder builder = new StringBuilder();
            for (String item : pathList) {
                if (StringUtils.isNullOrEmpty(item)) {
                    builder.append("/");
                    builder.append(item);
                }
            }
            path = builder.toString();
        }

        /**
         * 获取 URL
         *
         * @return url
         */
        String getUrl() throws QiniuException {
            StringBuilder result = new StringBuilder();
            result.append(host);

            String path = getPath();
            if (path != null) {
                result.append(path);
            }

            String query = getQuery();
            if (query != null) {
                result.append('?');
                result.append(query);
            }

            return result.toString();
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
