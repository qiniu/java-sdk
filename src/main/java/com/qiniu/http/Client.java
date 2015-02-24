package com.qiniu.http;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.squareup.okhttp.*;
import okio.BufferedSink;

import java.io.File;
import java.io.IOException;
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
        httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS);
        httpClient.setReadTimeout(Config.RESPONSE_TIMEOUT, TimeUnit.SECONDS);
    }

    private static String userAgent() {
        String javaVersion = "Java/" + System.getProperty("java.version");
        String os = System.getProperty("os.name") + " "
                + System.getProperty("os.arch") + " " + System.getProperty("os.version");
        String sdk = "QiniuJava/" + Config.VERSION;
        return sdk + " (" + os + ") " + javaVersion;
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

    private static RequestBody create(final MediaType contentType, final byte[] content, final int offset, final int size) {
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

    public Response post(String url, StringMap params, StringMap headers) throws QiniuException {
        final FormEncodingBuilder f = new FormEncodingBuilder();
        params.iterate(new StringMap.Do() {
            @Override
            public void deal(String key, Object value) {
                f.add(key, value.toString());
            }
        });
        return post(url, f.build(), headers);
    }

    public Response post(String url, byte[] body, StringMap headers, String contentType) throws QiniuException {
        RequestBody rbody = null;
        if (body != null && body.length > 0) {
            MediaType t = MediaType.parse(contentType);

            rbody = RequestBody.create(t, body);
        }
        return post(url, rbody, headers);
    }

    public Response post(String url, byte[] body, int offset, int size, StringMap headers, String contentType) throws QiniuException {
        RequestBody rbody = null;
        if (body != null && body.length > 0) {
            MediaType t = MediaType.parse(contentType);
            rbody = create(t, body, offset, size);
        }
        return post(url, rbody, headers);
    }

    private Response post(String url, RequestBody body, StringMap headers) throws QiniuException {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (body != null) {
            requestBuilder.post(body);
        }
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
        final MultipartBuilder mb = new MultipartBuilder();
        mb.addFormDataPart(name, fileName, file);

        fields.iterate(new StringMap.Do() {
            @Override
            public void deal(String key, Object value) {
                mb.addFormDataPart(key, value.toString());
            }
        });
        mb.type(MediaType.parse("multipart/form-data"));
        RequestBody body = mb.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        return send(requestBuilder, headers);
    }

    public Response send(final Request.Builder requestBuilder, StringMap headers) throws QiniuException {
        headers.iterate(new StringMap.Do() {
            @Override
            public void deal(String key, Object value) {
                requestBuilder.header(key, value.toString());
            }
        });

        requestBuilder.header("User-Agent", userAgent());
        long start = System.currentTimeMillis();
        com.squareup.okhttp.Response res = null;
        Response r;
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        try {
            res = httpClient.newCall(requestBuilder.build()).execute();
            r = new Response(res, duration);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QiniuException(e);
        }
        if (r.statusCode >= 300) {
            throw new QiniuException(r);
        }
        return r;
    }
}
