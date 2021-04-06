package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Md5;
import com.qiniu.util.StringUtils;


class ResumeUploadPerformerV2 extends ResumeUploadPerformer {

    ResumeUploadPerformerV2(Client client, String key, UploadToken token, ResumeUploadSource source, Recorder recorder,
                            UploadOptions options, Configuration config) {
        super(client, key, token, source, recorder, options, config);
    }

    @Override
    boolean shouldUploadInit() {
        // uploadId 无效时需要 init,
        // 当 uploadId 不存在即无效
        // 当 uploadId 存在，则必为断点续传，断点续传进度信息恢复时会检测有效性，即存在必有效
        return StringUtils.isNullOrEmpty(uploadSource.uploadId);
    }

    @Override
    Response uploadInit() throws QiniuException {
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                return initPart(host);
            }
        });
    }

    @Override
    Response uploadBlock(final ResumeUploadSource.Block block) throws QiniuException {
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                return uploadPart(host, block);
            }
        });
    }

    @Override
    Response completeUpload() throws QiniuException {
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                return completeParts(host);
            }
        });
    }

    private Response initPart(String host) throws QiniuException {
        ApiUploadV2InitUpload api = new ApiUploadV2InitUpload(client);
        ApiUploadV2InitUpload.Request request = new ApiUploadV2InitUpload.Request(host, this.token.getToken()).setKey(key);
        ApiUploadV2InitUpload.Response response = api.request(request);

        if (response.isOK()) {
            String uploadId = response.getUploadId();
            if (uploadId == null) {
                throw new QiniuException(new Exception("uploadId is empty"));
            }

            Long expireAt = response.getExpireAt();
            if (expireAt == null) {
                throw new QiniuException(new Exception("expireAt is empty"));
            }

            uploadSource.uploadId = uploadId;
            uploadSource.expireAt = expireAt;
        }

        return response.getResponse();
    }

    private Response uploadPart(String host, ResumeUploadSource.Block block) throws QiniuException {
        ApiUploadV2UploadPart api = new ApiUploadV2UploadPart(client);
        ApiUploadV2UploadPart.Request request = new ApiUploadV2UploadPart.Request(host, this.token.getToken(),
                uploadSource.uploadId, block.index + 1)
                .setKey(key)
                .setUploadData(block.data, 0, block.size, null);
        ApiUploadV2UploadPart.Response response = api.request(request);

        if (response.isOK()) {

            if (options.checkCrc) {
                String serverMd5 = response.getMd5();
                if (serverMd5 == null) {
                    throw new QiniuException(new Exception("block's md5 is empty"));
                }

                String md5 = Md5.md5(block.data);
                if (!serverMd5.equals(md5)) {
                    throw new QiniuException(new Exception("block's md5 is not match"));
                }
            }

            String etag = response.getEtag();
            if (etag == null) {
                throw new QiniuException(new Exception("block's etag is empty"));
            }
            block.etag = etag;
            block.data = null;
        }

        return response.getResponse();
    }

    private Response completeParts(String host) throws QiniuException {
        ApiUploadV2CompleteUpload api = new ApiUploadV2CompleteUpload(client);
        ApiUploadV2CompleteUpload.Request request = new ApiUploadV2CompleteUpload.Request(host, this.token.getToken(),
                uploadSource.uploadId, uploadSource.getPartInfo())
                .setKey(key)
                .setFileMimeType(options.mimeType)
                .setFileName(uploadSource.getFileName())
                .setCustomParam(options.params.map())
                .setCustomMetaParam(options.metaDataParam.map());
        return api.request(request).getResponse();
    }
}
