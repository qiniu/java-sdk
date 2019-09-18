package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

/**
 * 跨域规则<br>
 * 最多允许设置10条跨域规则。<br>
 * 对于同一个域名如果设置了多条规则，那么按顺序使用第一条匹配的规则去生成返回值。<br>
 * 对于简单跨域请求，只匹配 Origin；<br>
 * 对于预检请求， 需要匹配 Origin、AllowedMethod、AllowedHeader；<br>
 * 备注：如果没有设置任何corsRules，那么默认允许所有的跨域请求<br>
 * 参考：https://www.w3.org/TR/cors/
 */
public class CorsRule {

    /**
     * allowed_orgin: 允许的域名。必填；支持通配符*；*表示全部匹配；只有第一个*生效；需要设置"Scheme"；大小写敏感。<br>
     * 例如<br>
     * 规则：http://*.abc.*.com 请求："http://test.abc.test.com" 结果：不通过<br>
     * 规则："http://abc.com" 请求："https://abc.com" 结果：不通过<br>
     * 规则："abc.com" 请求："http://abc.com" 结果：不通过<br>
     */
    @SerializedName("allowed_origin")
    String[] allowedOrigin = new String[]{};

    /**
     * allowed_method: 允许的方法。必填；不支持通配符；大小写不敏感；
     */
    @SerializedName("allowed_method")
    String[] allowedMethod = new String[]{};

    /**
     * allowed_header: 允许的header。选填；支持通配符*，但只能是单独的*，表示允许全部header，其他*不生效；空则不允许任何header；大小写不敏感；
     */
    @SerializedName("allowed_header")
    String[] allowedHeader = new String[]{};

    /**
     * exposed_header: 暴露的header。选填；不支持通配符；X-Log, X-Reqid是默认会暴露的两个header；其他的header如果没有设置，则不会暴露；大小写不敏感；
     */
    @SerializedName("exposed_header")
    String[] exposedHeader = new String[]{};

    /**
     * max_age: 结果可以缓存的时间。选填；空则不缓存；
     */
    @SerializedName("max_age")
    long maxAge = 0;

    public CorsRule(String[] allowedOrigin, String[] allowedMethod) {
        this.allowedOrigin = allowedOrigin;
        this.allowedMethod = allowedMethod;
    }

    public CorsRule(String[] allowedOrigin, String[] allowedMethod, String[] allowedHeader,
                    String[] exposedHeader, long maxAge) {
        this.allowedOrigin = allowedOrigin;
        this.allowedMethod = allowedMethod;
        this.allowedHeader = allowedHeader;
        this.exposedHeader = exposedHeader;
        this.maxAge = maxAge;
    }

    public String[] getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String[] allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public String[] getAllowedMethod() {
        return allowedMethod;
    }

    public void setAllowedMethod(String[] allowedMethod) {
        this.allowedMethod = allowedMethod;
    }

    public String[] getAllowedHeader() {
        return allowedHeader;
    }

    public void setAllowedHeader(String[] allowedHeader) {
        this.allowedHeader = allowedHeader;
    }

    public String[] getExposedHeader() {
        return exposedHeader;
    }

    public void setExposedHeader(String[] exposedHeader) {
        this.exposedHeader = exposedHeader;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

}
