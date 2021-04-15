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
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                return makeBlock(host, block);
            }
        });
    }

    @Override
    Response completeUpload() throws QiniuException {
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                return makeFile(host);
            }
        });
    }

    private Response makeBlock(String host, ResumeUploadSource.Block block) throws QiniuException {
        ApiUploadV1MakeBlock api = new ApiUploadV1MakeBlock(client);
        ApiUploadV1MakeBlock.Request request = new ApiUploadV1MakeBlock.Request(host, token.getToken(), block.size)
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
            block.data = null;
        }

        return response.getResponse();
    }

    private Response makeFile(String host) throws QiniuException {
        String[] contexts = uploadSource.getAllBlockContextList();
        ApiUploadV1MakeFile api = new ApiUploadV1MakeFile(client);
        final ApiUploadV1MakeFile.Request request = new ApiUploadV1MakeFile.Request(host, token.getToken(), uploadSource.getSize(), contexts)
                .setKey(key)
                .setFileMimeType(options.mimeType)
                .setFileName(uploadSource.getFileName())
                .setCustomParam(options.params.map())
                .setCustomMetaParam(options.metaDataParam.map());
        return api.request(request).getResponse();
    }
}
