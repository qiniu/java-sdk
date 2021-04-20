package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

/**
 * 私有云下载 URL 类
 */
public class DownloadPrivateCloudUrl extends DownloadUrl {

    private final Configuration cfg;
    private final String bucketName;
    private final String accessKey;

    /**
     * 构造器
     * 如果知道下载的 domain 信息可以使用此接口
     * 如果不知道 domain 信息，可以使用 {@link DownloadPrivateCloudUrl#DownloadPrivateCloudUrl(Configuration, String, String, String)}
     *
     * @param domain     下载 domain, eg: qiniu.com 【必须】
     * @param useHttps   是否使用 https 【必须】
     * @param bucketName bucket 名称 【必须】
     * @param key        下载资源在七牛云存储的 key 【必须】
     * @param accessKey  七牛账户 accessKey 【必须】
     */
    public DownloadPrivateCloudUrl(String domain, boolean useHttps, String bucketName, String key, String accessKey) {
        super(domain, useHttps, key);
        this.cfg = null;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
    }

    /**
     * 构造器
     * 如果不知道 domain 信息，可使用此接口；内部有查询 domain 逻辑
     * 查询 domain 流程：
     * 1. 根据 {@link Configuration#defaultUcHost} 查找 bucketName 所在的{@link Configuration#region}
     * 2. 获取 {@link Configuration#region} 中的 ioHost({@link Configuration#ioHost(String, String)} ) 作为 domain
     * 注：需要配置正确的 {@link Configuration#defaultUcHost}
     *
     * @param cfg        查询 domain 时的Configuration 【必须】
     * @param bucketName bucket 名称【必须】
     * @param key        下载资源在七牛云存储的 key【必须】
     * @param accessKey  七牛账户 accessKey【必须】
     */
    public DownloadPrivateCloudUrl(Configuration cfg, String bucketName, String key, String accessKey) {
        super(null, cfg.useHttpsDomains, key);
        this.cfg = cfg;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
    }

    @Override
    protected void willBuildUrl() throws QiniuException {
        super.willBuildUrl();
        if (StringUtils.isNullOrEmpty(getDomain())) {
            setDomain(queryDomain());
        }
    }

    @Override
    protected void willSetKeyForUrl(Api.Request request) throws QiniuException {
        request.addPathSegment("getfile");
        request.addPathSegment(accessKey);
        request.addPathSegment(bucketName);
        super.willSetKeyForUrl(request);
    }

    private String queryDomain() throws QiniuException {
        if (cfg == null) {
            ApiUtils.throwInvalidRequestParamException("configuration");
        }
        if (accessKey == null) {
            ApiUtils.throwInvalidRequestParamException("accessKey");
        }
        if (bucketName == null) {
            ApiUtils.throwInvalidRequestParamException("bucketName");
        }

        ConfigHelper configHelper = new ConfigHelper(cfg);
        String host = configHelper.ioHost(accessKey, bucketName);
        if (StringUtils.isNullOrEmpty(host)) {
            return host;
        }
        if (host.contains("http://")) {
            return host.replaceFirst("http://", "");
        }
        if (host.contains("https://")) {
            return host.replaceFirst("https://", "");
        }
        return host;
    }

}
