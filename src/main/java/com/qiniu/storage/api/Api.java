package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import javafx.util.Pair;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
         * 请求的 urlPrefix， scheme + host
         * eg: https://upload.qiniu.com
         */
        private final String urlPrefix;

        /**
         * 请求 url 的 path
         */
        private String path;

        /**
         * 请求 url 的 信息，最终会被按顺序拼接作为 path /Segment0/Segment1
         */
        private final List<String> pathList = new ArrayList<>();

        /**
         * 请求 url 的 query
         * 由 queryInfo 拼接
         */
        private String query;

        /**
         * 请求 url 的 query 信息，最终会被拼接作为 query key0=value0&key1=value1
         */
        private final List<Pair<String, String>> queryPairs = new ArrayList<>();

        /**
         * 请求头
         */
        private final Map<String, String> header = new HashMap<>();

        /**
         * 请求数据是在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据
         */
        byte[] body = new byte[0];
        int bodySize = 0;
        int bodyOffset = 0;
        String bodyContentType = Client.DefaultMime;

        /**
         * 上传凭证
         */
        private String token;

        /**
         * 构造请求对象
         * URL = host + action
         *
         * @param host 请求使用的 host
         */
        public Request(String host) {
            this.urlPrefix = host;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        /**
         * 添加 path 信息，注意 path 添加顺序
         * 所有的 path item 最终会按照顺序被拼接作为 path
         * eg: /item0/item1
         *
         * @param segment 被添加 path segment
         */
        public void addPathSegment(String segment) {
            if (segment == null) {
                return;
            }
            pathList.add(segment);
            path = null;
        }

        /**
         * 获取 url 的 path 信息
         *
         * @return path 信息
         */
        public String getPath() throws QiniuException {
            if (path == null) {
                buildPath();
            }
            return path;
        }

        /**
         * 根据 queryInfo 组装 query 字符串
         *
         * @throws QiniuException 组装 query 时的异常，一般为缺失必要参数的异常
         */
        public void buildPath() throws QiniuException {
            path = "/" + StringUtils.join(pathList, "/");
        }


        /**
         * 增加 query 键值对
         *
         * @param key   key
         * @param value value
         */
        public void addQueryPair(String key, String value) {
            if (StringUtils.isNullOrEmpty(key) || value == null) {
                return;
            }
            queryPairs.add(new Pair<String, String>(key, value));
            query = null;
        }

        /**
         * 设置上传凭证
         *
         * @param token 上传凭证
         */
        public void setToken(String token) {
            this.token = token;
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
         * 根据 queryInfo 组装 query 字符串
         *
         * @throws QiniuException 组装 query 时的异常，一般为缺失必要参数的异常
         */
        public void buildQuery() throws QiniuException {

            StringBuilder builder = new StringBuilder();

            for (Pair<String, String> pair : queryPairs) {
                if (builder.length() > 0) {
                    builder.append("&");
                }

                try {
                    builder.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
                    if (pair.getValue() != null) {
                        builder.append("=");
                        builder.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
                    }
                } catch (Exception e) {
                    throw new QiniuException(e);
                }
            }

            query = builder.toString();
        }

        /**
         * 增加请求头
         *
         * @param key   key
         * @param value value
         */
        public void addHeaderField(String key, String value) {
            if (StringUtils.isNullOrEmpty(key) || StringUtils.isNullOrEmpty(value)) {
                return;
            }
            header.put(key, value);
        }


        /**
         * 获取请求头信息
         *
         * @return 请求头信息
         */
        public StringMap getHeader() {
            StringMap header = new StringMap();
            for (String key : this.header.keySet()) {
                header.put(key, this.header.get(key));
            }

            header.put("Authorization", "UpToken " + token);

            return header;
        }

        /**
         * 获取 URL
         *
         * @return url
         */
        URL getUrl() throws QiniuException {
            try {
                URL url = new URL(urlPrefix);
                String file = url.getFile();
                String path = getPath();
                if (StringUtils.isNullOrEmpty(path)) {
                    file += path;
                }

                String query = getQuery();
                if (StringUtils.isNullOrEmpty(query)) {
                    file += '?' + query;
                }
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
            } catch (Exception e) {
                throw new QiniuException(e);
            }
        }

        /**
         * 设置请求体
         * 请求数据：在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据作为请求体
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
