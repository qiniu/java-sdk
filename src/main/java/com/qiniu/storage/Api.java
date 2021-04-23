package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.RequestStreamBody;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * api 基类
 */
public class Api {

    private final Client client;

    protected Api(Client client) {
        this.client = client;
    }

    protected com.qiniu.http.Response requestByClient(Request request) throws QiniuException {
        if (client == null) {
            ApiUtils.throwInvalidRequestParamException("client");
        }

        if (request == null) {
            ApiUtils.throwInvalidRequestParamException("request");
        }

        request.prepareToRequest();

        if (request.method == MethodType.GET) {
            return client.get(request.getUrl().toString(), request.getHeader());
        } else if (request.method == MethodType.POST) {
            return client.post(request.getUrl().toString(), request.getRequestBody(), request.getHeader());
        } else if (request.method == MethodType.PUT) {
            return client.put(request.getUrl().toString(), request.getRequestBody(), request.getHeader());
        } else if (request.method == MethodType.DELETE) {
            return client.delete(request.getUrl().toString(), request.getRequestBody(), request.getHeader());
        } else {
            throw QiniuException.unrecoverable("暂不支持这种请求方式");
        }
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
        private final List<String> pathSegments = new ArrayList<>();

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
        private MethodType method;

        /**
         * 请求头
         */
        private final Map<String, String> header = new HashMap<>();

        /**
         * 请求 body
         */
        private RequestBody body;
        /**
         * 请求时，每次从流中读取的数据大小
         * 注： body 使用 InputStream 时才有效
         */
        private long streamBodySinkSize = 1024 * 10;

        /**
         * 构造请求对象
         *
         * @param urlPrefix 请求的 urlPrefix， scheme + host
         */
        protected Request(String urlPrefix) {
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
         * 获取 请求的 Host
         *
         * @return Host
         * @throws QiniuException 解析 urlPrefix 时的异常
         */
        public String getHost() throws QiniuException {
            try {
                URL url = new URL(urlPrefix);
                return url.getHost();
            } catch (Exception e) {
                throw new QiniuException(e);
            }
        }

        /**
         * 添加 path 信息，注意 path 添加顺序
         * 所有的 path item 最终会按照顺序被拼接作为 path
         * eg: /item0/item1
         *
         * @param segment 被添加 path segment
         */
        protected void addPathSegment(String segment) {
            if (segment == null) {
                return;
            }
            pathSegments.add(segment);
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
        protected void buildPath() throws QiniuException {
            path = "/" + StringUtils.join(pathSegments, "/");
        }


        /**
         * 增加 query 键值对
         *
         * @param key   key
         * @param value value
         */
        protected void addQueryPair(String key, String value) {
            if (StringUtils.isNullOrEmpty(key)) {
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
        protected void buildQuery() throws QiniuException {

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
        protected void setMethod(MethodType method) {
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
            StringMap header = new StringMap();
            for (String key : this.header.keySet()) {
                header.put(key, this.header.get(key));
            }
            return header;
        }

        /**
         * 获取 URL
         *
         * @return url
         */
        public URL getUrl() throws QiniuException {
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
        protected void setBody(byte[] body, int offset, int size, String contentType) {
            if (StringUtils.isNullOrEmpty(contentType)) {
                contentType = Client.DefaultMime;
            }
            MediaType type = MediaType.parse(contentType);
            this.body = RequestBody.create(type, body, offset, size);
        }

        /**
         * 设置请求体
         *
         * @param body        请求数据源
         * @param contentType 请求数据类型
         * @param limitSize   最大读取 body 的大小；body 有多余则被舍弃；body 不足则会上传多有 body；
         *                    如果提前不知道 body 大小，但想上传所有 body，limitSize 设置为 -1 即可；
         */
        protected void setBody(InputStream body, String contentType, long limitSize) {
            if (StringUtils.isNullOrEmpty(contentType)) {
                contentType = Client.DefaultMime;
            }
            MediaType type = MediaType.parse(contentType);
            this.body = new RequestStreamBody(body, type, limitSize);
        }

        /**
         * 使用 streamBody 时，每次读取 streamBody 的大小，读取后发送
         * 默认：{@link Api.Request#streamBodySinkSize}
         * 相关：{@link RequestStreamBody#writeTo(BufferedSink) sinkSize}
         *
         * @param streamBodySinkSize 每次读取 streamBody 的大小
         */
        public Request setStreamBodySinkSize(long streamBodySinkSize) {
            this.streamBodySinkSize = streamBodySinkSize;
            return this;
        }

        /**
         * 是否有请求体
         *
         * @return 是否有请求体
         */
        public boolean hasBody() {
            return body != null;
        }

        private RequestBody getRequestBody() {
            if (hasBody()) {
                if (body instanceof RequestStreamBody) {
                    ((RequestStreamBody) body).setSinkSize(streamBodySinkSize);
                }
                return body;
            } else {
                return RequestBody.create(null, new byte[0]);
            }
        }

        /**
         * 构造 body 信息，如果需要设置请求体，子类需要重写
         *
         * @throws QiniuException
         */
        protected void buildBodyInfo() throws QiniuException {

        }

        /**
         * 准备上传 做一些参数检查 以及 参数构造
         *
         * @throws QiniuException 异常，一般为参数缺失
         */
        protected void prepareToRequest() throws QiniuException {
            buildPath();
            buildQuery();
            buildBodyInfo();
        }

        protected static class Pair<K, V> {

            /**
             * Key of this <code>Pair</code>.
             */
            private K key;

            /**
             * Gets the key for this pair.
             *
             * @return key for this pair
             */
            K getKey() {
                return key;
            }

            /**
             * Value of this this <code>Pair</code>.
             */
            private V value;

            /**
             * Gets the value for this pair.
             *
             * @return value for this pair
             */
            V getValue() {
                return value;
            }

            /**
             * Creates a new pair
             *
             * @param key   The key for this pair
             * @param value The value to use for this pair
             */
            protected Pair(K key, V value) {
                this.key = key;
                this.value = value;
            }

            /**
             * <p><code>String</code> representation of this
             * <code>Pair</code>.</p>
             *
             * <p>The default name/value delimiter '=' is always used.</p>
             *
             * @return <code>String</code> representation of this <code>Pair</code>
             */
            @Override
            public String toString() {
                return key + "=" + value;
            }

            /**
             * <p>Generate a hash code for this <code>Pair</code>.</p>
             *
             * <p>The hash code is calculated using both the name and
             * the value of the <code>Pair</code>.</p>
             *
             * @return hash code for this <code>Pair</code>
             */
            @Override
            public int hashCode() {
                // name's hashCode is multiplied by an arbitrary prime number (13)
                // in order to make sure there is a difference in the hashCode between
                // these two parameters:
                //  name: a  value: aa
                //  name: aa value: a
                return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
            }

            /**
             * <p>Test this <code>Pair</code> for equality with another
             * <code>Object</code>.</p>
             *
             * <p>If the <code>Object</code> to be tested is not a
             * <code>Pair</code> or is <code>null</code>, then this method
             * returns <code>false</code>.</p>
             *
             * <p>Two <code>Pair</code>s are considered equal if and only if
             * both the names and values are equal.</p>
             *
             * @param o the <code>Object</code> to test for
             *          equality with this <code>Pair</code>
             * @return <code>true</code> if the given <code>Object</code> is
             * equal to this <code>Pair</code> else <code>false</code>
             */
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o instanceof Pair) {
                    Pair pair = (Pair) o;
                    if (!Objects.equals(key, pair.key)) return false;
                    if (!Objects.equals(value, pair.value)) return false;
                    return true;
                }
                return false;
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
        protected Response(com.qiniu.http.Response response) throws QiniuException {
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
         * 根据 keyPath 读取 data map 中对应的 String value
         * eg：
         * dataMap: {"key00" : { "key10" : "key10_value"}}
         * keyPath = new String[]{"key00", "key10"}
         * 调用方法后 value = key10_value
         *
         * @param keyPath keyPath
         * @return keyPath 对应的 String value
         */
        public String getStringValueFromDataMap(String... keyPath) {
            Object value = getValueFromDataMap(keyPath);
            if (value == null) {
                return null;
            }
            return value.toString();
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
        public Long getLongValueFromDataMap(String... keyPath) {
            Object value = getValueFromDataMap(keyPath);
            return ApiUtils.objectToLong(value);
        }

        /**
         * 根据 keyPath 读取 data map 中对应的 Integer value
         * eg：
         * dataMap: {"key00" : { "key10" : 10}}
         * keyPath = new String[]{"key00", "key10"}
         * 调用方法后 value = 10
         *
         * @param keyPath keyPath
         * @return keyPath 对应的 Integer value
         */
        public Integer getIntegerValueFromDataMap(String... keyPath) {
            Object value = getValueFromDataMap(keyPath);
            return ApiUtils.objectToInteger(value);
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
        public Object getValueFromDataMap(String... keyPath) {
            if (dataMap == null) {
                return null;
            }
            return ApiUtils.getValueFromMap(dataMap.map(), keyPath);
        }
    }
}
