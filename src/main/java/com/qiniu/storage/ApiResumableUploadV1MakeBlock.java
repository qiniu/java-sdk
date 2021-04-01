package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

public class ApiResumableUploadV1MakeBlock extends Api {

    public ApiResumableUploadV1MakeBlock(Client client) {
        super(client);
    }

    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        Integer blockSize;

        public Request(String host, String token, Integer blockSize) {
            super(host);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
            this.blockSize = blockSize;
        }

        public Request setBlockData(byte[] data, int offset, int size, String contentType) {
            super.setBody(data, offset, size, contentType);
            return this;
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

        @Override
        void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                throwInvalidRequestParamException("block data");
            }
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
