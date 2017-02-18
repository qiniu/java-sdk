package com.qiniu.cdn;

import java.util.Map;

/**
 * 这里定义了Cdn请求相关回复的封装类
 */
public class CdnResult {
    /**
     * 刷新结果
     * 参考文档：<a href="https://developer.qiniu.com/fusion/api/cache-refresh#refresh-response">缓存刷新</a>
     */
    public class RefreshResult {
        /**
         * 自定义错误码
         */
        public int code;
        /**
         * 自定义错误码描述，字符串
         */
        public String error;
        /**
         * 刷新请求id，只有在任务被接受时才有值
         */
        public String requestId;
        /**
         * 无效的url列表
         */
        public String[] invalidUrls;
        /**
         * 无效的dir列表
         */
        public String[] invalidDirs;
        /**
         * 每日的刷新url限额
         */
        public int urlQuotaDay;
        /**
         * 每日的当前剩余的刷新url限额
         */
        public int urlSurplusDay;
        /**
         * 每日的刷新dir限额
         */
        public int dirQuotaDay;
        /**
         * 每日的当前剩余的刷新dir限额
         */
        public int dirSurplusDay;
    }

    /**
     * 预取结果
     * 参考文档：<a href="https://developer.qiniu.com/fusion/api/file-prefetching">文件预取</a>
     */
    public class PrefetchResult {
        /**
         * 自定义状态码
         */
        public int code;
        /**
         * 自定义错误码描述，字符串
         */
        public String error;
        /**
         * 预取请求 id，只有在任务被接受时才有值
         */
        public String requestId;
        /**
         * 无效的 url 列表
         */
        public String[] invalidUrls;
        /**
         * 每日的预取 url 限额
         */
        public int quotaDay;
        /**
         * 每日的当前剩余的预取 url 限额
         */
        public int surplusDay;
    }

    /**
     * 带宽数据获取结果
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/traffic-bandwidth">流量带宽</a>
     */
    public class BandwidthResult {
        /**
         * 自定义状态码
         */
        public int code;
        /**
         * 自定义错误码描述，字符串
         */
        public String error;
        /**
         * 数据时间点
         */
        public String[] time;
        /**
         * 数据
         */
        public Map<String, BandwidthData> data;
    }

    public class BandwidthData {
        /**
         * 国内数据
         */
        public Long[] china;
        /**
         * 海外数据
         */
        public Long[] oversea;
    }

    /**
     * 流量数据获取结果
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/traffic-bandwidth">流量带宽</a>
     */
    public class FluxResult {
        /**
         * 自定义状态码
         */
        public int code;
        /**
         * 自定义错误码描述，字符串
         */
        public String error;
        /**
         * 数据时间点
         */
        public String[] time;
        /**
         * 数据
         */
        public Map<String, FluxData> data;
    }

    public class FluxData {
        /**
         * 国内数据
         */
        public Long[] china;
        /**
         * 海外数据
         */
        public Long[] oversea;
    }

    /**
     * 日志数据获取结果
     * 参考文档：<a href="https://developer.qiniu.com/fusion/api/download-the-log">日志下载</a>
     */
    public class LogListResult {
        /**
         * 自定义状态码
         */
        public int code;
        /**
         * 自定义错误码描述，字符串
         */
        public String error;
        /**
         * 日志信息
         */
        public Map<String, LogData[]> data;
    }

    public class LogData {
        /**
         * 日志文件名
         */
        public String name;
        /**
         * 日志文件大小
         */
        public int size;
        /**
         * 日志文件最后修改时间
         */
        public long mtime;
        /**
         * 日志文件下载链接
         */
        public String url;
    }
}
