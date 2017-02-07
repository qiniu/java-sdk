package com.qiniu.cdn;

import java.util.Map;

/**
 * Created by jemy on 03/02/2017.
 */
public class CdnResult {
    /**
     * 刷新结果
     *
     * @link https://developer.qiniu.com/fusion/api/cache-refresh
     */
    public class RefreshResult {
        private int code;
        private String error;
        private String requestId;
        private String[] invalidUrls;
        private String[] invalidDirs;
        private int urlQuotaDay;
        private int urlSurplusDay;
        private int dirQuotaDay;
        private int dirSurplusDay;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String[] getInvalidUrls() {
            return invalidUrls;
        }

        public void setInvalidUrls(String[] invalidUrls) {
            this.invalidUrls = invalidUrls;
        }

        public String[] getInvalidDirs() {
            return invalidDirs;
        }

        public void setInvalidDirs(String[] invalidDirs) {
            this.invalidDirs = invalidDirs;
        }

        public int getUrlQuotaDay() {
            return urlQuotaDay;
        }

        public void setUrlQuotaDay(int urlQuotaDay) {
            this.urlQuotaDay = urlQuotaDay;
        }

        public int getUrlSurplusDay() {
            return urlSurplusDay;
        }

        public void setUrlSurplusDay(int urlSurplusDay) {
            this.urlSurplusDay = urlSurplusDay;
        }

        public int getDirQuotaDay() {
            return dirQuotaDay;
        }

        public void setDirQuotaDay(int dirQuotaDay) {
            this.dirQuotaDay = dirQuotaDay;
        }

        public int getDirSurplusDay() {
            return dirSurplusDay;
        }

        public void setDirSurplusDay(int dirSurplusDay) {
            this.dirSurplusDay = dirSurplusDay;
        }
    }

    /**
     * 预取结果
     *
     * @link https://developer.qiniu.com/fusion/api/file-prefetching
     */
    public class PrefetchResult {
        private int code;
        private String error;
        private String requestId;
        private String[] invalidUrls;
        private int quotaDay;
        private int surplusDay;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String[] getInvalidUrls() {
            return invalidUrls;
        }

        public void setInvalidUrls(String[] invalidUrls) {
            this.invalidUrls = invalidUrls;
        }

        public int getQuotaDay() {
            return quotaDay;
        }

        public void setQuotaDay(int quotaDay) {
            this.quotaDay = quotaDay;
        }

        public int getSurplusDay() {
            return surplusDay;
        }

        public void setSurplusDay(int surplusDay) {
            this.surplusDay = surplusDay;
        }
    }

    /**
     * 带宽数据获取结果
     *
     * @link http://developer.qiniu.com/fusion/api/traffic-bandwidth
     */
    public class BandwidthResult {
        private int code;
        private String error;
        private String[] time;
        private Map<String, BandwidthData> data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String[] getTime() {
            return time;
        }

        public void setTime(String[] time) {
            this.time = time;
        }

        public Map<String, BandwidthData> getData() {
            return data;
        }

        public void setData(Map<String, BandwidthData> data) {
            this.data = data;
        }
    }

    public class BandwidthData {
        private Long[] china;
        private Long[] oversea;

        public Long[] getChina() {
            return china;
        }

        public void setChina(Long[] china) {
            this.china = china;
        }

        public Long[] getOversea() {
            return oversea;
        }

        public void setOversea(Long[] oversea) {
            this.oversea = oversea;
        }
    }

    /**
     * 流量数据获取结果
     *
     * @link http://developer.qiniu.com/fusion/api/traffic-bandwidth
     */
    public class FluxResult {
        private int code;
        private String error;
        private String[] time;
        private Map<String, FluxData> data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String[] getTime() {
            return time;
        }

        public void setTime(String[] time) {
            this.time = time;
        }

        public Map<String, FluxData> getData() {
            return data;
        }

        public void setData(Map<String, FluxData> data) {
            this.data = data;
        }
    }

    public class FluxData {
        private Long[] china;
        private Long[] oversea;

        public Long[] getChina() {
            return china;
        }

        public void setChina(Long[] china) {
            this.china = china;
        }

        public Long[] getOversea() {
            return oversea;
        }

        public void setOversea(Long[] oversea) {
            this.oversea = oversea;
        }
    }

    /**
     * 日志数据获取结果
     *
     * @link https://developer.qiniu.com/fusion/api/download-the-log
     */
    public class LogListResult {
        private int code;
        private String error;
        private String[] time;
        private Map<String, LogData[]> data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String[] getTime() {
            return time;
        }

        public void setTime(String[] time) {
            this.time = time;
        }

        public Map<String, LogData[]> getData() {
            return data;
        }

        public void setData(Map<String, LogData[]> data) {
            this.data = data;
        }
    }

    public class LogData {
        private String name;
        private int size;
        private long mtime;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getMtime() {
            return mtime;
        }

        public void setMtime(long mtime) {
            this.mtime = mtime;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
