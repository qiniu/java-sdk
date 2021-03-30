package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class MakeBlockApi extends Api {

    public MakeBlockApi(Client client) {
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
        int blockSize = -1;

        public Request(String host, String token) {
            super(host, token);
        }

        public Request setBlockSize(int blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        @Override
        public void buildAction() throws QiniuException {
            action = String.format("/mkblk/%d", blockSize);
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
