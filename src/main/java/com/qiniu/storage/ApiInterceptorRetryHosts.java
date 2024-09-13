package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

class ApiInterceptorRetryHosts extends Api.Interceptor {
    private final int retryMax;
    private final Retry.Interval retryInterval;
    private final Retry.RetryCondition retryCondition;
    private final int hostFreezeDuration;
    private final Retry.HostFreezeCondition hostFreezeCondition;
    private final HostProvider hostProvider;

    @Override
    int priority() {
        return Api.Interceptor.PriorityRetryHosts;
    }

    private ApiInterceptorRetryHosts(int retryMax,
                                     Retry.Interval retryInterval,
                                     Retry.RetryCondition retryCondition,
                                     int hostFreezeDuration,
                                     Retry.HostFreezeCondition hostFreezeCondition,
                                     HostProvider hostProvider) {
        this.retryMax = retryMax;
        this.retryInterval = retryInterval;
        this.retryCondition = retryCondition;
        this.hostFreezeDuration = hostFreezeDuration;
        this.hostFreezeCondition = hostFreezeCondition;
        this.hostProvider = hostProvider;
    }

    @Override
    Api.Response intercept(Api.Request request, Api.Handler handler) throws QiniuException {
        if (request == null || hostProvider == null) {
            return handler.handle(request);
        }

        String reqHost = request.getHost();
        if (!hostProvider.isHostValid(reqHost)) {
            // 支持不配置默认的 host，未配置则从 provider 中获取
            String firstHost = hostProvider.provider();
            if (!StringUtils.isNullOrEmpty(firstHost)) {
                request.setHost(firstHost);
            } else {
                throw QiniuException.unrecoverable("no host provide");
            }
        }

        if (retryMax == 0) {
            return handler.handle(request);
        }

        QiniuException exception = null;
        Api.Response response = null;
        for (int i = 0; ; i++) {
            exception = null;
            response = null;
            String host = request.getHost();
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

            if (hostFreezeCondition.shouldFreezeHost(request, response, exception)) {
                hostProvider.freezeHost(host, hostFreezeDuration);
            }

            if (cloneRequest == null) {
                break;
            }
            request = cloneRequest;

            String newHost = hostProvider.provider();
            if (StringUtils.isNullOrEmpty(newHost)) {
                break;
            }
            if (!newHost.equals(host)) {
                request.setHost(newHost);
            }

            if (response != null && response.getResponse() != null) {
                response.getResponse().close();
            }

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
        private int hostFreezeDuration;
        private Retry.HostFreezeCondition hostFreezeCondition;
        private HostProvider hostProvider;

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

        Builder setHostFreezeDuration(int hostFreezeDuration) {
            this.hostFreezeDuration = hostFreezeDuration;
            return this;
        }

        Builder setHostFreezeCondition(Retry.HostFreezeCondition hostFreezeCondition) {
            this.hostFreezeCondition = hostFreezeCondition;
            return this;
        }

        Builder setHostProvider(HostProvider hostProvider) {
            this.hostProvider = hostProvider;
            return this;
        }

        /**
         * 构建拦截器
         * <p>
         * 注：hostProvider 必须配置，否则不会重试：{@link ApiInterceptorRetryHosts.Builder#setHostProvider}
         **/
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

            if (hostFreezeDuration <= 0) {
                hostFreezeDuration = 600 * 1000;
            }

            if (hostFreezeCondition == null) {
                hostFreezeCondition = Retry.defaultHostFreezeCondition();
            }

            return new ApiInterceptorRetryHosts(retryMax, retryInterval, retryCondition,
                    hostFreezeDuration, hostFreezeCondition, hostProvider);
        }
    }
}
