package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.DefaultHeader;

class ApiInterceptorDefaultHeader extends Api.Interceptor {

    private ApiInterceptorDefaultHeader() {
    }

    @Override
    int priority() {
        return Api.Interceptor.PrioritySetHeader;
    }

    @Override
    Api.Response intercept(final Api.Request request, Api.Handler handler) throws QiniuException {
        DefaultHeader.setDefaultHeader(new DefaultHeader.HeadAdder() {
            @Override
            public void addHeader(String key, String value) {
                request.addHeaderField(key, value);
            }
        });
        return handler.handle(request);
    }

    static class Builder {

        Api.Interceptor build() {
            return new ApiInterceptorDefaultHeader();
        }
    }
}
