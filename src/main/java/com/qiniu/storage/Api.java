package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.RequestStreamBody;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * api 基类，非七牛 Api 请不要使用此接口，此 Api 有业务定制
 */
public class Api {

    private final Client client;

    private final List<Interceptor> interceptors;

    /**
     * 构造函数
     *
     * @param client 请求的 Client【必须】
     **/
    protected Api(Client client) {
        this.client = client;
        this.interceptors = null;
    }

    /**
     * 构造函数
     *
     * @param client 请求的 Client 【必须】
     * @param config 请求的流程的配置信息
     **/
    Api(Client client, Config config) {
        this(client, Api.createInterceptors(config));
    }

    /**
     * 构造函数
     *
     * @param client       请求的 Client【必须】
     * @param interceptors 请求的拦截器
     **/
    Api(Client client, Interceptor... interceptors) {
        if (client == null) {
            client = new Client();
        }
        this.client = client;

        List<Interceptor> is = new ArrayList<>();
        is.add(new ApiInterceptorDefaultHeader.Builder().build());

        if (interceptors != null) {
            is.addAll(Arrays.asList(interceptors));
        }

        Collections.sort(is, new Comparator<Interceptor>() {
            @Override
            public int compare(Interceptor o1, Interceptor o2) {
                return o1.priority() - o2.priority();
            }
        });

        // 反转
        Collections.reverse(is);

        this.interceptors = is;
    }

    private static Interceptor[] createInterceptors(Config config) {
        if (config == null) {
            config = new Config.Builder().build();
        }
        return new Interceptor[]{
                new ApiInterceptorAuth.Builder()
                        .setAuth(config.auth)
                        .build(),
                new ApiInterceptorDebug.Builder()
                        .setRequestLevel(config.requestDebugLevel)
                        .setResponseLevel(config.responseDebugLevel)
                        .build(),
                new ApiInterceptorRetryHosts.Builder()
                        .setHostProvider(config.hostProvider)
                        .setRetryInterval(config.retryInterval)
                        .setRetryMax(config.hostRetryMax)
                        .setRetryCondition(config.retryCondition)
                        .setHostFreezeCondition(config.hostFreezeCondition)
                        .setHostFreezeDuration(config.hostFreezeDuration)
                        .build(),
                new ApiInterceptorRetrySimple.Builder()
                        .setRetryMax(config.singleHostRetryMax)
                        .setRetryInterval(config.retryInterval)
                        .setRetryCondition(config.retryCondition)
                        .build()
        };
    }

    protected com.qiniu.http.Response innerRequest(Request request) throws QiniuException {
        if (client == null) {
            ApiUtils.throwInvalidRequestParamException("client");
        }

        MethodType method = request.getMethod();
        String url = request.getUrl().toString();
        StringMap header = request.getHeader();
        RequestBody body = method.hasContent() ? request.getRequestBody() : null;
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .method(method.toString(), body);
        return client.send(requestBuilder, header);
    }

    protected com.qiniu.http.Response requestByClient(Request request) throws QiniuException {
        if (request == null) {
            ApiUtils.throwInvalidRequestParamException("request");
        }

        request.prepareToRequest();

        return innerRequest(request);
    }

    protected com.qiniu.http.Response requestWithInterceptor(Request request) throws QiniuException {
        if (request == null) {
            ApiUtils.throwInvalidRequestParamException("request");
        }

        request.prepareToRequest();

        if (interceptors == null || interceptors.size() == 0) {
            return innerRequest(request);
        }

        Handler handler = new Handler() {
            @Override
            public Api.Response handle(Request req) throws QiniuException {
                return new Response(innerRequest(req));
            }
        };

        for (Interceptor interceptor : interceptors) {
            final Handler h = handler;
            final Interceptor i = interceptor;
            handler = new Handler() {
                @Override
                public Api.Response handle(Request req) throws QiniuException {
                    return i.intercept(req, h);
                }
            };
        }

        Response response = handler.handle(request);
        return response != null ? response.getResponse() : null;
    }

    protected Response request(Request request) throws QiniuException {
        return new Response(requestWithInterceptor(request));
    }

    public static final class Config {

        public static final int DebugLevelNone = ApiInterceptorDebug.LevelPrintNone;
        public static final int DebugLevelNormal = ApiInterceptorDebug.LevelPrintNormal;
        public static final int DebugLevelDetail = ApiInterceptorDebug.LevelPrintDetail;

