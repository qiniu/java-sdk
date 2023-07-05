package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Crc32;

class ResumeUploadPerformerV1 extends ResumeUploadPerformer {


    ResumeUploadPerformerV1(Client client, String key, UploadToken token, ResumeUploadSource source, Recorder recorder,
                            UploadOptions options, Configuration config) {
        super(client, key, token, source, recorder, options, config);
    }

    @Override
    boolean shouldUploadInit() {
        return false;
    }

    @Override
    Response uploadInit() throws QiniuException {
        return null;
    }

    @Override
    Response uploadBlock(final ResumeUploadSource.Block block) throws QiniuException {
        String urlPrefix = configHelper.upHost(token.getToken());
        ApiUploadV1MakeBlock api = new ApiUploadV1MakeBlock(client, getUploadApiConfig());
        ApiUploadV1MakeBlock.Request request = new ApiUploadV1MakeBlock.Request(urlPrefix, token.getToken(), block.size)
                .setFirstChunkData(block.data, 0, block.size, null);
        ApiUploadV1MakeBlock.Response response = api.request(request);

        if (response.isOK()) {
            if (options.checkCrc) {
                Long serverCrc = response.getCrc32();
                if (serverCrc == null) {
                    throw new QiniuException(new Exception("block's crc32 is empty"));
                }

                long crc = Crc32.bytes(block.data, 0, block.size);
                if ((long) serverCrc != crc) {
                    throw new QiniuException(new Exception("block's crc32 is not match"));
                }
            }

            String ctx = response.getCtx();
            if (ctx == null) {
                throw new QiniuException(new Exception("block's ctx is empty"));
            }
            block.context = ctx;

            Long expiredAt = response.getExpiredAt();
            if (expiredAt == null) {
                throw new QiniuException(new Exception("block's expiredAt is empty"));
            }
            block.expiredAt = expiredAt;

            block.data = null;
        }

        return response.getResponse();
    }

    @Override
    Response completeUpload() throws QiniuException {
        String urlPrefix = configHelper.upHost(token.getToken());
        String[] contexts = uploadSource.getAllBlockContextList();
        ApiUploadV1MakeFile api = new ApiUploadV1MakeFile(client, getUploadApiConfig());
        final ApiUploadV1MakeFile.Request request = new ApiUploadV1MakeFile.Request(urlPrefix, token.getToken(), uploadSource.getSize(), contexts)
                .setKey(key)
                .setFileMimeType(options.mimeType)
                .setFileName(uploadSource.getFileName())
                .setCustomParam(options.params.map())
                .setCustomMetaParam(options.metaDataParam.map());
        return api.request(request).getResponse();
    }
}
