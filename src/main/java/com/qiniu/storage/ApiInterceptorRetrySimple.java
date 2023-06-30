package com.qiniu.storage;

import com.qiniu.common.QiniuException;

final class ApiInterceptorRetrySimple extends Api.Interceptor {

    private final int retryMax;
    private final Retry.Interval retryInterval;
    private final Retry.RetryCondition retryCondition;

    private ApiInterceptorRetrySimple(int retryMax, Retry.Interval retryInterval, Retry.RetryCondition retryCondition) {
        this.retryMax = retryMax;
        this.retryInterval = retryInterval;
        this.retryCondition = retryCondition;
    }

    @Override
    int priority() {
        return Api.Interceptor.PriorityRetrySimple;
    }

    @Override
    Api.Response intercept(Api.Request request, Api.Handler handler) throws QiniuException {
        if (request == null || retryMax == 0) {
            return handler.handle(request);
        }

        QiniuException exception = null;
        Api.Response response = null;
        for (int i = 0; ; i++) {
            exception = null;
            response = null;
            Api.Request cloneRequest = request.clone();
            try {
                response = handler.handle(request);
            } catch (QiniuException e) {
                exception = e;
            }

            if (i >= retryMax) {
                break;
            }

            if (!retryCondition.shouldRetry(request, response, exception)) {
                break;
            }

            if (cloneRequest == null) {
                break;
            }
            request = cloneRequest;

            int interval = retryInterval.interval();
            if (interval <= 0) {
                continue;
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException ignore) {
            }
        }

        if (exception != null) {
            throw exception;
        }

        return response;
    }

    static final class Builder {
        private int retryMax;
        private Retry.Interval retryInterval;
        private Retry.RetryCondition retryCondition;

        Builder setRetryMax(int retryMax) {
            this.retryMax = retryMax;
            return this;
        }

        Builder setRetryInterval(Retry.Interval retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        Builder setRetryCondition(Retry.RetryCondition retryCondition) {
            this.retryCondition = retryCondition;
            return this;
        }

        Api.Interceptor build() {
            if (retryMax < 0) {
                retryMax = 0;
            }

            if (retryInterval == null) {
                retryInterval = Retry.defaultInterval();
            }

            if (retryCondition == null) {
                retryCondition = Retry.defaultCondition();
            }

            return new ApiInterceptorRetrySimple(retryMax, retryInterval, retryCondition);
        }
    }
}
