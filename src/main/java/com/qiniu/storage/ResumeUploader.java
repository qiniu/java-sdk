package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ResumeUploader {
    final Client client;
    final String key;
    final String upToken;
    final UploadSource source;
    final Recorder recorder;
    final UploadOptions options;
    final Configuration config;

    private ResumeUploadPerformer uploadPerformer;

    /**
     * 构建分片上传文件的对象
     */
    public ResumeUploader(Client client, String upToken, String key, File file,
                          StringMap params, String mime, Recorder recorder, Configuration configuration) {

        this(client, key, upToken,
                new UploadSource(getRecorderKey(key, file.getName(), recorder), file),
                recorder, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

    public ResumeUploader(Client client, String upToken, String key, InputStream inputStream, String fileName, long fileSize,
                          StringMap params, String mime, Recorder recorder, Configuration configuration) {

        this(client, key, upToken,
                new UploadSource(getRecorderKey(key, fileName, recorder), fileName, fileSize, inputStream),
                recorder, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

    private ResumeUploader(Client client, String key, String upToken, UploadSource source, Recorder recorder, UploadOptions options, Configuration configuration) {

        this.client = client;
        this.key = key;
        this.upToken = upToken;
        this.source = source;
        this.recorder = recorder;
        this.options = options;
        this.config = configuration;
    }

    /**
     * 上传文件
     */
    public Response upload() throws QiniuException {
        try {
            return _upload();
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response _upload() throws QiniuException {
        // 检查参数
        if (!source.isValid()) {
            throw QiniuException.unrecoverable(new Exception("InputStream or File is invalid"));
        }

        UploadToken token = new UploadToken(upToken);
        if (!token.isValid()) {
            throw QiniuException.unrecoverable(new Exception("token is invalid"));
        }

        // 选择上传策略
        if (config.resumeVersion == Configuration.ResumeVersion.V2) {
            uploadPerformer = new ResumeUploadPerformerV2(client, key, token, source, recorder, options, config);
        } else {
            uploadPerformer = new ResumeUploadPerformerV1(client, key, token, source, recorder, options, config);
        }

        // 恢复本地断点续传数据
        uploadPerformer.recoverUploadProgressFromLocal();

        // 上传数据至服务 - 步骤1
        Response response = uploadPerformer.uploadInit();
        if (!response.isOK()) {
            uploadPerformer.saveUploadProgressToLocal();
            return response;
        }

        // 上传数据至服务 - 步骤2
        response = uploadData();
        if (!response.isOK()) {
            uploadPerformer.saveUploadProgressToLocal();
            return response;
        }

        // 上传数据至服务 - 步骤3
        response = uploadPerformer.completeUpload();
        if (response.isOK()) {
            uploadPerformer.removeUploadProgressFromLocal();
        } else {
            uploadPerformer.saveUploadProgressToLocal();
        }

        return response;
    }

    private Response uploadData() throws QiniuException {
        Response response = null;
        do {
            response = uploadPerformer.uploadNextData();
        } while (response.isOK() && !uploadPerformer.isAllBlocksOfSourceUploaded());
        return response;
    }

    private static String getRecorderKey(String key, String fileName, Recorder recorder) {
        if (recorder == null) {
            return null;
        }
        return recorder.recorderKeyGenerate(key, fileName);
    }
}
