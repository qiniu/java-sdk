package com.qiniu.storage;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Md5;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 分片上传基础接口实现，以及部分辅助代码
 * https://developer.qiniu.com/kodo/api/6364/multipartupload-interface
 *
 * 若上传到同区域，如全上传到 华东存储，则可只使用一个实例；
 * 若上传到不同区域，则每个区域最好单独使用一个示例。一个实例多区域并发上传可能导致内部频繁报错，上传效率低；
 */
public class MultipartUpload {
    private ConfigHelper configHelper;
    private Client client;
    private String host;

    /**
     * 若上传到同区域，如全上传到 华东存储，则可只使用一个实例；
     * 若上传到不同区域，则每个区域最好单独使用一个示例。一个实例多区域并发上传可能导致内部频繁报错，上传效率低；
     *
     * @param configuration Nullable, if null, then create a new one.
     * @param client        Nullable, if null, then create a new one with configuration.
     */
    public MultipartUpload(Configuration configuration, Client client) {
        if (configuration == null) {
            configuration = new Configuration();
        }
        if (client == null) {
            client = new Client(configuration);
        }
        this.configHelper = new ConfigHelper(configuration);
        this.client = client;
    }

    public void initUpHost(String uploadToken) throws QiniuException {
        if (host == null) {
            host = configHelper.upHost(uploadToken);
        }
    }

    public void changeHost(String upToken, String host) {
        try {
            this.host = configHelper.tryChangeUpHost(upToken, host);
        } catch (Exception e) {
            // ignore
            // use the old up host
        }
    }

    public Response initiateMultipartUpload(String bucket, String key, String upToken) throws QiniuException {
        initUpHost(upToken);

        String url = host + "/buckets/" + bucket + "/objects/" + genKey(key) + "/uploads";
        byte[] data = new byte[0];
        StringMap headers = new StringMap().put("Authorization", "UpToken " + upToken);
        String contentType = "";
        return client.post(url, data, headers, contentType);
    }

