package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.*;

import java.util.*;


class ResumeUploadPerformerV2 extends ResumeUploadPerformer {

    ResumeUploadPerformerV2(Client client, String key, UploadToken token, ResumeUploadSource source, Recorder recorder,
                            UploadOptions options, Configuration config) {
        super(client, key, token, source, recorder, options, config);
    }

    @Override
    boolean shouldInit() {
        // 包含 uploadId 且有效
        if (StringUtils.isNullOrEmpty(uploadSource.uploadId)) {
            return true;
        }

        long currentTimestamp = new Date().getTime() / 1000;
        long expireAtTimestamp = uploadSource.expireAt - 24 * 3600 * 3;
        return expireAtTimestamp < currentTimestamp;
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
        String action = String.format(Locale.ENGLISH, "/buckets/%s/objects/%s/uploads", this.token.getBucket(), resumeV2EncodeKey(key));
        String url = host + action;
        Response response = post(url, new byte[0], 0, 0);
        if (response != null && response.isOK()) {

            StringMap jsonMap = response.jsonToMap();
            if (jsonMap == null) {
                throw new QiniuException(new Exception("upload's info is empty"));
            }

            String uploadId = jsonMap.get("uploadId").toString();
            Object expireAt = jsonMap.get("expireAt");
            if (uploadId == null || expireAt == null) {
                throw new QiniuException(new Exception("uploadId or expireAt is empty"));
            }
            uploadSource.uploadId = uploadId;
            if (expireAt instanceof Double) {
                uploadSource.expireAt = ((Double) expireAt).longValue();
            } else if (expireAt instanceof Integer) {
                uploadSource.expireAt = ((Integer) expireAt).longValue();
            } else if (expireAt instanceof Long) {
                uploadSource.expireAt = (Long) expireAt;
            } else {
                uploadSource.expireAt = new Date().getTime() / 1000;
            }
        }
        return response;
    }

    private Response uploadPart(String host, ResumeUploadSource.Block block) throws QiniuException {
        String uploadId = uploadSource.uploadId;
        int partIndex = block.index;
        String action = String.format("/buckets/%s/objects/%s/uploads/%s/%d", token.getBucket(), resumeV2EncodeKey(key), uploadId, partIndex);
        String url = host + action;
        Response response = put(url, block.data, 0, block.size);

        if (response != null && response.isOK()) {

            StringMap jsonMap = response.jsonToMap();
            if (jsonMap == null) {
                throw new QiniuException(new Exception("block's info is empty"));
            }

            if (options.checkCrc) {
                if (jsonMap.get("md5") == null) {
                    throw new QiniuException(new Exception("block's md5 is empty"));
                }

                String md5 = Md5.md5(block.data);
                String serverMd5 = jsonMap.get("md5").toString();
                if (!serverMd5.equals(md5)) {
                    throw new QiniuException(new Exception("block's md5 is not match"));
                }
            }

            if (jsonMap.get("etag") == null) {
                throw new QiniuException(new Exception("block's etag is empty"));
            }
            block.etag = jsonMap.get("etag").toString();
            block.data = null;
        }

        return response;
    }

    private Response completeParts(String host) throws QiniuException {

        List<Map<String, Object>> partInfoArray = uploadSource.getPartInfo();
        String uploadId = uploadSource.uploadId;

        String action = String.format("/buckets/%s/objects/%s/uploads/%s", token.getBucket(), resumeV2EncodeKey(key), uploadId);
        String url = host + action;

        HashMap<String, Object> bodyMap = new HashMap<>();
        if (partInfoArray != null) {
            bodyMap.put("parts", partInfoArray);
        }
        if (uploadSource.getFileName() != null) {
            bodyMap.put("fname", uploadSource.getFileName());
        }
        if (options.mimeType != null) {
            bodyMap.put("mimeType", options.mimeType);
        }
        if (options.params != null) {
            bodyMap.put("customVars", options.params.map());
        }
        if (options.metaDataParam != null) {
            bodyMap.put("metaData", options.metaDataParam.map());
        }

        String bodyString = Json.encode(bodyMap);
        byte[] body = bodyString.getBytes();
        return post(url, body, 0, body.length);
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
