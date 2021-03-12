package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Json;
import com.qiniu.util.Md5;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


class ResumeUploadPerformerV2 extends ResumeUploadPerformer {

    ResumeUploadPerformerV2(Client client, String key, UploadToken token, UploadSource source, Recorder recorder, UploadOptions options, Configuration config) {
        super(client, key, token, source, recorder, options, config);
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
    Response uploadNextData() throws QiniuException {
        final ResumeUploadSource.Block block = getNextUploadingBlock();
        return retryUploadAction(new UploadAction() {
            @Override
            public Response uploadAction(String host) throws QiniuException {
                Response response = uploadPart(host, block);
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
                return completeParts(host);
            }
        });
    }

    private Response initPart(String host) throws QiniuException {

        String action = String.format(Locale.ENGLISH, "/buckets/%s/objects/%s", this.token.getBucket(), resumeV2EncodeKey(key));
        String url = host + action;
        return post(url, null);
    }

    private Response uploadPart(String host, ResumeUploadSource.Block block) throws QiniuException {
        String uploadId = uploadSource.uploadId;
        int partIndex = block.index;
        byte[] blockData = null;
        try {
            blockData = uploadSource.getBlockData(block);
        } catch (IOException e) {
            throw QiniuException.unrecoverable(e);
        }

        String action = String.format("/buckets/%s/objects/%s/uploads/%s/%d", token.getBucket(), resumeV2EncodeKey(key), uploadId, partIndex);
        String url = host + action;
        Response response = put(url, blockData);

        if (response != null && response.isOK()) {

            StringMap jsonMap = response.jsonToMap();
            if (jsonMap == null) {
                throw new QiniuException(new Exception("block's info is empty"));
            }

            if (options.checkCrc) {
                if (jsonMap.get("md5") == null) {
                    throw new QiniuException(new Exception("block's md5 is empty"));
                }

                String md5 = Md5.md5(blockData);
                String serverMd5 = jsonMap.get("md5").toString();
                if (!serverMd5.equals(md5)) {
                    throw new QiniuException(new Exception("block's md5 is not match"));
                }
            }

            if (jsonMap.get("etag") == null) {
                throw new QiniuException(new Exception("block's etag is empty"));
            }
            block.etag = jsonMap.get("etag").toString();
        }

        return response;
    }

    private Response completeParts(String host) throws QiniuException {

        List<StringMap> partInfoArray = uploadSource.getPartInfo();
        String uploadId = uploadSource.uploadId;

        String action = String.format("/buckets/%s/objects/%s/uploads/%s", token.getBucket(), resumeV2EncodeKey(key), uploadId);
        String url = host + action;

        HashMap<String, Object> bodyMap = new HashMap<>();
        if (partInfoArray != null) {
            bodyMap.put("parts", partInfoArray);
        }
        if (uploadSource.fileName != null) {
            bodyMap.put("fname", uploadSource.fileName);
        }
        if (options.mimeType != null) {
            bodyMap.put("mimeType", options.mimeType);
        }
        if (options.params != null) {
            bodyMap.put("customVars", options.params);
        }
        if (options.metaDataParam != null) {
            bodyMap.put("metaData", options.metaDataParam);
        }

        String bodyString = Json.encode(bodyMap);
        byte[] body = bodyString.getBytes();
        return post(url, body);
    }

    private String resumeV2EncodeKey(String key) {
        String encodeKey = null;
        if (key == null) {
            encodeKey = "~";
        } else if (key.equals("")) {
            encodeKey = "";
        } else {
            encodeKey = UrlSafeBase64.encodeToString(key);
        }
        return encodeKey;
    }
}
