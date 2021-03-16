package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.InputStream;

public class ResumeUploader {
    final Client client;
    final String key;
    final String upToken;
    final ResumeUploadSource source;
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
                new ResumeUploadSourceFile(file, configuration, getRecorderKey(key, file, recorder), getRegionTargetId(upToken, configuration)),
                recorder, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

    public ResumeUploader(Client client, String upToken, String key, InputStream stream,
                          StringMap params, String mime, Configuration configuration) {
        this(client, key, upToken,
                new ResumeUploadSourceStream(stream, configuration, null, getRegionTargetId(upToken, configuration)),
                null, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

//    public ResumeUploader(Client client, String upToken, String key, InputStream inputStream,
//                          String fileName, long fileSize, StringMap params, String mime, Recorder recorder, Configuration configuration) {
//        this(client, key, upToken,
//                new ResumeUploadSourceStream(inputStream, configuration, null, getRegionTargetId(upToken, configuration)),
//                recorder, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
//    }

    private ResumeUploader(Client client, String key, String upToken, ResumeUploadSource source, Recorder recorder, UploadOptions options, Configuration configuration) {

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
            close();
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
        System.out.println("上传步骤 1: 开始");
        Response response = null;
        if (uploadPerformer.shouldInit()) {
            response = uploadPerformer.uploadInit();
            if (!response.isOK()) {
                return response;
            }
        }
        System.out.printf("上传步骤 1: 结束 %s \n", response);

        // 上传数据至服务 - 步骤2
        System.out.println("上传步骤 2: 开始");
        if (!uploadPerformer.isAllBlocksOfSourceUploaded()) {
            response = uploadData();
            if (!response.isOK()) {
                return response;
            }
        }
        System.out.printf("上传步骤 2: 结束 %s \n", response);

        // 上传数据至服务 - 步骤3
        System.out.println("上传步骤 3: 开始");
        response = uploadPerformer.completeUpload();
        if (response.isOK()) {
            uploadPerformer.removeUploadProgressFromLocal();
        }
        System.out.printf("上传步骤 3: 结束 %s \n", response);

        return response;
    }

    private Response uploadData() throws QiniuException {
        Response response = null;
        do {
            response = uploadPerformer.uploadNextData();
            if (response != null && response.isOK()) {
                uploadPerformer.saveUploadProgressToLocal();
            }
            System.out.println("上传块结束");
        } while (!uploadPerformer.isAllBlocksOfSourceUploaded());
        return response;
    }

    private void close() {
        try {
            source.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRecorderKey(String key, File file, Recorder recorder) {
        if (recorder == null) {
            return null;
        }
        return recorder.recorderKeyGenerate(key, file);
    }

    private static String getRegionTargetId(String upToken, Configuration config) {
        if (config == null || upToken == null) {
            return null;
        }

        UploadToken token = null;
        try {
            token = new UploadToken(upToken);
        } catch (QiniuException ignored) {
        }

        if (token == null || !token.isValid()) {
            return null;
        }
        return config.region.getRegion(token);
    }
}