    public Response uploadPart(String bucket, String key, String upToken, String uploadId, byte[] data,
                               int dataOff, int dataLength, int partNum) throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + genKey(key) + "/uploads/" + uploadId + "/" + partNum;
        String md5 = Md5.md5(data, dataOff, dataLength);
        StringMap headers = new StringMap().
                put("Content-MD5", md5).
                put("Authorization", "UpToken " + upToken);
        return client.put(url, data, dataOff, dataLength, headers, "application/octet-stream");
    }

    public Response uploadPart(String bucket, String key, String upToken, String uploadId, byte[] data,
                               int partNum) throws QiniuException {
        return uploadPart(bucket, key, upToken, uploadId, data, 0, data.length, partNum);
    }

    public Response completeMultipartUpload(String bucket, String key, String upToken,
                                            String uploadId, List<EtagIdx> etags,
                                            String fileName, OptionsMeta params) throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + genKey(key) + "/uploads/" + uploadId;
        final StringMap headers = new StringMap().put("Authorization", "UpToken " + upToken);
        sortByPartNumberAsc(etags);
        byte[] data = new MakefileBody(etags, fileName, params)
                .json().getBytes(Charset.forName("UTF-8"));
        return client.post(url, data, headers, "application/json");
    }

    public Response abortMultipartUpload(String bucket, String key, String upToken,
                                         String uploadId) throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + genKey(key) + "/uploads/" + uploadId;
        StringMap headers = new StringMap().put("Authorization", "UpToken " + upToken);
        return client.delete(url, headers);
    }

    /**
     * @param key 指定的上传文件名。 null 对象是表示系统自动生成文件名(key)；其它表示使用指定的值为文件名(key)
     *            <p>
     *            上传到七牛存储保存的文件名， http 上传时需要对其进行编码，此方法根据要求生成对应的编码。
     *            注意:
     *            当 key 为空 "" 时表示空的文件名，正常进行 url_safe_base64 编码 ;
     *            当 key 为未进行 UrlSafeBase64 编码的字符  ~  的时候，表示未设置文件名：对应参数为 null 对象；
     *            具体行为如分片上传v1:  使用文件的 hash 作为文件名， 如果设置了saveKey则使用saveKey的规则进行文件命名
     */
    static String genKey(String key) {
        String base64Key = key != null ? UrlSafeBase64.encodeToString(key) : "~";
        return base64Key;
    }

    /**
     * 按照 partNumber 排序
     */
    public static void sortByPartNumberAsc(List<EtagIdx> etags) {
        Collections.sort(etags, new Comparator<EtagIdx>() {
            @Override
            public int compare(EtagIdx o1, EtagIdx o2) {
                return o1.partNumber - o2.partNumber; // small enough and both greater than 0 //
            }
        });
    }

    public static class InitRet {
        long expireAt;
        String uploadId;

        public InitRet(long expireAt, String uploadId) {
            this.expireAt = expireAt;
            this.uploadId = uploadId;
        }

        public long getExpireAt() {
            return expireAt;
        }

        public String getUploadId() {
            return uploadId;
        }
    }

    public static class UploadPartRet {
        String etag;
        String md5;

        public UploadPartRet(String etag, String md5) {
            this.etag = etag;
            this.md5 = md5;
        }

        public String getEtag() {
            return etag;
        }

        public String getMd5() {
            return md5;
        }
    }

    public static class EtagIdx {
        String etag; // mkfile need
        int partNumber; // mkfile need
        int size;

        EtagIdx(String etag, int idx, int size) {
            this.etag = etag;
            this.partNumber = idx;
            this.size = size;
        }

        public String toString() {
            return new Gson().toJson(this);
        }
    }


    public static class OptionsMeta {
        String mimeType;
        StringMap metadata;
        StringMap customVars;

        public OptionsMeta setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        /**
         * @param key   start with X-Qn-Meta-
         * @param value not null or empty
         */
        public OptionsMeta addMetadata(String key, String value) {
            if (metadata == null) {
                metadata = new StringMap();
            }
            metadata.put(key, value);
            return this;
        }

        /**
         * @param key   start with x:
         * @param value not null or empty
         */
        public OptionsMeta addCustomVar(String key, String value) {
            if (customVars == null) {
                customVars = new StringMap();
            }
            customVars.put(key, value);
            return this;
        }
    }


    class MakefileBody {
        List<EtagIdx> parts;
        String fname;
        String mimeType;
        Map<String, Object> metadata;
        Map<String, Object> customVars;

        MakefileBody(List<EtagIdx> etags, String fileName, OptionsMeta params) {
            this.parts = etags;
            this.fname = fileName;
            if (params != null) {
                this.mimeType = params.mimeType;
                if (params.metadata != null && params.metadata.size() > 0) {
                    this.metadata = filterParam(params.metadata, "X-Qn-Meta-");
                }
                if (params.customVars != null && params.customVars.size() > 0) {
                    this.customVars = filterParam(params.customVars, "x:");
                }
            }
        }

        private Map<String, Object> filterParam(StringMap param, final String keyPrefix) {
            final Map<String, Object> ret = new HashMap<>();
            final String prefix = keyPrefix.toLowerCase();
            param.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    if (key != null && value != null && !StringUtils.isNullOrEmpty(value.toString())
                            && key.toLowerCase().startsWith(prefix)) {
                        ret.put(key, value);
                    }
                }
            });
            return ret;
        }

        public String json() {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setExclusionStrategies(new CompleteMultipartUploadExclusionStrategy()).create();
            return gson.toJson(this);
        }
    }

    static class CompleteMultipartUploadExclusionStrategy implements ExclusionStrategy {

        /**
         * 只需要 etag partNumber ，不需要 size
         */
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return EtagIdx.class == f.getDeclaringClass() && "size".equals(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

}
