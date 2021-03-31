package com.qiniu.storage.api;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class MakeBlockApi extends Api {

    public MakeBlockApi(Client client) {
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
            if (jsonMap == null) {
                return null;
            }

            Object crc = jsonMap.get("crc");
            if (crc == null) {
                return null;
            }

            return new Long(crc.toString());
        }

        public String getCtx() {
            if (jsonMap == null) {
                return null;
            }

            Object ctx = jsonMap.get("ctx");
            if (ctx == null) {
                return null;
            }

            return ctx.toString();
        }
    }
}
