package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.IOException;

abstract class ResumeUploadPerformer {

    private final Client client;
    private final Recorder recorder;
    private final Configuration config;
    private final ConfigHelper configHelper;

    final String key;
    final UploadToken token;
    final ResumeUploadSource uploadSource;
    final UploadOptions options;

    ResumeUploadPerformer(Client client, String key, UploadToken token, ResumeUploadSource source,
                          Recorder recorder, UploadOptions options, Configuration config) {
        this.client = client;
        this.key = key;
        this.token = token;
        this.uploadSource = source;
        this.options = options == null ? UploadOptions.defaultOptions() : options;
        this.recorder = recorder;
        this.config = config;
        this.configHelper = new ConfigHelper(config);
    }

    boolean isAllBlocksUploadingOrUploaded() {
        return uploadSource.isAllBlocksUploadingOrUploaded();
    }

    boolean isAllBlocksUploaded() {
        return uploadSource.isAllBlocksUploaded();
    }


    abstract boolean shouldUploadInit();

    abstract Response uploadInit() throws QiniuException;

    Response uploadNextData() throws QiniuException {
        ResumeUploadSource.Block block = null;
        synchronized (this) {
            block = getNextUploadingBlock();
            if (block != null) {
                block.isUploading = true;
            }
        }

        if (block == null) {
            return Response.createSuccessResponse();
        }

        try {
            return uploadBlock(block);
        } finally {
            block.isUploading = false;
        }
    }

    abstract Response uploadBlock(ResumeUploadSource.Block block) throws QiniuException;

    abstract Response completeUpload() throws QiniuException;

    private ResumeUploadSource.Block getNextUploadingBlock() throws QiniuException {

        ResumeUploadSource.Block block = null;
        try {
            block = uploadSource.getNextUploadingBlock();
        } catch (IOException e) {
            throw new QiniuException(e);
        }
        return block;
    }

    void recoverUploadProgressFromLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.recordKey)) {
            return;
        }

        byte[] data = recorder.get(uploadSource.recordKey);
        if (data == null) {
            return;
        }
        String jsonString = new String(data, Constants.UTF_8);
        ResumeUploadSource source = null;
        try {
            source = new Gson().fromJson(jsonString, uploadSource.getClass());
        } catch (Exception ignored) {
        }
        if (source == null) {
            return;
        }

        boolean isCopy = uploadSource.recoverFromRecordInfo(source);
        if (!isCopy) {
            removeUploadProgressFromLocal();
        }
    }

    void saveUploadProgressToLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.recordKey)) {
            return;
        }
        String dataString = new Gson().toJson(uploadSource);
        recorder.set(uploadSource.recordKey, dataString.getBytes(Constants.UTF_8));
    }

    void removeUploadProgressFromLocal() {
        if (recorder == null || StringUtils.isNullOrEmpty(uploadSource.recordKey)) {
            return;
        }
        recorder.del(uploadSource.recordKey);
    }

    private String getUploadHost() throws QiniuException {
        return configHelper.upHost(token.getToken());
    }

    private void changeHost(String host) {
        try {
            configHelper.tryChangeUpHost(token.getToken(), host);
        } catch (Exception ignored) {
        }
    }

    Response post(String url, byte[] data, int offset, int size) throws QiniuException {
        StringMap header = new StringMap();
        header.put("Authorization", "UpToken " + token.getToken());
        return client.post(url, data, offset, size, header, options.mimeType);
    }

    Response put(String url, byte[] data, int offset, int size) throws QiniuException {
        StringMap header = new StringMap();
        header.put("Authorization", "UpToken " + token.getToken());
        return client.put(url, data, offset, size, header, options.mimeType);
    }

    Response retryUploadAction(UploadAction action) throws QiniuException {
        Response response = null;
        int retryCount = 0;


        do {
            boolean shouldRetry = false;
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

            if (!shouldRetry) {
                break;
            }

            if (retryCount >= config.retryMax) {
                throw QiniuException.unrecoverable("failed after retry times");
            }
        } while (true);

        return response;
    }

    interface UploadAction {
        Response uploadAction(String host) throws QiniuException;
    }

}
