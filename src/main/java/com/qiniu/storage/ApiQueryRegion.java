package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.util.StringUtils;

import java.util.List;
import java.util.Map;

public class ApiQueryRegion extends Api {

    private static final String[] DEFAULT_UC_BACKUP_HOSTS = Configuration.defaultUcHosts;

    /**
     * api 构建函数
     *
     * @param client 请求client 【必须】
     */
    public ApiQueryRegion(Client client) {
        this(client, new Api.Config.Builder()
                .setHostRetryMax(DEFAULT_UC_BACKUP_HOSTS.length)
                .setHostProvider(HostProvider.arrayProvider(DEFAULT_UC_BACKUP_HOSTS))
                .build());
    }

    public ApiQueryRegion(Client client, Api.Config config) {
        super(client, config);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象 【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestWithInterceptor(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        private static final String DEFAULT_URL_PREFIX = "https://" + DEFAULT_UC_BACKUP_HOSTS[0];

        private String ak;
        private String bucket;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则使用默认，默认：Request.DEFAULT_URL_PREFIX
         * @param token     上传请求凭证 【必须】
         */
        public Request(String urlPrefix, String token) {
            super(StringUtils.isNullOrEmpty(urlPrefix) ? DEFAULT_URL_PREFIX : urlPrefix);
            setMethod(MethodType.GET);
            try {
                UploadToken upToken = new UploadToken(token);
                this.ak = upToken.getAccessKey();
                this.bucket = upToken.getBucket();
            } catch (QiniuException e) {
                e.printStackTrace();
            }
        }

        public Request(String urlPrefix, String ak, String bucket) {
            super(StringUtils.isNullOrEmpty(urlPrefix) ? DEFAULT_URL_PREFIX : urlPrefix);
            setMethod(MethodType.GET);
            this.ak = ak;
            this.bucket = bucket;
        }

        @Override
        protected void buildQuery() throws QiniuException {
            if (StringUtils.isNullOrEmpty(ak) || StringUtils.isNullOrEmpty(bucket)) {
                throw QiniuException.unrecoverable("query region error: ak and bucket can't empty");
            }

            addQueryPair("ak", this.ak);
            addQueryPair("bucket", this.bucket);
            super.buildQuery();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("/v4");
            addPathSegment("/query");
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
    public static class Response extends Api.Response {

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
         * @param region 区域信息
         * @return region id
         */
        public String getRegionId(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object regionId = ApiUtils.getValueFromMap(region, "region");
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
         * @param region 区域信息
         * @return region 缓存有效期
         */
        public Long getRegionTTL(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object ttl = ApiUtils.getValueFromMap(region, "ttl");
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
         * @param region 区域信息
         * @return Host 列表
         */
        public List<String> getRegionUpHosts(Map<String, Object> region) {
            if (region == null) {
                return null;
            }

            Object domains = ApiUtils.getValueFromMap(region, "up", "domains");
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
