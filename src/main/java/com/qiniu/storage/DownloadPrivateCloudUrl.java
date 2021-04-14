package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

public class DownloadPrivateCloudUrl extends DownloadUrl {

    private final Configuration cfg;
    private final String bucketName;
    private final String accessKey;

    public DownloadPrivateCloudUrl(String domain, boolean useHttps, String bucketName, String key, String accessKey) {
        super(domain, useHttps, key);
        this.cfg = null;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
    }

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

        String host = cfg.ioHost(accessKey, bucketName);
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