        /**
         * 鉴权信息
         * 注：上传接口鉴权通过 up token 鉴权，不需要设置此参数
         **/
        private final Auth auth;

        /**
         * 多个域名切换重试的最大次数
         * 当一个域名请求失败，如果符合重试条件，会尝试切换其他域名进行重试
         **/
        private final int hostRetryMax;

        /**
         * 单个域名重试的最大次数
         * 某个域名的请求失败，如果符合重试条件，会尝试重试
         **/
        private final int singleHostRetryMax;

        /**
         * 重试间隔
         **/
        private final Retry.Interval retryInterval;

        /**
         * 重试条件
         **/
        private final Retry.RetryCondition retryCondition;

        /**
         * 域名冻结条件，域名冻结后在一定时间内不再使用
         **/
        private final Retry.HostFreezeCondition hostFreezeCondition;

        /**
         * 重试域名提供对象
         **/
        private final HostProvider hostProvider;

        /**
         * 域名冻结时间，单位：毫秒
         **/
        private final int hostFreezeDuration;

        /**
         * 请求信息 Debug 等级
         * 参考：
         * {@link Config#DebugLevelNone}
         * {@link Config#DebugLevelNormal}
         * {@link Config#DebugLevelDetail}
         * <p>
         * 注：不再范围内的均按 {@link Config#DebugLevelNone} 处理
         **/
        private final int requestDebugLevel;

        /**
         * 响应信息 Debug 等级
         * 参考：
         * {@link Config#DebugLevelNone}
         * {@link Config#DebugLevelNormal}
         * {@link Config#DebugLevelDetail}
         * <p>
         * 注：不再范围内的均按 {@link Config#DebugLevelNone} 处理
         **/
        private final int responseDebugLevel;


        private Config(Auth auth, int hostRetryMax, int singleHostRetryMax, Retry.Interval retryInterval, Retry.RetryCondition retryCondition, Retry.HostFreezeCondition hostFreezeCondition, HostProvider hostProvider, int hostFreezeDuration, int requestDebugLevel, int responseDebugLevel) {
            this.auth = auth;
            this.hostRetryMax = hostRetryMax;
            this.singleHostRetryMax = singleHostRetryMax;
            this.retryInterval = retryInterval;
            this.retryCondition = retryCondition;
            this.hostFreezeCondition = hostFreezeCondition;
            this.hostProvider = hostProvider;
            this.hostFreezeDuration = hostFreezeDuration;
            this.requestDebugLevel = requestDebugLevel;
            this.responseDebugLevel = responseDebugLevel;
        }

        public static final class Builder {
            private Auth auth;
            private int hostRetryMax;
            private int singleHostRetryMax;
            private Retry.Interval retryInterval;
            private Retry.RetryCondition retryCondition;
            private Retry.HostFreezeCondition hostFreezeCondition;
            private HostProvider hostProvider;
            private int hostFreezeDuration;
            private int requestDebugLevel;
            private int responseDebugLevel;

            public Builder setAuth(Auth auth) {
                this.auth = auth;
                return this;
            }

            public Builder setHostRetryMax(int hostRetryMax) {
                this.hostRetryMax = hostRetryMax;
                return this;
            }

            public Builder setSingleHostRetryMax(int singleHostRetryMax) {
                this.singleHostRetryMax = singleHostRetryMax;
                return this;
            }

            public Builder setRetryInterval(int retryInterval) {
                this.retryInterval = Retry.staticInterval(retryInterval);
                return this;
            }

            public Builder setRetryInterval(Retry.Interval retryInterval) {
                this.retryInterval = retryInterval;
                return this;
            }

            public Builder setRetryCondition(Retry.RetryCondition retryCondition) {
                this.retryCondition = retryCondition;
                return this;
            }

            public Builder setHostFreezeDuration(int hostFreezeDuration) {
                this.hostFreezeDuration = hostFreezeDuration;
                return this;
            }

            public Builder setHostProvider(HostProvider hostProvider) {
                this.hostProvider = hostProvider;
                return this;
            }

            public Builder setHostFreezeCondition(Retry.HostFreezeCondition hostFreezeCondition) {
                this.hostFreezeCondition = hostFreezeCondition;
                return this;
            }

            public Builder setRequestDebugLevel(int requestDebugLevel) {
                this.requestDebugLevel = requestDebugLevel;
                return this;
            }

