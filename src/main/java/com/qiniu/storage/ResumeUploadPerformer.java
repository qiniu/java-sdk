package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;

import java.io.IOException;
import java.util.List;

abstract class ResumeUploadPerformer {

    final Client client;
    private final Recorder recorder;
    private final Configuration config;
    final ConfigHelper configHelper;

    final String key;
    final UploadToken token;
    final ResumeUploadSource uploadSource;
    final UploadOptions options;
    private Api.Config uploadApiConfig;

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
        List<String> ipHosts = this.configHelper.upHostsWithoutScheme();
        this.uploadApiConfig = new Api.Config.Builder()
                .setSingleHostRetryMax(config.retryMax)
                .setHostRetryMax(ipHosts.size())
                .setRetryInterval(Retry.staticInterval(config.retryInterval))
                .setHostFreezeDuration(config.hostFreezeDuration)
                .setHostProvider(HostProvider.arrayProvider(ipHosts.toArray(new String[0])))
                .build();
    }

    protected Api.Config getUploadApiConfig() {
        return uploadApiConfig;
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
}
