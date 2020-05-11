package com.qiniu.storage.model;

/**
 * 空间referer配置
 */
public class BucketReferAntiLeech {

    /**
     * 是否开启源站的防盗链，默认为开启，且不可变<br>
     * <br>
     * bucket 上的防盗链设置默认是不生效的，在以下两种情况下生效<br>
     * 1. enableSource 开启<br>
     * 2. 特定 cdn 约定：请求的 header 中携带了 X-Rbl 字段，会将防盗链设置以 header 的方式返回给 cdn（在sdk中不支持此方式）
     */
    final boolean enableSource = true;
    /**
     * 防盗链模式， 0 - 关闭Refer防盗链, 1 - 开启Referer白名单，2 - 开启Referer黑名单
     */
    int mode = 0;
    /**
     * 是否允许空的referer访问
     */
    boolean allowEmptyReferer = true;
    /**
     * Pattern 匹配HTTP Referer头, 当模式是1或者2的时候有效<br>
     * Mode为1的时候表示允许Referer符合该Pattern的HTTP请求访问<br>
     * Mode为2的时候表示禁止Referer符合该Pattern的HTTP请求访问<br>
     * 当前允许的匹配字符串格式分为三种:<br>
     * 一种为空主机头域名, 比如 foo.com; 一种是泛域名, 比如 *.bar.com;<br>
     * 一种是完全通配符, 即一个 *;<br>
     * 多个规则之间用;隔开, 比如: foo.com;*.bar.com;sub.foo.com;*.sub.bar.com
     */
    String pattern;

    /**
     * 设置防盗链模式,
     * 0 - 关闭Refer防盗链, 1 - 开启Referer白名单，2 - 开启Referer黑名单
     *
     * @param mode
     * @throws Exception
     */
    public BucketReferAntiLeech setMode(int mode) throws Exception {
        if (mode != 0 && mode != 1 && mode != 2) {
            throw new Exception("Referer anti_leech_mode must be in [0, 1, 2]");
        }
        this.mode = mode;
        return this;
    }

    /**
     * 设置是否允许空referer，默认为true
     *
     * @param allowEmptyReferer
     */
    public BucketReferAntiLeech setAllowEmptyReferer(boolean allowEmptyReferer) {
        this.allowEmptyReferer = allowEmptyReferer;
        return this;
    }

    /**
     * 设置pattern<br>
     * pattern不可为空<br>
     * 当前允许的匹配字符串格式分为三种: foo.com、*.foo.com、*，用;隔开
     *
     * @param pattern
     */
    public BucketReferAntiLeech setPattern(String pattern) throws Exception {
        if (pattern == null || pattern.isEmpty()) {
            throw new Exception("Empty pattern is not allowed");
        }
        this.pattern = pattern;
        return this;
    }

    /**
     * 追加pattern<br>
     * pattern
     *
     * @param pattern
     * @return
     * @throws Exception
     */
    public BucketReferAntiLeech addPattern(String pattern) throws Exception {
        if (pattern == null || pattern.isEmpty()) {
            throw new Exception("Empty pattern is not allowed");
        }
        if (this.pattern.endsWith(";")) {
            this.pattern = this.pattern.substring(0, this.pattern.length() - 1);
        }
        this.pattern = this.pattern + ";" + pattern;
        return this;
    }

    /**
     * 编码成query参数格式
     *
     * @return
     */
    public String asQueryString() {
        final int allowEmptyReferer = this.allowEmptyReferer ? 1 : 0;
        final int enableSource = this.enableSource ? 1 : 0;
        return String.format("mode=%d&norefer=%d&pattern=%s&source_enabled=%d",
                this.mode,
                allowEmptyReferer,
                null == this.pattern ? "" : this.pattern,
                enableSource
        );
    }
}
