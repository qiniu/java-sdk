package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;

public abstract class BaseUploader {

    protected final Client client;
    protected final String key;
    protected final String upToken;
    protected final ConfigHelper configHelper;
    protected final Configuration config;

    BaseUploader(Client client, String upToken, String key, Configuration config) {
        this.client = client;
        this.key = key;
        this.upToken = upToken;
        if (config == null) {
            this.config = new Configuration();
        } else {
            this.config = config.clone();
        }
        this.configHelper = new ConfigHelper(this.config);
    }

    public Response upload() throws QiniuException {
        if (this.config == null) {
            throw QiniuException.unrecoverable("config can't be empty");
        }
        return uploadWithRegionRetry();
    }

    private Response uploadWithRegionRetry() throws QiniuException {
        Response response = null;
        while (true) {
            try {
                response = uploadFlows();
                if (!couldSwitchRegionAndRetry(response, null)
                        || !couldReloadSource() || !reloadSource()
                        || config.region == null || !config.region.switchRegion(new UploadToken(upToken))) {
                    break;
                }
            } catch (QiniuException e) {
                if (!couldSwitchRegionAndRetry(null, e)
                        || !couldReloadSource() || !reloadSource()
                        || config.region == null || !config.region.switchRegion(new UploadToken(upToken))) {
                    throw e;
                }
            }
        }
        return response;
    }

    abstract Response uploadFlows() throws QiniuException;

    abstract boolean couldReloadSource();

    abstract boolean reloadSource();

    private boolean couldSwitchRegionAndRetry(Response response, QiniuException exception) {
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
}
