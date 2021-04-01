package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class ApiResumableUploadV1MakeBlock extends Api {

    public ApiResumableUploadV1MakeBlock(Client client) {
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
        Integer blockSize;

        public Request(String host, String token, Integer blockSize) {
            super(host);
            setToken(token);
            this.blockSize = blockSize;
        }

        @Override
        public void buildPath() throws QiniuException {
            if (blockSize == null) {
                throwInvalidRequestParamException("block size");
            }

            addPathSegment("mkblk");
            addPathSegment(blockSize + "");
            super.buildPath();
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        public Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        public Long getCrc() {
            return getLongValueFromDataMap("crc");
        }

        public String getCtx() {
            return getStringValueFromDataMap("ctx");
        }
    }
}
