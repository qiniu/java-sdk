package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

public class ApiResumableUploadV1MakeFile extends Api {
    public ApiResumableUploadV1MakeFile(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        com.qiniu.http.Response response = client.post(request.getUrl().toString(), request.body, request.bodyOffset, request.bodySize,
                request.getHeader(), request.bodyContentType);
        return new Response(response);
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        String key;
        String fileName;
        Long fileSize;
        String fileMimeType;
        public final StringMap params = new StringMap();
        public final StringMap metaDataParam = new StringMap();

        public Request(String host, String token, Long fileSize, String[] blockContexts) {
            super(host);
            setToken(token);
            this.fileSize = fileSize;
            this.setBlockContexts(blockContexts);
        }

        public Request setKey(String key) {
            this.key = key;
            return this;
        }

        public Request setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Request setFileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }

        private void setBlockContexts(String[] contexts) {
            String s = StringUtils.join(contexts, ",");
            byte[] body = StringUtils.utf8Bytes(s);
            setBody(body, body.length, 0, null);
        }

        @Override
        public void buildPath() throws QiniuException {
            addPathSegment("mkfile");
            addPathSegment(fileSize + "");

            if (!StringUtils.isNullOrEmpty(fileMimeType)) {
                addPathSegment("mimeType");
                addPathSegment(UrlSafeBase64.encodeToString(fileMimeType));
            }

            if (!StringUtils.isNullOrEmpty(fileName)) {
                addPathSegment("fname");
                addPathSegment(UrlSafeBase64.encodeToString(fileName));
            }

            if (key != null) {
                addPathSegment("key");
                addPathSegment(UrlSafeBase64.encodeToString(key));
            }

            params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + value));
                }
            });

            metaDataParam.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    addPathSegment(key);
                    addPathSegment(UrlSafeBase64.encodeToString("" + value));
                }
            });

            super.buildQuery();
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }
    }
}
