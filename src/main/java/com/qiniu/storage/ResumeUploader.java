package com.qiniu.storage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.File;
import java.io.InputStream;

/**
 * 同步分片上传
 * <p>
 * 分片上传 v1
 * 参考文档：<a href="https://developer.qiniu.com/kodo/7443/shard-to-upload">分片上传</a>
 * <p/>
 * 上传通过将一个文件分割为固定大小的块(4M)，每次上传一个块的内容（服务端只分块，没有分片）。
 * 等待所有块都上传完成之后，再将这些块拼接起来，构成一个完整的文件。
 * <p/>
 * <p>
 * 分片上传 v2
 * 参考文档：<a href="https://developer.qiniu.com/kodo/6364/multipartupload-interface">分片上传</a>
 * <p/>
 * 上传通过将一个文件分割为固定大小的块(大小可配置，通过 Configuration.resumableUploadAPIV2BlockSize)，每次上传一个块的内容。
 * 等待所有块都上传完成之后，再将这些块拼接起来，构成一个完整的文件。
 * <p/>
 * <p>
 * 另外分片上传还支持纪录上传进度，如果本次上传被暂停，那么下次还可以从上次
 * 上次完成的文件偏移位置，继续开始上传，这样就实现了断点续传功能。
 * <p>
 * 服务端网络较稳定，较大文件（如500M以上）才需要将块记录保存下来。
 * 小文件没有必要，可以有效地实现大文件的上传。
 */
public class ResumeUploader extends BaseUploader {
    private final ResumeUploadSource source;
    private final Recorder recorder;
    private final UploadOptions options;

    ResumeUploadPerformer uploadPerformer;