            public Builder setResponseDebugLevel(int responseDebugLevel) {
                this.responseDebugLevel = responseDebugLevel;
                return this;
            }

            public Config build() {
                return new Config(auth, hostRetryMax, singleHostRetryMax, retryInterval, retryCondition, hostFreezeCondition, hostProvider, hostFreezeDuration, requestDebugLevel, responseDebugLevel);
            }
        }
    }

    /**
     * api 请求基类
     */
    public static class Request implements Cloneable {

        /**
         * 请求的 scheme
         * eg: https
         */
        private String scheme;

        /**
         * 请求的域名
         */
        private String host;

        /**
         * 请求服务的端口号
         */
        private int port;

        /**
         * 请求 url 的 path
         */
        private String path;

        /**
         * 请求 url 的 信息，最终会被按顺序拼接作为 path /Segment0/Segment1
         */
        private List<String> pathSegments = new ArrayList<>();

        /**
         * 请求 url 的 query
         * 由 queryInfo 拼接
         */
        private String query;

        /**
         * 请求 url 的 query 信息，最终会被拼接作为 query key0=value0&key1=value1
         */
        private List<Pair<String, String>> queryPairs = new ArrayList<>();

        /**
         * Http 请求方式
         */
        private MethodType method = MethodType.GET;

        /**
         * 请求头
         */
        private Map<String, String> header = new HashMap<>();


