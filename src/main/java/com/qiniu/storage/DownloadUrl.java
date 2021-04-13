package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;

import java.net.URL;

public class DownloadUrl {

    private boolean useHttps = false;

    private String domain;

    private String key;

    private Long deadline;

    private String token;

    private String attname;

    private String fop;

    public URL build() throws QiniuException {
        Api.Request request = new Api.Request(getUrlPrefix());
        request.addPathSegment(key);

        if (StringUtils.isNullOrEmpty(attname)) {
            request.addQueryPair("attname", attname);
        }

        if (deadline != null) {
            request.addQueryPair("e", deadline.toString());
        }

        if (StringUtils.isNullOrEmpty(token)) {
            request.addQueryPair("token", token);
        }

        return request.getUrl();
    }

    private String getUrlPrefix() throws QiniuException {
        if (useHttps) {
            return "https://" + domain;
        } else {
            return "http://" + domain;
        }
    }
}
