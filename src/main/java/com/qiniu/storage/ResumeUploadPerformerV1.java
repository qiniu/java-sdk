package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.*;

import java.io.IOException;

class ResumeUploadPerformerV1 extends ResumeUploadPerformer {


    ResumeUploadPerformerV1(Client client, String key, UploadToken token, UploadSource source, Recorder recorder,
                            UploadOptions options, Configuration config) {
        super(client, key, token, source, recorder, options, config);
    }

    @Override
    Response uploadInit() throws QiniuException {
        return Response.createSuccessResponse();
    }

    @Override
    Response uploadNextData() throws QiniuException {
        final ResumeUploadSource.Block block = getNextUploadingBlock();
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                Response response = makeBlock(host, block);
                block.updateState();
                return response;
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
        String action = String.format("/mkblk/%d", block.size);
        String url = host + action;

        byte[] blockData = null;
        try {
            blockData = uploadSource.getBlockData(block);
        } catch (IOException e) {
            throw QiniuException.unrecoverable(e);
        }

        Response response = post(url, blockData);
        if (response.isOK()) {

            StringMap jsonMap = response.jsonToMap();
            if (jsonMap == null) {
                throw new QiniuException(new Exception("block's info is empty"));
            }

            if (options.checkCrc) {
                if (jsonMap.get("crc") == null) {
                    throw new QiniuException(new Exception("block's crc32 is empty"));
                }

                long crc = Crc32.bytes(blockData, 0, block.size);
                long serverCrc = new Long(jsonMap.get("crc").toString());
                if (serverCrc != crc) {
                    throw new QiniuException(new Exception("block's crc32 is not match"));
                }
            }

            if (jsonMap.get("ctx") == null) {
                throw new QiniuException(new Exception("block's ctx is empty"));
            }
            block.context = jsonMap.get("ctx").toString();
        }
        return response;
    }

    private Response makeFile(String host) throws QiniuException {
        String[] contexts = uploadSource.getAllBlockContextList();
        String action = String.format("/mkfile/%s/mimeType/%s", uploadSource.size, UrlSafeBase64.encodeToString(options.mimeType));
        String url = host + action;

        final StringBuilder b = new StringBuilder(url);
        if (key != null) {
            b.append("/key/");
            b.append(UrlSafeBase64.encodeToString(key));
        }

        if (options.params != null) {
            options.params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    b.append("/");
                    b.append(key);
                    b.append("/");
                    b.append(UrlSafeBase64.encodeToString("" + value));
                }
            });
        }

        if (options.metaDataParam != null) {
            options.metaDataParam.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    b.append("/");
                    b.append(key);
                    b.append("/");
                    b.append(UrlSafeBase64.encodeToString("" + value));
                }
            });
        }

        url = b.toString();
        String s = StringUtils.join(contexts, ",");
        return post(url, StringUtils.utf8Bytes(s));
    }
}