        /**
         * 请求 body
         **/
        private Request.Body body;

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
            try {
                URL url = new URL(urlPrefix);
                this.scheme = url.getProtocol();
                this.host = url.getHost();
                this.port = url.getPort();
                this.addPathSegment(url.getPath());

                String query = url.getQuery();
                if (StringUtils.isNullOrEmpty(query)) {
                    return;
                }
                String[] queryKVs = query.split("&");
                if (queryKVs == null || queryKVs.length == 0) {
                    return;
                }
                for (String kv : queryKVs) {
                    if (StringUtils.isNullOrEmpty(kv)) {
                        continue;
                    }
                    String[] keyValue = kv.split("=", 2);
                    if (keyValue == null || keyValue.length != 2) {
                        continue;
                    }
                    this.addQueryPair(keyValue[0], keyValue[1]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected Request(String scheme, String host) {
            this.scheme = scheme;
            this.host = host;
        }

        /**
         * 获取请求的 urlPrefix， scheme + host
         * eg: https://upload.qiniu.com
         *
         * @return urlPrefix
         */
        public String getUrlPrefix() {
            return scheme + "://" + host;
        }

        /**
         * 获取 请求的 Host
         *
         * @return Host
         * @throws QiniuException 解析 urlPrefix 时的异常
         */
        public String getHost() throws QiniuException {
            return host;
        }

        void setHost(String host) {
            this.host = host;
        }

        /**
         * 添加 path 信息，注意 path 添加顺序
         * 所有的 path item 最终会按照顺序被拼接作为 path
         * eg: /item0/item1
         *
         * @param segment 被添加 path segment
         */
        protected void addPathSegment(String segment) {
            if (StringUtils.isNullOrEmpty(segment)) {
                return;
            }
            pathSegments.add(segment);
            path = null;
        }

        String getMethodString() {
            if (method == null) {
                return "GET";
            }
            return method.toString();
        }

        MethodType getMethod() {
            if (method == null) {
                return MethodType.GET;
            }
            return method;
        }

        /**
         * 获取 url 的 path 信息
         *
         * @return path 信息
         * @throws QiniuException 异常
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
            path = StringUtils.join(pathSegments, "/");
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
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
            if (method == null) {
                return;
            }
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

        private void removeHeaderField(String key) {
            if (StringUtils.isNullOrEmpty(key)) {
                return;
            }
            header.remove(key);
        }

        /**
         * 获取请求头信息
         *
         * @return 请求头信息
         * @throws QiniuException 异常
         */
        public StringMap getHeader() throws QiniuException {
            StringMap header = new StringMap();
            for (String key : this.header.keySet()) {
                header.put(key, this.header.get(key));
            }

            if (body == null || body.contentType == null
                    || header.keySet().contains("Content-Type")) {
                return header;
            }

            header.put("Content-Type", body.contentType.toString());

            return header;
        }

        /**
         * 获取 URL
         *
         * @return url
         * @throws QiniuException 异常
         */
        public URL getUrl() throws QiniuException {
            if (StringUtils.isNullOrEmpty(scheme)) {
                throw QiniuException.unrecoverable("scheme is empty, check if scheme is set or your url format is correct.");
            }

            if (StringUtils.isNullOrEmpty(host)) {
                throw QiniuException.unrecoverable("host is empty, check if host is set or your url format is correct.");
            }

            try {
                String file = "";
                String path = getPath();
                if (!StringUtils.isNullOrEmpty(path)) {
                    file += path;
                }

                String query = getQuery();
                if (!StringUtils.isNullOrEmpty(query)) {
                    file += '?' + query;
                }
                return new URL(scheme, host, port, file);
            } catch (Exception e) {
                throw new QiniuException(e);
            }
        }

        /**
         * 设置请求体
         * 请求数据：在 body 中，从 bodyOffset 开始，获取 bodySize 大小的数据作为请求体
         * 此方式配置的 body 支持重试，支持 auth
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
            this.body = new Request.Body.BytesBody(body, offset, size, contentType);
        }

        /**
         * 设置请求体
         * 此方式配置的 body 不支持重试，不支持 auth
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
            Request.Body.InputStreamBody b = new Request.Body.InputStreamBody(body, contentType, limitSize);
            b.streamBodySinkSize = streamBodySinkSize;
            this.body = b;
        }

        /**
         * 设置表单请求体
         * 此方式配置的 body 支持重试，不支持 auth
         *
         * @param name        表单 name 【必须】
         * @param fileName    表单 fileName
         * @param fields      表单 fields
         * @param body        表单 byte[] 类型 body 【必须】
         * @param contentType 表单 body 的 Mime type
         **/
        protected void setFormBody(String name, String fileName, StringMap fields, byte[] body, String contentType) {
            if (StringUtils.isNullOrEmpty(contentType)) {
                contentType = Client.DefaultMime;
            }
            this.body = new Request.Body.FormBody(name, fileName, fields, body, contentType);
        }

        /**
         * 设置表单请求体
         * 此方式配置的 body 支持重试，不支持 auth
         *
         * @param name        表单 name 【必须】
         * @param fileName    表单 fileName
         * @param fields      表单 fields
         * @param body        表单 File 类型 body 【必须】
         * @param contentType 表单 body 的 Mime type
         **/
        protected void setFormBody(String name, String fileName, StringMap fields, File body, String contentType) {
            if (StringUtils.isNullOrEmpty(contentType)) {
                contentType = Client.DefaultMime;
            }
            this.body = new Request.Body.FormBody(name, fileName, fields, body, contentType);
        }

        /**
         * 使用 streamBody 时，每次读取 streamBody 的大小，读取后发送
         * 默认：{@link Api.Request#streamBodySinkSize}
         * 相关：{@link RequestStreamBody#writeTo(BufferedSink) sinkSize}
         *
         * @param streamBodySinkSize 每次读取 streamBody 的大小
         * @return Request
         */
        public Request setStreamBodySinkSize(long streamBodySinkSize) {
            this.streamBodySinkSize = streamBodySinkSize;
            if (body != null && body instanceof Request.Body.InputStreamBody) {
                ((Body.InputStreamBody) body).streamBodySinkSize = streamBodySinkSize;
            }
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
            if (!hasBody()) {
                return Body.BytesBody.empty().get();
            }

            if (body instanceof Body.InputStreamBody) {
                ((Body.InputStreamBody) body).streamBodySinkSize = streamBodySinkSize;
            }

            return body.get();
        }

        byte[] getBytesBody() {
            if (!hasBody()) {
                return null;
            }
            return body.getBytes();
        }

        /**
         * 构造 body 信息，如果需要设置请求体，子类需要重写
         *
         * @throws QiniuException 异常
         */
        protected void buildBodyInfo() throws QiniuException {

        }

        boolean canRetry() {
            if (!hasBody()) {
                return true;
            }

            return body.canReset();
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

        public Request clone() {
            try {
                Request clone = (Request) super.clone();
                clone.pathSegments = new ArrayList<>(pathSegments);
                clone.queryPairs = new ArrayList<>(queryPairs);
                clone.header = new HashMap<>(header);
                return clone;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
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

        private abstract static class Body {

            protected final MediaType contentType;

            protected Body(String contentType) {
                this.contentType = MediaType.parse(contentType);
            }

            protected boolean canReset() {
                return false;
            }

            protected void reset() throws QiniuException {
                throw QiniuException.unrecoverable("not support reset");
            }

            protected abstract RequestBody get();

            protected byte[] getBytes() {
                return null;
            }

            private static final class BytesBody extends Body {
                private final byte[] bytes;
                private final int offset;
                private final int length;

                private static BytesBody empty() {
                    return new BytesBody(new byte[0], 0, 0, Client.DefaultMime);
                }

                private BytesBody(byte[] bytes, int offset, int length, String contentType) {
                    super(contentType);
                    this.bytes = bytes;
                    this.offset = offset;
                    this.length = length;
                }

                @Override
                protected boolean canReset() {
                    return true;
                }

                @Override
                protected void reset() throws QiniuException {
                }

                @Override
                protected RequestBody get() {
                    return RequestBody.create(contentType, bytes, offset, length);
                }

                @Override
                public byte[] getBytes() {
                    if (bytes == null || bytes.length == 0) {
                        return new byte[]{};
                    }
                    if (offset == 0 && length == bytes.length) {
                        return bytes;
                    }
                    return Arrays.copyOfRange(bytes, offset, offset + length);
                }
            }

            private static final class InputStreamBody extends Body {
                private final InputStream stream;

                private final long limitSize;

                private long streamBodySinkSize = 1024 * 10;

                private InputStreamBody(InputStream stream, String contentType, long limitSize) {
                    super(contentType);
                    this.stream = stream;
                    this.limitSize = limitSize;
                }

                @Override
                protected RequestBody get() {
                    RequestStreamBody b = new RequestStreamBody(stream, contentType, limitSize);
                    b.setSinkSize(streamBodySinkSize);
                    return b;
                }
            }

            private static final class FormBody extends Body {
                private final String name;
                private final String fileName;
                private final StringMap fields;
                private final byte[] bytes;
                private final File file;

                private FormBody(String name, String fileName, StringMap fields, byte[] body, String contentType) {
                    super(contentType);
                    this.name = name;
                    this.fileName = fileName;
                    this.fields = fields;
                    this.bytes = body;
                    this.file = null;
                }

                private FormBody(String name, String fileName, StringMap fields, File body, String contentType) {
                    super(contentType);
                    this.name = name;
                    this.fileName = fileName;
                    this.fields = fields;
                    this.bytes = null;
                    this.file = body;
                }

                @Override
                protected boolean canReset() {
                    return true;
                }

                @Override
                protected void reset() throws QiniuException {
                }

                @Override
                protected RequestBody get() {
                    RequestBody body = null;
                    if (bytes != null) {
                        body = RequestBody.create(contentType, bytes);
                    } else if (file != null) {
                        body = RequestBody.create(contentType, file);
                    } else {
                        body = RequestBody.create(contentType, new byte[0]);
                    }

                    final MultipartBody.Builder b = new MultipartBody.Builder();
                    b.addFormDataPart(name, fileName, body);

                    fields.forEach(new StringMap.Consumer() {
                        @Override
                        public void accept(String key, Object value) {
                            b.addFormDataPart(key, value.toString());
                        }
                    });

                    b.setType(MediaType.parse("multipart/form-data"));

                    return b.build();
                }
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
        private StringMap dataMap;

        /**
         * 响应数据
         */
        private Object[] dataArray;

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
            this.response = response;
            if (response == null) {
                return;
            }

            String bodyString = response.bodyString();
            try {
                if (bodyString.startsWith("[")) {
                    this.dataArray = Json.decodeArray(bodyString);
                } else {
                    this.dataMap = Json.decode(bodyString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 获取 response data array，当 response body 为 json，且为数组时有值
         *
         * @return data array
         */
        public Object[] getDataArray() {
            return dataArray;
        }

        /**
         * 获取 response data map，当 response body 为 json，且为键值对时有值
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


    interface Handler {
        Response handle(Request request) throws QiniuException;
    }

    abstract static class Interceptor {

        static final int PriorityDefault = 100;
        static final int PriorityRetryHosts = 200;
        static final int PriorityRetrySimple = 300;
        static final int PrioritySetHeader = 400;
        static final int PriorityNormal = 500;
        static final int PriorityAuth = 600;
        static final int PriorityDebug = 700;

        /**
         * 拦截器，优先级，越小优先级越高
         * <p>
         * 默认：{@link Interceptor#PriorityNormal}
         **/
        int priority() {
            return PriorityNormal;
        }

        /**
         * 拦截方法
         **/
        abstract Api.Response intercept(Api.Request request, Handler handler) throws QiniuException;
    }
}
