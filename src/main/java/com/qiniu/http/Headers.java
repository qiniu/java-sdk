package com.qiniu.http;

import com.qiniu.util.StringMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Headers {
    // 代理了部分 okhttp3.Headers 方法 //
    okhttp3.Headers innerHeaders;

    Headers(Builder builder) {
        innerHeaders = builder.innerBuilder.build();
    }

    private Headers() {

    }

    public static Headers of(Map<String, String> headers) {
        Headers inner = new Headers();
        inner.innerHeaders = okhttp3.Headers.of(headers);
        return inner;
    }

    public static Headers of(StringMap headers) {
        final Builder builder = new Builder();
        if (headers == null) {
            return builder.build();
        }
        headers.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                if (null != value) {
                    builder.add(key, value.toString());
                }
            }
        });
        return builder.build();
    }

    /**
     * Returns the last value corresponding to the specified field, or null.
     */
    public String get(String name) {
        return innerHeaders.get(name);
    }

    /**
     * Returns an immutable case-insensitive set of header names.
     */
    public Set<String> names() {
        return innerHeaders.names();
    }

    /**
     * Returns an immutable list of the header values for {@code name}.
     */
    public List<String> values(String name) {
        return innerHeaders.values(name);
    }

    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.innerBuilder = innerHeaders.newBuilder();
        return builder;
    }

    public static final class Builder {
        // 代理了部分 okhttp3.Headers.Builder 方法 //
        private okhttp3.Headers.Builder innerBuilder;

        public Builder() {
            innerBuilder = new okhttp3.Headers.Builder();
        }

        public Builder add(String name, String value) {
            innerBuilder.add(name, value);
            return this;
        }

        public Builder addAll(Headers headers) {
            innerBuilder.addAll(headers.innerHeaders);
            return this;
        }

        public Builder set(String name, String value) {
            innerBuilder.set(name, value);
            return this;
        }

        public Headers build() {
            return new Headers(this);
        }
    }
}
