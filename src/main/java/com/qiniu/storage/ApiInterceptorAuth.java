package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Headers;
import com.qiniu.util.Auth;

/**
 * 仅是 Qiniu 签名，Body 必须是 bytes[]
 **/
final class ApiInterceptorAuth extends Api.Interceptor {

    private final Auth auth;

    private ApiInterceptorAuth(Auth auth) {
        this.auth = auth;
    }

    @Override
    int priority() {
        return Api.Interceptor.PriorityAuth;
    }

    @Override
    Api.Response intercept(Api.Request request, Api.Handler handler) throws QiniuException {
        if (auth == null || request == null) {
            return handler.handle(request);
        }

        if (request.getAuthType() == Api.Request.AuthTypeQiniu) {
            String url = request.getUrl().toString();
            String method = request.getMethodString();
            Headers headers = Headers.of(request.getHeader());
            byte[] body = request.getBytesBody();
            String authorization = "Qiniu " + auth.signQiniuAuthorization(url, method, body, headers);
            request.addHeaderField("Authorization", authorization);
        }

        return handler.handle(request);
    }

    static final class Builder {

        private Auth auth;

        Builder setAuth(Auth auth) {
            this.auth = auth;
            return this;
        }

        Builder setAuthType(int authType) {
            return this;
        }

        Api.Interceptor build() {
            return new ApiInterceptorAuth(auth);
        }
    }
}
