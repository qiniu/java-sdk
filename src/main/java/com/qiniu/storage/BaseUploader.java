package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;

import javax.net.ssl.SSLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public abstract class BaseUploader {

    protected final Client client;
    protected final String key;
    protected final String upToken;
    protected final Configuration config;

    BaseUploader(Client client, String upToken, String key, Configuration config) {
        this.client = client;
        this.key = key;
        this.upToken = upToken;
        this.config = config;
    }

    public Response upload() throws QiniuException {
        return uploadWithRegionRetry();
    }

    private Response uploadWithRegionRetry() throws QiniuException {
        Response response = null;
        while (true) {
            try {
                response = uploadFlows();
                if (!couldSwitchRegionAndRetry(response, null) || config.region == null || !config.region.switchRegion(new UploadToken(upToken))) {
                    break;
                }
            } catch (QiniuException e) {
                if (!couldSwitchRegionAndRetry(null, e) || config.region == null || !config.region.switchRegion(new UploadToken(upToken))) {
                    throw e;
                }
            }
        }
        return response;
    }

    abstract Response uploadFlows() throws QiniuException;

    private boolean couldSwitchRegionAndRetry(Response response, QiniuException exception) {
        Response checkResponse = response;
        if (checkResponse == null && exception != null) {
            checkResponse = exception.response;
        }

        if (checkResponse != null) {
            int statusCode = checkResponse.statusCode;
            if ((statusCode > 300 && statusCode < 400)
                    || (statusCode >= 400 && statusCode < 500 && statusCode != 400 && statusCode != 406)
                    || statusCode == 501 || statusCode == 573
                    || statusCode == 608 || statusCode == 612 || statusCode == 614 || statusCode == 616
                    || statusCode == 619 || statusCode == 630 || statusCode == 631 || statusCode == 640
                    || statusCode == 701
                    || statusCode < 100) {
                return false;
            } else {
                return true;
            }
        }

        if (exception != null && exception.getCause() != null) {
            Throwable e = exception.getCause();
            String msg = e.getMessage();
            return e instanceof UnknownHostException
                    || (msg != null && msg.indexOf("Broken pipe") == 0)
                    || e instanceof SocketTimeoutException
                    || e instanceof java.net.ConnectException
                    || e instanceof ProtocolException
                    || e instanceof SSLException;
        }

        return false;
    }
}
