package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Crc32;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

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
                return makeFile(host, uploadSource.getFileName());
            }
        });
    }

    private Response makeBlock(String host, ResumeUploadSource.Block block) throws QiniuException {
        String action = String.format("/mkblk/%d", block.size);
        String url = host + action;

        Response response = post(url, block.data, 0, block.size);

        System.out.printf("== make block:%d upload :%s \n", block.index, response);

        if (response.isOK()) {

            StringMap jsonMap = response.jsonToMap();
            if (jsonMap == null) {
                throw new QiniuException(new Exception("block's info is empty"));
            }

            if (options.checkCrc) {
                if (jsonMap.get("crc") == null) {
                    throw new QiniuException(new Exception("block's crc32 is empty"));
                }

                long crc = Crc32.bytes(block.data, 0, block.size);
                long serverCrc = new Long(jsonMap.get("crc").toString());
                if ((long) serverCrc != crc) {
                    throw new QiniuException(new Exception("block's crc32 is not match"));
                }
            }

            if (jsonMap.get("ctx") == null) {
                throw new QiniuException(new Exception("block's ctx is empty"));
            }
            block.context = jsonMap.get("ctx").toString();
            block.data = null;
        }
        return response;
    }

    private Response makeFile(String host, String fileName) throws QiniuException {
        String[] contexts = uploadSource.getAllBlockContextList();
        String action = String.format("/mkfile/%s/mimeType/%s", uploadSource.getSize(),
                UrlSafeBase64.encodeToString(options.mimeType));
        if (!StringUtils.isNullOrEmpty(fileName)) {
            action = String.format("%s/fname/%s", action, UrlSafeBase64.encodeToString(fileName));
        }

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
        byte[] data = StringUtils.utf8Bytes(s);
        return post(url, StringUtils.utf8Bytes(s), (int) 0, data.length);
    }
}
