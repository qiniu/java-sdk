package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

public class MakeFileApi extends Api {
    public MakeFileApi(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        com.qiniu.http.Response response = client.post(request.getUrl(), request.body, request.bodyOffset, request.bodySize,
                request.header, request.bodyContentType);
        return new Response(response);
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        String key;
        String fileName;
        long fileSize;
        String fileMineType;
        public final StringMap params = new StringMap();
        public final StringMap metaDataParam = new StringMap();

        public Request(String host, String token) {
            super(host, token);
        }

        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        public Request setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Request setFileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Request setFileMineType(String fileMineType) {
            this.fileMineType = fileMineType;
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            pathList.add("mkfile");
            pathList.add(fileSize + "");
            pathList.add("mimeType");
            pathList.add(UrlSafeBase64.encodeToString(fileMineType));

            String path = String.format("/mkfile/%s/mimeType/%s", fileSize,
                    UrlSafeBase64.encodeToString(fileMineType));
            if (!StringUtils.isNullOrEmpty(fileName)) {
                pathList.add("fname");
                pathList.add(UrlSafeBase64.encodeToString(fileName));
            }

            if (key != null) {
                pathList.add("key");
                pathList.add(UrlSafeBase64.encodeToString(key));
            }

            params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    pathList.add(key);
                    pathList.add(UrlSafeBase64.encodeToString("" + value));
                }
            });

            metaDataParam.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    pathList.add(key);
                    pathList.add(UrlSafeBase64.encodeToString("" + value));
                }
            });

            super.buildQuery();
        }

        public void setBody(String[] contexts) throws QiniuException {
            String s = StringUtils.join(contexts, ",");
            byte[] body = StringUtils.utf8Bytes(s);
            setBody(body, body.length, 0, null);
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        public long getCrc() throws QiniuException {
            if (jsonMap.get("crc") == null) {
                throw new QiniuException(new Exception("block's crc32 is empty"));
            }
            return new Long(jsonMap.get("crc").toString());
        }

        public String getCtx() throws QiniuException {
            if (jsonMap.get("ctx") == null) {
                throw new QiniuException(new Exception("block's ctx is empty"));
            }
            return jsonMap.get("ctx").toString();
        }
    }
}
