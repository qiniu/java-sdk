package com.qiniu.http;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import okhttp3.*;
import okio.BufferedSink;
import qiniu.happydns.DnsClient;
import qiniu.happydns.Domain;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定义HTTP请求管理相关方法
 */
public final class Client {
    public static final String ContentTypeHeader = "Content-Type";
    public static final String DefaultMime = "application/octet-stream";
    public static final String JsonMime = "application/json";
    public static final String FormMime = "application/x-www-form-urlencoded";
    private final OkHttpClient httpClient;

    public Client() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        ConnectionPool connectionPool = new ConnectionPool(32, 5, TimeUnit.MINUTES);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.dispatcher(dispatcher);
        builder.connectionPool(connectionPool);
        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                okhttp3.Response response = chain.proceed(request);
                IpTag tag = (IpTag) request.tag();
                try {
                    tag.ip = chain.connection().socket().getRemoteSocketAddress().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    tag.ip = "";
                }
                return response;
            }
        });
        if (Config.dns != null) {
            final DnsClient d = Config.dns;
            builder.dns(new Dns() {
                @Override
                public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                    InetAddress[] ips;
                    Domain domain = new Domain(hostname, false, Config.dnsHostFirst);
                    try {
                        ips = d.queryInetAddress(domain);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new UnknownHostException(e.getMessage());
                    }
                    if (ips == null) {
                        throw new UnknownHostException(hostname + " resolve failed");
                    }
                    List<InetAddress> l = new ArrayList<>();
                    Collections.addAll(l, ips);
                    return l;
                }
            });
        }
        final ProxyConfiguration p = Config.proxy;
        if (p != null) {
            builder.proxy(p.proxy());
            if (p.user != null && p.password != null) {
                builder.proxyAuthenticator(p.authenticator());
            }
        }
        builder.connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(Config.RESPONSE_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS);
        httpClient = builder.build();

    }

    private static String userAgent() {
        String javaVersion = "Java/" + System.getProperty("java.version");
        String os = System.getProperty("os.name") + " "
                + System.getProperty("os.arch") + " " + System.getProperty("os.version");
        String sdk = "QiniuJava/" + Config.VERSION;
        return sdk + " (" + os + ") " + javaVersion;
    }

    private static RequestBody create(final MediaType contentType,
                                      final byte[] content, final int offset, final int size) {
        if (content == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return size;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, size);
            }
        };
    }

    public Response get(String url) throws QiniuException {
        return get(url, new StringMap());
    }

    public Response get(String url, StringMap headers) throws QiniuException {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return send(requestBuilder, headers);
    }

    public Response post(String url, byte[] body, StringMap headers) throws QiniuException {
        return post(url, body, headers, DefaultMime);
    }

    public Response post(String url, String body, StringMap headers) throws QiniuException {
        return post(url, StringUtils.utf8Bytes(body), headers, DefaultMime);
    }

    public Response post(String url, StringMap params, StringMap headers) throws QiniuException {
        final FormBody.Builder f = new FormBody.Builder();
        params.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                f.add(key, value.toString());
            }
        });
        return post(url, f.build(), headers);
    }

    public Response post(String url, byte[] body, StringMap headers, String contentType) throws QiniuException {
        RequestBody rbody;
        if (body != null && body.length > 0) {
            MediaType t = MediaType.parse(contentType);
            rbody = RequestBody.create(t, body);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }
        return post(url, rbody, headers);
    }

    public Response post(String url, byte[] body, int offset, int size,
                         StringMap headers, String contentType) throws QiniuException {
        RequestBody rbody;
        if (body != null && body.length > 0) {
            MediaType t = MediaType.parse(contentType);
            rbody = create(t, body, offset, size);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }
        return post(url, rbody, headers);
    }

    private Response post(String url, RequestBody body, StringMap headers) throws QiniuException {
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        return send(requestBuilder, headers);
    }

    public Response multipartPost(String url,
                                  StringMap fields,
                                  String name,
                                  String fileName,
                                  byte[] fileBody,
                                  String mimeType,
                                  StringMap headers) throws QiniuException {
        RequestBody file = RequestBody.create(MediaType.parse(mimeType), fileBody);
        return multipartPost(url, fields, name, fileName, file, headers);
    }

    public Response multipartPost(String url,
                                  StringMap fields,
                                  String name,
                                  String fileName,
                                  File fileBody,
                                  String mimeType,
                                  StringMap headers) throws QiniuException {
        RequestBody file = RequestBody.create(MediaType.parse(mimeType), fileBody);
        return multipartPost(url, fields, name, fileName, file, headers);
    }

    private Response multipartPost(String url,
                                   StringMap fields,
                                   String name,
                                   String fileName,
                                   RequestBody file,
                                   StringMap headers) throws QiniuException {
        final MultipartBody.Builder mb = new MultipartBody.Builder();
        mb.addFormDataPart(name, fileName, file);

        fields.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                mb.addFormDataPart(key, value.toString());
            }
        });
        mb.setType(MediaType.parse("multipart/form-data"));
        RequestBody body = mb.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        return send(requestBuilder, headers);
    }

    public Response send(final Request.Builder requestBuilder, StringMap headers) throws QiniuException {
        if (headers != null) {
            headers.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    requestBuilder.header(key, value.toString());
                }
            });
        }

        requestBuilder.header("User-Agent", userAgent());
        long start = System.currentTimeMillis();
        okhttp3.Response res = null;
        Response r;
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        IpTag tag = new IpTag();
        try {
            res = httpClient.newCall(requestBuilder.tag(tag).build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new QiniuException(e);
        }
        r = Response.create(res, tag.ip, duration);
        if (r.statusCode >= 300) {
            throw new QiniuException(r);
        }

        return r;
    }

    public void asyncSend(final Request.Builder requestBuilder, StringMap headers, final AsyncCallback cb) {
        if (headers != null) {
            headers.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    requestBuilder.header(key, value.toString());
                }
            });
        }

        requestBuilder.header("User-Agent", userAgent());
        final long start = System.currentTimeMillis();
        IpTag tag = new IpTag();
        httpClient.newCall(requestBuilder.tag(tag).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                long duration = (System.currentTimeMillis() - start) / 1000;
                cb.complete(Response.createError(null, "", duration, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                long duration = (System.currentTimeMillis() - start) / 1000;
                cb.complete(Response.create(response, "", duration));
            }
        });
    }

    public void asyncPost(String url, byte[] body, int offset, int size,
                          StringMap headers, String contentType, AsyncCallback cb) {
        RequestBody rbody;
        if (body != null && body.length > 0) {
            MediaType t = MediaType.parse(contentType);
            rbody = create(t, body, offset, size);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }

        Request.Builder requestBuilder = new Request.Builder().url(url).post(rbody);
        asyncSend(requestBuilder, headers, cb);
    }

    public void asyncMultipartPost(String url,
                                   StringMap fields,
                                   String name,
                                   String fileName,
                                   byte[] fileBody,
                                   String mimeType,
                                   StringMap headers,
                                   AsyncCallback cb) {
        RequestBody file = RequestBody.create(MediaType.parse(mimeType), fileBody);
        asyncMultipartPost(url, fields, name, fileName, file, headers, cb);
    }

    public void asyncMultipartPost(String url,
                                   StringMap fields,
                                   String name,
                                   String fileName,
                                   File fileBody,
                                   String mimeType,
                                   StringMap headers,
                                   AsyncCallback cb) throws QiniuException {
        RequestBody file = RequestBody.create(MediaType.parse(mimeType), fileBody);
        asyncMultipartPost(url, fields, name, fileName, file, headers, cb);
    }

    private void asyncMultipartPost(String url,
                                    StringMap fields,
                                    String name,
                                    String fileName,
                                    RequestBody file,
                                    StringMap headers,
                                    AsyncCallback cb) {
        final MultipartBody.Builder mb = new MultipartBody.Builder();
        mb.addFormDataPart(name, fileName, file);

        fields.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                mb.addFormDataPart(key, value.toString());
            }
        });
        mb.setType(MediaType.parse("multipart/form-data"));
        RequestBody body = mb.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        asyncSend(requestBuilder, headers, cb);
    }

    private static class IpTag {
        public String ip = null;
    }
}
