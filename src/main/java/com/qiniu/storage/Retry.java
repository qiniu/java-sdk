package com.qiniu.storage;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

public final class Retry {
    private Retry() {
    }

    static boolean canSwitchRegionAndRetry(Response response, QiniuException exception) {
        Response checkResponse = response;
        if (checkResponse == null && exception != null) {
            checkResponse = exception.response;
        }

        if (checkResponse != null) {
            int statusCode = checkResponse.statusCode;
            return (statusCode > -2 && statusCode < 200) || (statusCode > 299
                    && statusCode != 401 && statusCode != 413 && statusCode != 419
                    && statusCode != 608 && statusCode != 614 && statusCode != 630);
        }

        return exception == null || !exception.isUnrecoverable();
    }

    static Boolean canRequestRetryAgain(Response response, QiniuException exception) {
        if (response != null) {
            return response.needRetry();
        }

        if (exception == null || exception.isUnrecoverable()) {
            return false;
        }

        if (exception.response != null) {
            return exception.response.needRetry();
        }

        return true;
    }

    static Boolean requestShouldSwitchHost(Response response, QiniuException exception) {
        if (response != null) {
            return response.needSwitchServer();
        }

        if (exception == null) {
            return true;
        }

        if (exception.isUnrecoverable()) {
            return false;
        }

        if (exception.response != null) {
            return exception.response.needSwitchServer();
        }

        return true;
    }

    public interface Interval {

        /**
         * 重试时间间隔，单位：毫秒
         **/
        int interval();
    }

    public static Interval defaultInterval() {
        return staticInterval(200);
    }

    public static Interval staticInterval(final int interval) {
        return new Retry.Interval() {
            @Override
            public int interval() {
                return interval;
            }
        };
    }

    public interface RetryCondition {

        /**
         * 是否需要重试
         **/
        boolean shouldRetry(Api.Request request, Api.Response response, QiniuException exception);
    }

    public static RetryCondition defaultCondition() {
        return new RetryCondition() {
            @Override
            public boolean shouldRetry(Api.Request request, Api.Response response, QiniuException exception) {
                return request.canRetry() && canRequestRetryAgain(response != null ? response.getResponse() : null, exception);
            }
        };
    }

    public interface HostFreezeCondition {
        boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception);
    }

    public static HostFreezeCondition defaultHostFreezeCondition() {
        return new HostFreezeCondition() {
            @Override
            public boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception) {
                return true;
            }
        };
    }
}
