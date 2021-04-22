package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 公有云下载 URL 类
 */
public class DownloadUrl {

    private String domain;
    private boolean useHttps = false;
    private String key;
    private Auth auth;
    private Long deadline;
    private String style;
    private String styleSeparator;
    private String styleParam;
    private String fop;
    private String attname;
    private List<Api.Request.Pair<String, String>> customQuerys = new ArrayList<>();

    /**
     * 构造器
     *
     * @param domain   下载 domain, eg: qiniu.com【必须】
     * @param useHttps 是否使用 https【必须】
     * @param key      下载资源在七牛云存储的 key【必须】
     */
    public DownloadUrl(String domain, boolean useHttps, String key) {
        this.domain = domain;
        this.useHttps = useHttps;
        this.key = key;
    }

    /**
     * 设置下载 domain
     *
     * @param domain 下载 domain
     */
    protected void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * 获取下载 domain
     *
     * @return 下载 domain
     */
    protected String getDomain() {
        return domain;
    }

    /**
     * 浏览器访问时指定下载文件名【可选】
     * 默认情况下，如果在浏览器中访问一个资源URL，浏览器都会试图直接在浏览器中打开这个资源，例如一张图片。
     * 如果希望浏览器的动作是下载而不是打开，可以给该资源URL添加参数 attname 来指定文件名
     *
     * @param attname 文件名
     * @return
     */
    public DownloadUrl setAttname(String attname) {
        this.attname = attname;
        return this;
    }

    /**
     * 配置 fop【可选】
     * 开发者可以在访问资源时制定执行一个或多个数据处理指令，以直接获取经过处理后的结果。比较典型的一个场景是图片查看，客户端可以上传一
     * 张高精度的图片，然后在查看图片的时候根据屏幕规格生成一张大小适宜的缩略图。这样既可以明显降低网络流量，而且可以提高图片显示速度，
     * 还能降低移动设备的内存占用.
     * eg:
     * 针对该原图生成一张480x320大小的缩略图:imageView2/2/w/320/h/480
     * https://developer.qiniu.com/dora/6217/directions-for-use-pfop
     *
     * @param fop fop
     * @return DownloadUrl
     */
    public DownloadUrl setFop(String fop) {
        this.fop = fop;
        return this;
    }

    /**
     * 配置 style【可选】
     * 如果觉得 fop 这样的形式够冗长，还可以为这些串行的 fop 集合定义一个友好别名。如此一来，就可以用友好URL风格进行访问，这个别名就是 style 。
     * eg:
     * > 对 userBucket 的 fop（imageView2/2/w/320/h/480） 使用 style 的方式, 分隔符为 "-"
     * >> 使用 qrsctl 命令定义 style: （qrsctl separator <bucket> <styleSeparator>）
     * >> qrsctl separator userBucket -
     * >> 定义数据处理的别名为 aliasName: (qrsctl style <bucket> <aliasName> <fop>)
     * >> qrsctl style userBucket iphone imageView2/2/w/320/h/480
     * <p>
     * https://developer.qiniu.com/dora/6217/directions-for-use-pfop
     *
     * @param style          style 名【必须】
     * @param styleSeparator url 和数据处理之间的分隔符【必须】
     * @param styleParam     style 参数【可选】
     * @return DownloadUrl
     */
    public DownloadUrl setStyle(String style, String styleSeparator, String styleParam) {
        this.style = style;
        this.styleSeparator = styleSeparator;
        this.styleParam = styleParam;
        return this;
    }

    /**
     * URL 增加 query 信息 【可选】
     * query 信息必须为七牛云支持的，否则会被视为无效
     *
     * @param queryName  query 名
     * @param queryValue query 值
     * @return DownloadUrl
     */
    public DownloadUrl addCustomQuery(String queryName, String queryValue) {
        customQuerys.add(new Api.Request.Pair<String, String>(queryName, queryValue));
        return this;
    }

    /**
     * 构建带有有效期的下载 URL 字符串
     * 一般构建私有资源的下载 URL 字符串；公开资源可以直接使用 {@link DownloadUrl#buildURL }
     *
     * @param auth     凭证信息【必须】
     * @param deadline 有效期时间戳，单位：秒 【必须】
     * @return 下载 URL 字符串
     * @throws QiniuException 构建异常，一般为参数缺失
     */
    public String buildURL(Auth auth, long deadline) throws QiniuException {
        this.auth = auth;
        this.deadline = deadline;
        return buildURL();
    }

    /**
     * 构建资源下载 URL 字符串
     *
     * @return 下载 URL 字符串
     * @throws QiniuException 构建异常，一般为参数缺失
     */
    public String buildURL() throws QiniuException {
        willBuildUrl();

        Api.Request request = new Api.Request(getUrlPrefix());

        willSetKeyForUrl(request);
        String keyAndStyle = null;
        keyAndStyle = urlPathEncode(key);
        if (!StringUtils.isNullOrEmpty(style) && !StringUtils.isNullOrEmpty(styleSeparator)) {
            keyAndStyle += urlPathEncode(styleSeparator + style);
            if (!StringUtils.isNullOrEmpty(styleParam)) {
                keyAndStyle += "@" + urlPathEncode(styleParam);
            }
        }
        if (!StringUtils.isNullOrEmpty(keyAndStyle)) {
            request.addPathSegment(keyAndStyle);
        }
        didSetKeyForUrl(request);

        if (!StringUtils.isNullOrEmpty(fop)) {
            request.addQueryPair(fop, null);
        }

        for (Api.Request.Pair<String, String> pair : customQuerys) {
            request.addQueryPair(pair.getKey(), pair.getValue());
        }

        if (!StringUtils.isNullOrEmpty(attname)) {
            request.addQueryPair("attname", attname);
        }

        didBuildUrl();

        String url = request.getUrl().toString();
        if (auth != null && deadline != null) {
            url = auth.privateDownloadUrlWithDeadline(url, deadline);
        }
        return url;
    }

    protected void willBuildUrl() throws QiniuException {
        // key 可以为 ""
        if (key == null) {
            ApiUtils.throwInvalidRequestParamException("key");
        }
    }

    protected void willSetKeyForUrl(Api.Request request) throws QiniuException {
        if (StringUtils.isNullOrEmpty(domain)) {
            ApiUtils.throwInvalidRequestParamException("domain");
        }
    }

    protected void didSetKeyForUrl(Api.Request request) throws QiniuException {
    }

    protected void didBuildUrl() throws QiniuException {
    }

    private String getUrlPrefix() throws QiniuException {
        if (useHttps) {
            return "https://" + domain;
        } else {
            return "http://" + domain;
        }
    }

    /**
     * 七牛 url path 特殊处理
     *
     * @param path raw url path
     * @return encode url path
     */
    private String urlPathEncode(String path) {
        return UrlUtils.urlEncode(path, "/~");
    }
}
