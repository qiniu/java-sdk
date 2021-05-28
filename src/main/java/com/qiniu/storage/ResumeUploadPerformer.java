package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;

import java.io.IOException;

abstract class ResumeUploadPerformer {

    final Client client;
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
            throw QiniuException.unrecoverable(e);
        }
        return block;
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

    Response retryUploadAction(UploadAction action) throws QiniuException {
        Response response = null;
        int retryCount = 0;

        do {
            boolean shouldSwitchHost = false;
            boolean shouldRetry = false;
            String host = getUploadHost();
            try {
                response = action.uploadAction(host);
                // 判断是否需要重试
                if (response == null || response.needRetry()) {
                    shouldRetry = true;
                }
                // 判断是否需要切换 host
                if (response == null || response.needSwitchServer()) {
                    shouldSwitchHost = true;
                }
            } catch (QiniuException e) {

                // 判断是否需要重试
                if (!e.isUnrecoverable() && (e.response == null || e.response.needRetry())) {
                    shouldRetry = true;
                } else {
                    throw e;
                }

                if (!e.isUnrecoverable() && (e.response == null || e.response.needSwitchServer())) {
                    shouldSwitchHost = true;
                }
            }

            if (!shouldRetry) {
                break;
            }

            retryCount++;
            if (retryCount >= config.retryMax) {
                throw new QiniuException(null, "failed after retry times");
            }

            if (shouldSwitchHost) {
                changeHost(host);
            }

        } while (true);

        return response;
    }

    interface UploadAction {
        Response uploadAction(String host) throws QiniuException;
    }

}
