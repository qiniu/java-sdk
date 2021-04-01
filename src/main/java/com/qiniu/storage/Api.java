package com.qiniu.storage;

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

    private final Client client;

    public Api(Client client) {
        this.client = client;
    }

    com.qiniu.http.Response requestByClient(Request request) throws QiniuException {
        if (client == null) {
            ApiUtils.throwInvalidRequestParamException("client");
        }

        if (request == null) {
            ApiUtils.throwInvalidRequestParamException("request");
        }

        request.prepareToRequest();

        if (request.method.equals(Request.HTTP_METHOD_GET)) {
            return client.get(request.getUrl().toString(), request.getHeader());
        } else if (request.method.equals(Request.HTTP_METHOD_POST)) {
            return client.post(request.getUrl().toString(), request.body, request.bodyOffset, request.bodySize,
                    request.getHeader(), request.bodyContentType);
        } else if (request.method.equals(Request.HTTP_METHOD_PUT)) {
            return client.put(request.getUrl().toString(), request.body, request.bodyOffset, request.bodySize,
                    request.getHeader(), request.bodyContentType);
        } else {
            throw QiniuException.unrecoverable("暂不支持这种请求方式");
        }
    }

    /**
     * api 请求基类
     */
    public static class Request {

        public static String HTTP_METHOD_GET = "GET";
        public static String HTTP_METHOD_POST = "POST";
        public static String HTTP_METHOD_PUT = "PUT";

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
         * Http 请求方式
         */
        private String method;

        /**
         * 请求头
         */
        private final Map<String, String> header = new HashMap<>();

        /**
         * 请求数据是在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据
         */
        private byte[] body = new byte[0];
        private int bodySize = 0;
        private int bodyOffset = 0;
        private String bodyContentType = Client.DefaultMime;

        /**
         * 上传凭证
         */
        private String token;
        private UploadToken uploadToken;

        /**
         * 构造请求对象
         *
         * @param urlPrefix 请求的 urlPrefix， scheme + host
         */
        public Request(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        /**
         * 获取请求的 urlPrefix， scheme + host
         * eg: https://upload.qiniu.com
         */
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
        void buildPath() throws QiniuException {
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
        void buildQuery() throws QiniuException {

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
         * 设置 Http 请求方式
         *
         * @param method Http 请求方式
         */
        void setMethod(String method) {
            this.method = method;
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
        public StringMap getHeader() throws QiniuException {
            if (token == null || !getUploadToken().isValid()) {
                ApiUtils.throwInvalidRequestParamException("token");
            }

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
                if (!StringUtils.isNullOrEmpty(path)) {
                    file += path;
                }

                String query = getQuery();
                if (!StringUtils.isNullOrEmpty(query)) {
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
         * @param offset      请求数据在 body 中的偏移量
         * @param size        请求数据大小
         * @param contentType 请求数据类型
         */
        public void setBody(byte[] body, int offset, int size, String contentType) {
            this.body = body;
            this.bodyOffset = offset;
            this.bodySize = size;
            if (!StringUtils.isNullOrEmpty(contentType)) {
                this.bodyContentType = contentType;
            }
        }

        boolean hasBody() {
            return body != null && body.length > 0 && bodySize > 0;
        }

        /**
         * 构造 body 信息，如果需要设置请求体，子类需要重写
         *
         * @throws QiniuException
         */
        void buildBodyInfo() throws QiniuException {
            if (body == null) {
                body = new byte[0];
                bodySize = 0;
                bodyOffset = 0;
            }
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
         * 获取上传凭证
         *
         * @return 上传凭证
         */
        UploadToken getUploadToken() throws QiniuException {
            if (uploadToken == null) {
                uploadToken = new UploadToken(token);
            }
            return uploadToken;
        }

        /**
         * 准备上传 做一些参数检查 以及 参数构造
         *
         * @throws QiniuException 异常，一般为参数缺失
         */
        private void prepareToRequest() throws QiniuException {
            buildPath();
            buildQuery();
            buildBodyInfo();
        }
    }


    /**
     * api 响应基类
     */
    public static class Response {

        /**
         * 响应数据
         */
        private final StringMap dataMap;

        /**
         * 原响应结果
         */
        private final com.qiniu.http.Response response;

        /**
         * 构建 Response
         *
         * @param response com.qiniu.http.Response
         * @throws QiniuException 解析 data 异常
         */
        Response(com.qiniu.http.Response response) throws QiniuException {
            this.dataMap = response.jsonToMap();
            this.response = response;
        }

        /**
         * 获取 response data map
         *
         * @return data map
         */
        public StringMap getDataMap() {
            return dataMap;
        }

        /**
         * 获取 com.qiniu.http.Response，信息量更大
         *
         * @return com.qiniu.http.Response
         */
        public com.qiniu.http.Response getResponse() {
            return response;
        }

        /**
         * 请求是否成功
         *
         * @return 是否成功
         */
        public boolean isOK() {
            return response.isOK();
        }

        /**
         * 根据 key 读取 data map 的 String value
         *
         * @param key key
         * @return key 对应的 String value
         */
        public String getStringValueFromDataMap(String key) {
            if (StringUtils.isNullOrEmpty(key)) {
                return null;
            }
            return getStringValueFromDataMap(new String[]{key});
        }

        /**
         * 根据 keyPath 读取 data map 中对应的 String value
         * eg：
         * dataMap: {"key00" : { "key10" : "key10_value"}}
         * keyPath = new String[]{"key00", "key10"}
         * 调用方法后 value = key10_value
         *
         * @param keyPath keyPath
         * @return keyPath 对应的 String value
         */
        public String getStringValueFromDataMap(String[] keyPath) {
            Object value = getValueFromDataMap(keyPath);
            if (value == null) {
                return null;
            }
            return value.toString();
        }

        /**
         * 根据 key 读取 data map 的 Long value
         *
         * @param key key
         * @return key 对应的 Long value
         */
        public Long getLongValueFromDataMap(String key) {
            if (StringUtils.isNullOrEmpty(key)) {
                return null;
            }
            return getLongValueFromDataMap(new String[]{key});
        }

        /**
         * 根据 keyPath 读取 data map 中对应的 Long value
         * eg：
         * dataMap: {"key00" : { "key10" : 10}}
         * keyPath = new String[]{"key00", "key10"}
         * 调用方法后 value = 10
         *
         * @param keyPath keyPath
         * @return keyPath 对应的 Long value
         */
        public Long getLongValueFromDataMap(String[] keyPath) {
            Object value = getValueFromDataMap(keyPath);
            if (value == null) {
                return null;
            }
            if (value instanceof Double) {
                return ((Double) value).longValue();
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Long) {
                return (Long) value;
            } else {
                return null;
            }
        }

        /**
         * 根据 keyPath 读取 data map 中对应的 value
         * eg：
         * dataMap: {"key00" : { "key10" : "key10_value"}}
         * keyPath = new String[]{"key00", "key10"}
         * 调用方法后 value = key10_value
         *
         * @param keyPath keyPath
         * @return keyPath 对应的 value
         */
        public Object getValueFromDataMap(String[] keyPath) {
            if (dataMap == null || keyPath == null || keyPath.length == 0) {
                return null;
            }

            Object value = dataMap.map();
            for (String key : keyPath) {
                if (value instanceof Map) {
                    value = ((Map) value).get(key);
                } else {
                    value = null;
                    break;
                }
            }

            return value;
        }
    }
}