    /**
     * 构建分片上传文件的对象【兼容老版本】
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * <p>
     * 支持分片上传 v1/v2，支持断点续传
     * 不支持并发【并发使用 ConcurrentResumeUploader】
     *
     * @param client        上传 client【必须】
     * @param upToken       上传凭证【必须】
     * @param key           文件保存名称【可选】
     * @param file          文件【必须】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type【可选】
     * @param recorder      断点续传信息记录对象【可选】
     * @param configuration 上传配置信息【必须】
     */
    public ResumeUploader(Client client, String upToken, String key, File file,
                          StringMap params, String mime, Recorder recorder, Configuration configuration) {
        this(client, key, upToken,
                new ResumeUploadSourceFile(file, configuration, getRecorderKey(key, file, recorder)),
                recorder, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

    /**
     * 构建分片上传文件流的对象【兼容老版本】
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * <p>
     * 支持分片上传 v1/v2，支持并发
     * 不支持断点续传，不支持定义file name，不支持并发【并发使用 ConcurrentResumeUploader】
     *
     * @param client        上传 client 【必须】
     * @param upToken       上传凭证 【必须】
     * @param key           文件保存名称 【可选】
     * @param stream        文件流 【必须】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type【可选】
     * @param configuration 上传配置信息 【必须】
     */
    public ResumeUploader(Client client, String upToken, String key, InputStream stream,
                          StringMap params, String mime, Configuration configuration) {
        this(client, upToken, key, stream, null, params, mime, configuration);
    }

    /**
     * 构建分片上传文件流的对象
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * <p>
     * 支持分片上传 v1/v2，支持并发，支持定义file name
     * 不支持断点续传，不支持并发【并发使用 ConcurrentResumeUploader】
     *
     * @param client        上传 client 【必须】
     * @param upToken       上传凭证 【必须】
     * @param key           文件保存名称 【可选】
     * @param stream        文件流 【必须】
     * @param fileName      文件名 【可选】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type 【可选】
     * @param configuration 上传配置信息 【必须】
     */
    public ResumeUploader(Client client, String upToken, String key, InputStream stream,
                          String fileName, StringMap params, String mime, Configuration configuration) {
        this(client, key, upToken,
                new ResumeUploadSourceStream(stream, configuration, null, fileName),
                null, new UploadOptions.Builder().params(params).metaData(params).mimeType(mime).build(), configuration);
    }

    private ResumeUploader(Client client, String key, String upToken, ResumeUploadSource source, Recorder recorder,
                           UploadOptions options, Configuration configuration) {
        super(client, upToken, key, configuration);

        this.source = source;
        this.recorder = recorder;
        this.options = options == null ? UploadOptions.defaultOptions() : options;
    }

    /**
     * 上传文件
     */
    public Response upload() throws QiniuException {
        try {
            recoverUploadProgressFromLocal();
            Response response = super.upload();
            if (response != null && response.isOK()) {
                removeUploadProgressFromLocal();
            }
            return response;
        } catch (QiniuException e) {
            saveUploadProgressToLocal();
            throw e;
        } finally {
            close();
        }
    }

    @Override
    Response uploadFlows() throws QiniuException {

        // 检查参数
        checkParam();

        // 选择上传策略
        UploadToken token = new UploadToken(upToken);
        if (config.resumableUploadAPIVersion == Configuration.ResumableUploadAPIVersion.V2) {
            uploadPerformer = new ResumeUploadPerformerV2(client, key, token, source, recorder, options, config);
        } else {
            uploadPerformer = new ResumeUploadPerformerV1(client, key, token, source, recorder, options, config);
        }

        // 上传数据至服务 - 步骤1
        Response response = null;
        if (uploadPerformer.shouldUploadInit()) {
            response = uploadPerformer.uploadInit();
            if (!response.isOK()) {
                return response;
            }
        }

        // 上传数据至服务 - 步骤2
        if (!uploadPerformer.isAllBlocksUploaded()) {
            response = uploadData();
            if (!response.isOK()) {
                return response;
            }
        }

        // 上传数据至服务 - 步骤3
        response = uploadPerformer.completeUpload();

        return response;
    }

    Response uploadData() throws QiniuException {
        Response response = null;
        do {
            response = uploadPerformer.uploadNextData();
        } while (!uploadPerformer.isAllBlocksUploadingOrUploaded());
        return response;
    }

    @Override
    boolean couldReloadSource() {
        return source.couldReload();
    }

    @Override
    boolean reloadSource() {
        if (source.reload()) {
            source.clearState();
            return true;
        } else {
            return false;
        }
    }

    private void close() {
        try {
            source.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkParam() throws QiniuException {
        if (client == null) {
            throw QiniuException.unrecoverable(new Exception("client can't be empty"));
        }

        if (config == null) {
            throw QiniuException.unrecoverable(new Exception("Configuration can't be empty"));
        }

        if (config.zone == null && config.region == null) {
            throw QiniuException.unrecoverable(new Exception("Configuration.region can't be empty"));
        }

        if (!source.isValid()) {
            throw QiniuException.unrecoverable(new Exception("InputStream or File is invalid"));
        }

        UploadToken token = new UploadToken(upToken);
        if (!token.isValid()) {
            throw QiniuException.unrecoverable(new Exception("token is invalid"));
        }
    }

    private static String getRecorderKey(String key, File file, Recorder recorder) {
        if (recorder == null) {
            return null;
        }
        return recorder.recorderKeyGenerate(key, file);
    }

    // recorder
    void recoverUploadProgressFromLocal() {
        if (recorder == null || source == null || StringUtils.isNullOrEmpty(source.recordKey)) {
            return;
        }

        byte[] data = recorder.get(source.recordKey);
        if (data == null) {
            return;
        }

        String jsonString = new String(data, Constants.UTF_8);
        Region region = null;
        ResumeUploadSource uploadSource = null;
        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonString);
            JsonObject sourceJson = jsonObject.getAsJsonObject("source");
            uploadSource = new Gson().fromJson(sourceJson, source.getClass());

            JsonObject regionJson = jsonObject.getAsJsonObject("region");
            region = new Gson().fromJson(regionJson, Region.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uploadSource == null || region == null) {
            removeUploadProgressFromLocal();
            return;
        }

        boolean isCopy = source.recoverFromRecordInfo(uploadSource);
        if (!isCopy) {
            removeUploadProgressFromLocal();
            return;
        }

        if (config.region == null) {
            config.region = region;
        } else {
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.addRegion(region);
            regionGroup.addRegion(config.region);
            config.region = regionGroup;
        }
    }

    void saveUploadProgressToLocal() {
        if (recorder == null || source == null || !source.hasUploadData() || StringUtils.isNullOrEmpty(source.recordKey)) {
            return;
        }
        try {
            JsonObject jsonObject = new JsonObject();
            JsonElement sourceJson = new Gson().toJsonTree(source);
            if (sourceJson == null) {
                return;
            }
            jsonObject.add("source", sourceJson);
            JsonElement regionJson = new Gson().toJsonTree(config.region.getCurrentRegion(new UploadToken(upToken)));
            if (regionJson == null) {
                return;
            }
            jsonObject.add("region", regionJson);
            String dataString = jsonObject.toString();
            recorder.set(source.recordKey, dataString.getBytes(Constants.UTF_8));
        } catch (Exception ignored) {
        }
    }

    void removeUploadProgressFromLocal() {
        if (recorder == null || source == null || StringUtils.isNullOrEmpty(source.recordKey)) {
            return;
        }
        recorder.del(source.recordKey);
    }
}
