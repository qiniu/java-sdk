package com.qiniu.http;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class RequestStreamBody extends RequestBody {

    private long limitSize = -1;
    private long sinkSize = 1024 * 100;
    private final MediaType type;
    private final InputStream stream;

    /**
     * 构造函数
     *
     * @param stream      请求数据流
     * @param contentType 请求数据类型
     */
    public RequestStreamBody(InputStream stream, String contentType) {
        this.stream = stream;
        this.type = MediaType.parse(contentType);
    }

    /**
     * 构造函数
     *
     * @param stream      请求数据流
     * @param contentType 请求数据类型
     */
    public RequestStreamBody(InputStream stream, MediaType contentType) {
        this.stream = stream;
        this.type = contentType;
    }

    /**
     * 构造函数
     *
     * @param stream      请求数据流
     * @param contentType 请求数据类型
     * @param limitSize   最大读取 stream 的大小；为 -1 时不限制读取所有
     */
    public RequestStreamBody(InputStream stream, MediaType contentType, long limitSize) {
        this.stream = stream;
        this.type = contentType;
        this.limitSize = limitSize;
    }

    /**
     * 配置请求时，每次从流中读取的数据大小
     *
     * @param sinkSize 每次从流中读取的数据大小
     * @return RequestStreamBody
     * @see RequestStreamBody#writeTo(BufferedSink)
     */
    public RequestStreamBody setSinkSize(long sinkSize) {
        if (sinkSize > 0) {
            this.sinkSize = sinkSize;
        }
        return this;
    }

    @Override
    public MediaType contentType() {
        return type;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(stream)) {
            int offset = 0;
            while (limitSize < 0 || offset < limitSize) {
                long byteSize = sinkSize;
                if (offset < limitSize) {
                    byteSize = Math.min(sinkSize, limitSize - offset);
                }

                try {
                    sink.write(source, byteSize);
                    sink.flush();
                    offset += byteSize;
                } catch (EOFException e) {
                    break;
                }
            }
        }
    }
}
