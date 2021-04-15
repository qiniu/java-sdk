package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.util.StringUtils;

import java.util.List;
import java.util.Map;

public class ApiQueryRegion extends ApiUpload {

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiQueryRegion(Client client) {
        super(client);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象 【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends ApiUpload.Request {
        private static final String DEFAULT_URL_PREFIX = "https://uc.qbox.me";

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则使用默认，默认：Request.DEFAULT_URL_PREFIX
         * @param token     请求凭证 【必须】
         */
        public Request(String urlPrefix, String token) {
            super(StringUtils.isNullOrEmpty(urlPrefix) ? DEFAULT_URL_PREFIX : urlPrefix);
            setToken(token);
            setMethod(MethodType.GET);
        }

        @Override
        protected void buildQuery() throws QiniuException {
            UploadToken token = getUploadToken();
            addQueryPair("ak", token.getAccessKey());
            addQueryPair("bucket", token.getBucket());
            super.buildQuery();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("v4");
            addPathSegment("query");
            super.buildPath();
        }
    }

    /**
     * 响应信息
     * dataMap：
     * { "hosts":[
     * { "region": "z1",
     * "ttl": 86400,
     * "io": { "domains": [ "iovip-z1.qbox.me" ] },
     * "up": { "domains": [ "upload-z1.qiniup.com", "up-z1.qiniup.com" ], "old": [ "upload-z1.qbox.me", "up-z1.qbox.me"] },
     * "uc": { "domains": [ "uc.qbox.me" ]},
     * "rs": { "domains": [ "rs-z1.qbox.me" ] },
     * "rsf": { "domains": [ "rsf-z1.qbox.me" ] },
     * "api": { "domains": [ "api-z1.qiniu.com" ] },
     * "s3": { "domains": [ "s3-cn-north-1.qiniucs.com" ], "region_alias": "cn-north-1" } }
     * ] }
     */
    public static class Response extends ApiUpload.Response {

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        /**
         * 获取默认 region 的 id
         *
         * @return region id
         */
        public String getDefaultRegionId() {
            return getRegionId(getDefaultRegion());
        }

        /**
         * 获取 region id
         *
         * @return region id
         */
        public String getRegionId(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object regionId = ApiUtils.getValueFromMap(region, new String[]{"region"});
            return regionId.toString();
        }

        /**
         * 获取默认 region 缓存有效期
         *
         * @return region 缓存有效期
         */
        public Long getDefaultRegionTTL() {
            return getRegionTTL(getDefaultRegion());
        }

        /**
         * 获取 region 缓存有效期
         *
         * @return region 缓存有效期
         */
        public Long getRegionTTL(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object ttl = ApiUtils.getValueFromMap(region, new String[]{"ttl"});
            return ApiUtils.objectToLong(ttl);
        }

        /**
         * 获取默认 region 上传的 Host 列表
         *
         * @return Host 列表
         */
        public List<String> getDefaultRegionUpHosts() {
            return getRegionUpHosts(getDefaultRegion());
        }

        /**
         * 获取 region 上传的 Host 列表
         *
         * @return Host 列表
         */
        public List<String> getRegionUpHosts(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object domains = ApiUtils.getValueFromMap(region, new String[]{"up", "domains"});
            if (!(domains instanceof List)) {
                return null;
            }
            return (List<String>) domains;
        }

        /**
         * 获取默认的 region， 默认第一个
         *
         * @return 默认的 region
         */
        private Map<String, Object> getDefaultRegion() {
            return getRegion(0);
        }

        /**
         * 根据 region index 获取 region
         * data map 中 region 有多个，可以根据 region 下标获取 region
         *
         * @param index region 下标
         * @return region
         */
        public Map<String, Object> getRegion(int index) {
            Object value = getValueFromDataMap("hosts");
            if (!(value instanceof List) || ((List) value).size() < index) {
                return null;
            }

            Object regionObject = ((List) value).get(index);
            if (!(regionObject instanceof Map)) {
                return null;
            }
            return (Map<String, Object>) regionObject;
        }
    }
}
