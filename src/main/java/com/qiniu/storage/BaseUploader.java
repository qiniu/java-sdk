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
                if (!Retry.shouldSwitchRegionAndRetry(response, null)
                        || !couldReloadSource() || !reloadSource()
                        || config.region == null || !config.region.switchRegion(new UploadToken(upToken))) {
                    break;
                }
            } catch (QiniuException e) {
                if (!Retry.shouldSwitchRegionAndRetry(null, e)
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
}
