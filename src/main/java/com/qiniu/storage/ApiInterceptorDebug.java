package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

final class ApiInterceptorDebug extends Api.Interceptor {

    /**
     * 不输出 Debug 信息
     **/
    static final int LevelPrintNone = 0;

    /**
     * 输出除 Body 以外的信息
     **/
    static final int LevelPrintNormal = 1;

    /**
     * 输出所有的信息，流式 body 不会输出
     **/
    static final int LevelPrintDetail = 2;

    private final int requestLevel;

    private final int responseLevel;

    private static boolean isNonePrintLevel(int level) {
        return level != LevelPrintNormal && level != LevelPrintDetail;
    }

    private ApiInterceptorDebug(int requestLevel, int responseLevel) {
        this.requestLevel = requestLevel;
        this.responseLevel = responseLevel;
    }

    @Override
    int priority() {
        return Api.Interceptor.PriorityDebug;
    }

    @Override
    Api.Response intercept(final Api.Request request, Api.Handler handler) throws QiniuException {
        if (request == null || (isNonePrintLevel(requestLevel) && isNonePrintLevel(responseLevel))) {
            return handler.handle(request);
        }

        String label = request.getUrl().toString();
        printRequest(label, request);

        QiniuException exception = null;
        Api.Response response = null;
        try {
            response = handler.handle(request);
        } catch (QiniuException e) {
            exception = e;
        }

        printResponse(label, response, exception);

        if (exception != null) {
            throw exception;
        }
        return response;
    }

    private void printRequest(String label, Api.Request request) throws QiniuException {
        if (isNonePrintLevel(requestLevel)) {
            return;
        }

        StringBuilder info = new StringBuilder(label + " Request:\n");
        info.append(request.getMethodString()).append(" ").append(request.getPath()).append("\n");
        StringMap header = request.getHeader();
        for (String key : header.keySet()) {
            Object value = header.get(key);
            info.append(key).append(": ").append(value).append("\n");
        }

        if (requestLevel == LevelPrintDetail) {
            byte[] body = request.getBytesBody();
            if (body != null) {
                info.append(StringUtils.utf8String(body)).append("\n");
            }
        }

        System.out.println(info);
    }

    private void printResponse(String label, Api.Response response, Exception e) throws QiniuException {
        if (isNonePrintLevel(responseLevel)) {
            return;
        }

        if (response != null && response.getResponse() != null) {
            StringBuilder info = new StringBuilder(label + " Response:\n");
            Response resp = response.getResponse();
            if (resp.getResponse() != null) {
                okhttp3.Response r = resp.getResponse();
                info.append(r.protocol()).append(" ").append(r.code()).append(r.message()).append("\n");
                okhttp3.Headers headers = r.headers();
                for (String key : headers.names()) {
                    Object value = headers.get(key);
                    info.append(key).append(": ").append(value).append("\n");
                }

                if (responseLevel == LevelPrintDetail && resp.body() != null) {
                    info.append(new String(resp.body())).append("\n");
                }
            } else {
                info.append(resp).append("\n");
            }
            System.out.println(info);
        } else if (e != null) {
            System.out.println(label + " Exception:\n" + e + "\n");
        } else {
            String info = label + " Nothing:\n";
            System.out.println(info);
        }
    }

    static final class Builder {

        private int requestLevel;

        private int responseLevel;

        /**
         * @param requestLevel 请求 Debug 等级，详见：
         *                     {@link ApiInterceptorDebug#LevelPrintNone}
         *                     {@link ApiInterceptorDebug#LevelPrintNormal}
         *                     {@link ApiInterceptorDebug#LevelPrintDetail}
         *                     <p>
         *                     注：不再范围内的均按 {@link ApiInterceptorDebug#LevelPrintNone} 处理
         **/
        Builder setRequestLevel(int requestLevel) {
            this.requestLevel = requestLevel;
            return this;
        }

        /**
         * @param responseLevel 请求 Debug 等级，详见：
         *                      {@link ApiInterceptorDebug#LevelPrintNone}
         *                      {@link ApiInterceptorDebug#LevelPrintNormal}
         *                      {@link ApiInterceptorDebug#LevelPrintDetail}
         *                      <p>
         *                      注：不再范围内的均按 {@link ApiInterceptorDebug#LevelPrintNone} 处理
         **/
        Builder setResponseLevel(int responseLevel) {
            this.responseLevel = responseLevel;
            return this;
        }

        Api.Interceptor build() {
            return new ApiInterceptorDebug(requestLevel, responseLevel);
        }
    }
}
