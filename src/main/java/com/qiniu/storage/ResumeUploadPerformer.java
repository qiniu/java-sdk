package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

abstract class ResumeUploadPerformer {

    private final Client client;
    private final Recorder recorder;
    private final Configuration config;
    private final ConfigHelper configHelper;

    final String key;
    final UploadToken token;
    final ResumeUploadSource uploadSource;
    final UploadOptions options;

    ResumeUploadPerformer(Client client, String key, UploadToken token, UploadSource source, Recorder recorder, UploadOptions options, Configuration config) {
        this.client = client;
        this.key = key;
        this.token = token;
        this.uploadSource = new ResumeUploadSource(source, config, config.region.getRegion(token));
        this.options = options == null ? UploadOptions.defaultOptions() : options;
        this.recorder = recorder;
        this.config = config;
        this.configHelper = new ConfigHelper(config);
    }

    boolean isAllBlocksOfSourceUploaded() {
        return uploadSource.isAllBlocksUploaded();
    }

    abstract Response uploadInit() throws QiniuException;

    abstract Response uploadNextData() throws QiniuException;

    abstract Response completeUpload() throws QiniuException;

    ResumeUploadSource.Block getNextUploadingBlock() {
        return uploadSource.getNextUploadingBlock();
    }

    void recoverUploadProgressFromLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.sourceId)) {
            return;
        }

        byte[] data = recorder.get(uploadSource.sourceId);
        if (data == null) {
            return;
        }

        ResumeUploadSource source = Json.decode(new String(data, Constants.UTF_8), ResumeUploadSource.class);
        if (source == null) {
            return;
        }

        boolean isCopy = uploadSource.copyResourceUploadStateWhenValidAndSame(source);
        if (!isCopy) {
            removeUploadProgressFromLocal();
        }
    }

    void saveUploadProgressToLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.sourceId)) {
            return;
        }
        String dataString = Json.encode(uploadSource);
        recorder.set(uploadSource.sourceId, dataString.getBytes(Constants.UTF_8));
    }

    void removeUploadProgressFromLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.sourceId)) {
            return;
        }
        recorder.del(uploadSource.sourceId);
    }

    private String getUploadHost() throws QiniuException {
        return configHelper.upHost(token.getToken());
    }

    private void changeHost(String host) {
        try {
            configHelper.tryChangeUpHost(token.getToken(), host);
        } catch (Exception e) {
            // ignore
            // use the old up host //
        }
    }

    Response post(String url, byte[] data) throws QiniuException {
        StringMap header = new StringMap();
        header.put("Authorization", "UpToken " + token.getToken());
        return client.post(url, data, header);
    }

    Response put(String url, byte[] data) throws QiniuException {
        StringMap header = new StringMap();
        header.put("Authorization", "UpToken " + token.getToken());
        String contentType = "application/octet-stream";
        return client.put(url, data, header, contentType);
    }

    Response retryUploadAction(UploadAction action) throws QiniuException {
        Response response = null;
        int retryCount = 0;
        boolean shouldRetry = false;

        do {
            String host = getUploadHost();
            try {
                response = action.uploadAction(host);
            } catch (QiniuException e) {
                // 切换 Host
                if (e.code() < 0 || e.response != null && e.response.needSwitchServer()) {
                    changeHost(host);
                }

                // 判断是否需要重试
                if (!e.isUnrecoverable() || (e.response != null && e.response.needRetry())) {
                    shouldRetry = true;
                } else {
                    throw e;
                }
            }

            // 判断是否需要重试
            if (!shouldRetry && (response == null || response.needRetry())) {
                shouldRetry = true;
            }

            retryCount++;
        } while (shouldRetry && retryCount < config.retryMax);

        return response;
    }

    interface UploadAction {
        Response uploadAction(String host) throws QiniuException;
    }

}
