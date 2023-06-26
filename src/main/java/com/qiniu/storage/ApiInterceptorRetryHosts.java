package com.qiniu.storage;

import com.qiniu.common.QiniuException;

public class ApiInterceptorRetryHosts extends Api.Interceptor {
    private final int retryMax;
    private final Retry.Interval retryInterval;
    private final Retry.Condition retryCondition;
    private final int hostFreezeDuration;
    private final HostFreezeCondition hostFreezeCondition;
    private final HostProvider hostProvider;

    @Override
    int priority() {
        return Api.Interceptor.PriorityRetryHosts;
    }

    private ApiInterceptorRetryHosts(int retryMax,
                                     Retry.Interval retryInterval,
                                     Retry.Condition retryCondition,
                                     int hostFreezeDuration,
                                     HostFreezeCondition hostFreezeCondition,
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
        if (retryMax == 0 || hostProvider == null) {
            return handler.handle(request);
        }

        QiniuException exception = null;
        Api.Response response = null;
        for (int i = 0; ; i++) {
            exception = null;
            response = null;
            Api.Request cloneRequest = request.clone();
            String host = request.getHost();

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

            int interval = retryInterval.interval();
            if (interval <= 0) {
                continue;
            }

            String newHost = hostProvider.provider();
            if (!newHost.equals(host)) {
                request.setHost(host);
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

    interface HostFreezeCondition {
        boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception);
    }

    static HostFreezeCondition defaultHostFreezeCondition() {
        return new HostFreezeCondition() {
            @Override
            public boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception) {
                return true;
            }
        };
    }

    public static class Builder {
        private int retryMax;
        private Retry.Interval retryInterval;
        private Retry.Condition retryCondition;
        private int hostFreezeDuration;
        private HostFreezeCondition hostFreezeCondition;
        private HostProvider hostProvider;

        public Builder setRetryMax(int retryMax) {
            this.retryMax = retryMax;
            return this;
        }

        public Builder setRetryInterval(Retry.Interval retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public Builder setRetryCondition(Retry.Condition retryCondition) {
            this.retryCondition = retryCondition;
            return this;
        }

        public Builder setHostFreezeDuration(int hostFreezeDuration) {
            this.hostFreezeDuration = hostFreezeDuration;
            return this;
        }

        public Builder setHostFreezeCondition(HostFreezeCondition hostFreezeCondition) {
            this.hostFreezeCondition = hostFreezeCondition;
            return this;
        }

        public Builder setHostProvider(HostProvider hostProvider) {
            this.hostProvider = hostProvider;
            return this;
        }

        public Api.Interceptor build() {
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
                hostFreezeCondition = defaultHostFreezeCondition();
            }

            return new ApiInterceptorRetryHosts(retryMax, retryInterval, retryCondition,
                    hostFreezeDuration, hostFreezeCondition, hostProvider);
        }
    }
}
