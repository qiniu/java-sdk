package com.qiniu.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.UncheckedQiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.Response;
import com.qiniu.storage.model.*;
import com.qiniu.util.*;

import java.util.*;

/**
 * 主要涉及了空间资源管理及批量操作接口的实现，具体的接口规格可以参考
 * 参考文档：<a href="http://developer.qiniu.com/kodo/api/rs">资源管理</a>
 */
public final class BucketManager {

    /**
     * Auth 对象
     * 该类需要使用QBox鉴权，所以需要指定Auth对象
     */
    private final Auth auth;
    /**
     * HTTP Client 对象
     * 该类需要通过该对象来发送HTTP请求
     */
    private final Client client;

    /**
     * ConfigHelper 对象
     * 该类相关的域名配置，解析配置，HTTP请求超时时间设置等
     */
    private ConfigHelper configHelper;

    private Configuration config;

    /**
     * 构建一个新的 BucketManager 对象
     *
     * @param auth Auth对象
     * @param cfg  Configuration对象
     */
    public BucketManager(Auth auth, Configuration cfg) {
        this.auth = auth;
        Configuration c2 = cfg == null ? new Configuration() : cfg.clone();
        this.config = c2;
        this.configHelper = new ConfigHelper(c2);
        client = new Client(c2);
    }

    public BucketManager(Auth auth, Client client) {
        this.auth = auth;
        this.client = client;
        this.config = new Configuration();
        this.configHelper = new ConfigHelper(this.config);
    }

    public BucketManager(Auth auth, Configuration cfg, Client client) {
        this.auth = auth;
        this.client = client;
        Configuration c2 = cfg == null ? new Configuration() : cfg.clone();
        this.config = c2;
        this.configHelper = new ConfigHelper(c2);
    }

    /**
     * EncodedEntryURI格式，其中 bucket+":"+key 称之为 entry
     *
     * @param bucket 空间名
     * @param key    文件 key
     * @return UrlSafeBase64.encodeToString(entry)
     * <a href="http://developer.qiniu.com/kodo/api/data-format"> 相关链接 </a>
     */
    public static String encodedEntry(String bucket, String key) {
        String encodedEntry;
        if (key != null) {
            encodedEntry = UrlSafeBase64.encodeToString(bucket + ":" + key);
        } else {
            encodedEntry = UrlSafeBase64.encodeToString(bucket);
        }
        return encodedEntry;
    }

    /**
     * EncodedEntryURI格式，用在不指定key值的情况下
     *
     * @param bucket 空间名称
     * @return UrlSafeBase64.encodeToString(bucket)
     * <a href="http://developer.qiniu.com/kodo/api/data-format"> 相关链接 </a>
     */
    public static String encodedEntry(String bucket) {
        return encodedEntry(bucket, null);
    }

    /**
     * 获取账号下所有空间名称列表
     *
     * @return 空间名称列表
     * @throws QiniuException 异常
     */
    public String[] buckets() throws QiniuException {
        Response res = bucketsResponse();
        String[] buckets = res.jsonToObject(String[].class);
        res.close();
        return buckets;
    }

    public Response bucketsResponse() throws QiniuException {
        String url = String.format("%s/buckets", configHelper.ucHost());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 创建空间
     *
     * @param bucketName 空间名
     * @param region     区域信息
     * @return Response
     * @throws QiniuException 异常
     */
    public Response createBucket(String bucketName, String region) throws QiniuException {
        String url = String.format("%s/mkbucketv3/%s/region/%s", configHelper.ucHost(), bucketName, region);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 删除空间
     *
     * @param bucketName 空间名
     * @return Response
     * @throws QiniuException 异常
     */
    public Response deleteBucket(String bucketName) throws QiniuException {
        String url = String.format("%s/drop/%s", configHelper.ucHost(), bucketName);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取该空间下所有的domain
     *
     * @param bucket 空间名
     * @return 该空间名下的 domain
     * @throws QiniuException 异常
     */
    public String[] domainList(String bucket) throws QiniuException {
        Response res = domainListResponse(bucket);
        String[] domains = res.jsonToObject(String[].class);
        res.close();
        return domains;
    }

    public Response domainListResponse(String bucket) throws QiniuException {
        String url = String.format("%s/v2/domains?tbl=%s", configHelper.ucHost(), bucket);
        Response res = get(url, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 根据前缀获取文件列表的迭代器
     *
     * @param bucket 空间名
     * @param prefix 文件名前缀
     * @return FileInfo迭代器
     */
    public FileListIterator createFileListIterator(String bucket, String prefix) {
        return new FileListIterator(bucket, prefix, 1000, null);
    }

    /**
     * 根据前缀获取文件列表的迭代器
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param limit     每次迭代的长度限制，最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return FileInfo迭代器
     */
    public FileListIterator createFileListIterator(String bucket, String prefix, int limit, String delimiter) {
        return new FileListIterator(bucket, prefix, limit, delimiter);
    }

    private String listQuery(String bucket, String prefix, String marker, int limit, String delimiter) {
        StringMap map = new StringMap().put("bucket", bucket).putNotEmpty("marker", marker)
                .putNotEmpty("prefix", prefix).putNotEmpty("delimiter", delimiter).putWhen("limit", limit, limit > 0);
        return map.formString();
    }

    /**
     * 列举空间文件 v1 接口，返回一个 response 对象。
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return Response
     * @throws QiniuException 异常
     */
    public Response listV1(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        String url = String.format("%s/list?%s", configHelper.rsfHost(auth.accessKey, bucket),
                listQuery(bucket, prefix, marker, limit, delimiter));
        return get(url);
    }

    /**
     * 列举空间文件 v1 接口
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，推荐值 1000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return FileListing
     * @throws QiniuException 异常
     */
    public FileListing listFiles(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        Response response = listV1(bucket, prefix, marker, limit, delimiter);
        if (!response.isOK()) {
            response.close();
            throw new QiniuException(response);
        }
        FileListing fileListing = response.jsonToObject(FileListing.class);
        response.close();
        return fileListing;
    }

    /**
     * 列举空间文件 v2 接口，返回一个 response 对象。v2 接口可以避免由于大量删除导致的列举超时问题，返回的 response 对象中的 body 可以转换为
     * string stream 来处理。
     * Deprecated，使用 {@link BucketManager#listV1(String, String, String, int, String)} } 替换
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，推荐值 1000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return Response 返回一个 okhttp response 对象
     * @throws QiniuException 异常
     */
    @Deprecated
    public Response listV2(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        String url = String.format("%s/v2/list?%s", configHelper.rsfHost(auth.accessKey, bucket),
                listQuery(bucket, prefix, marker, limit, delimiter));
        return post(url, null);
    }

    /**
     * 列举空间文件 v2 接口
     * Deprecated，使用 {@link BucketManager#listFiles(String, String, String, int, String)} 替换
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，推荐值 1000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return FileListing
     * @throws QiniuException 异常
     */
    @Deprecated
    public FileListing listFilesV2(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        Response response = listV2(bucket, prefix, marker, limit, delimiter);
        final String result = response.bodyString();
        response.close();
        List<String> lineList = Arrays.asList(result.split("\n"));
        FileListing fileListing = new FileListing();
        List<FileInfo> fileInfoList = new ArrayList<>();
        Set<String> commonPrefixSet = new HashSet<>();
        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            JsonObject jsonObject = Json.decode(line, JsonObject.class);
            if (jsonObject == null) {
                continue;
            }
            if (!(jsonObject.get("item") instanceof JsonNull)) {
                fileInfoList.add(Json.decode(jsonObject.get("item"), FileInfo.class));
            }
            String dir = jsonObject.get("dir").getAsString();
            if (!"".equals(dir)) commonPrefixSet.add(dir);
            if (i == lineList.size() - 1)
                fileListing.marker = jsonObject.get("marker").getAsString();
        }
        fileListing.items = fileInfoList.toArray(new FileInfo[]{});
        fileListing.commonPrefixes = commonPrefixSet.toArray(new String[]{});
        return fileListing;
    }

    /**
     * 获取空间中文件的属性
     *
     * @param bucket  空间名称
     * @param fileKey 文件名称
     * @return 文件属性
     * @throws QiniuException 异常
     *                        <a href="http://developer.qiniu.com/kodo/api/stat"> 相关链接 </a>
     */
    public FileInfo stat(String bucket, String fileKey) throws QiniuException {
        Response res = statResponse(bucket, fileKey);
        FileInfo fileInfo = res.jsonToObject(FileInfo.class);
        res.close();
        return fileInfo;
    }

    public Response statResponse(String bucket, String fileKey) throws QiniuException {
        Response res = rsPost(bucket, String.format("/stat/%s", encodedEntry(bucket, fileKey)), null);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 删除指定空间、文件名的文件
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @return Response
     * @throws QiniuException 异常
     *                        <a href="http://developer.qiniu.com/kodo/api/delete"> 相关链接 </a>
     */
    public Response delete(String bucket, String key) throws QiniuException {
        return rsPost(bucket, String.format("/delete/%s", encodedEntry(bucket, key)), null);
    }

    /**
     * 修改文件的MimeType
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param mime   文件的新MimeType
     * @return Response
     * @throws QiniuException 异常
     *                        <a href="http://developer.qiniu.com/kodo/api/chgm"> 相关链接 </a>
     */
    public Response changeMime(String bucket, String key, String mime)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String encodedMime = UrlSafeBase64.encodeToString(mime);
        String path = String.format("/chgm/%s/mime/%s", resource, encodedMime);
        return rsPost(bucket, path, null);
    }

    /**
     * 修改文件的元数据
     *
     * @param bucket  空间名称
     * @param key     文件名称
     * @param headers 需要修改的文件元数据
     * @return Response
     * @throws QiniuException 异常
     *                        <a href="https://developer.qiniu.com/kodo/api/1252/chgm"> 相关链接 </a>
     */
    public Response changeHeaders(String bucket, String key, Map<String, String> headers)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chgm/%s", resource);
        for (String k : headers.keySet()) {
            String encodedMetaValue = UrlSafeBase64.encodeToString(headers.get(k));
            path = String.format("%s/x-qn-meta-!%s/%s", path, k, encodedMetaValue);
        }
        return rsPost(bucket, path, null);
    }

    /**
     * 修改文件的类型（普通存储或低频存储）
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param type   存储类型
     * @return Response
     * @throws QiniuException 异常
     */
    public Response changeType(String bucket, String key, StorageType type)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chtype/%s/type/%d", resource, type.ordinal());
        return rsPost(bucket, path, null);
    }

    /**
     * 解冻归档存储
     * 文档：https://developer.qiniu.com/kodo/api/6380/restore-archive
     *
     * @param bucket          空间名称
     * @param key             文件名称
     * @param freezeAfterDays 解冻有效时长，取值范围 1～7
     * @return Response
     * @throws QiniuException 异常
     */
    public Response restoreArchive(String bucket, String key, int freezeAfterDays)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/restoreAr/%s/freezeAfterDays/%s", resource, Integer.toString(freezeAfterDays));
        String requestUrl = configHelper.rsHost(auth.accessKey, bucket) + path;
        return client.post(requestUrl, null,
                auth.authorizationV2(requestUrl, "POST", null, "application/json"), Client.JsonMime);
    }

    /**
     * 修改文件的状态（禁用或者正常）
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param status 0表示启用；1表示禁用。
     * @return Response
     * @throws QiniuException 异常
     */
    public Response changeStatus(String bucket, String key, int status)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/chstatus/%s/status/%d", resource, status);
        return rsPost(bucket, path, null);
    }

    /**
     * 重命名空间中的文件，可以设置force参数为true强行覆盖空间已有同名文件
     *
     * @param bucket     空间名称
     * @param oldFileKey 文件名称
     * @param newFileKey 新文件名
     * @param force      强制覆盖空间中已有同名（和 newFileKey 相同）的文件
     * @return Response
     * @throws QiniuException 异常
     */
    public Response rename(String bucket, String oldFileKey, String newFileKey, boolean force)
            throws QiniuException {
        return move(bucket, oldFileKey, bucket, newFileKey, force);
    }

    /**
     * 重命名空间中的文件
     *
     * @param bucket     空间名称
     * @param oldFileKey 文件名称
     * @param newFileKey 新文件名
     * @return Response
     * @throws QiniuException 异常
     *                        <a href="http://developer.qiniu.com/kodo/api/move">相关链接</a>
     */
    public Response rename(String bucket, String oldFileKey, String newFileKey)
            throws QiniuException {
        return move(bucket, oldFileKey, bucket, newFileKey);
    }

    /**
     * 复制文件，要求空间在同一账号下，可以设置force参数为true强行覆盖空间已有同名文件
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @return Response
     * @throws QiniuException 异常
     */
    public Response copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force)
            throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/copy/%s/%s/force/%s", from, to, force);
        return rsPost(fromBucket, path, null);
    }

    /**
     * 复制文件，要求空间在同一账号下
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @return Response
     * @throws QiniuException 异常
     */
    public Response copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey)
            throws QiniuException {
        Response res = copy(fromBucket, fromFileKey, toBucket, toFileKey, false);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 移动文件，要求空间在同一账号下
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @return Response
     * @throws QiniuException 异常
     */
    public Response move(String fromBucket, String fromFileKey, String toBucket,
                         String toFileKey, boolean force) throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/move/%s/%s/force/%s", from, to, force);
        return rsPost(fromBucket, path, null);
    }

    /**
     * 移动文件。要求空间在同一账号下, 可以添加force参数为true强行移动文件。
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @return Response
     * @throws QiniuException 异常
     */
    public Response move(String fromBucket, String fromFileKey, String toBucket, String toFileKey)
            throws QiniuException {
        return move(fromBucket, fromFileKey, toBucket, toFileKey, false);
    }

    /**
     * 抓取指定地址的文件，以指定名称保存在指定空间
     * 要求指定url可访问，大文件不建议使用此接口抓取。可先下载再上传
     * 如果不指定保存的文件名，那么以文件内容的 etag 作为文件名
     *
     * @param url    待抓取的文件链接
     * @param bucket 文件抓取后保存的空间
     * @return Response
     * @throws QiniuException 异常
     */
    public FetchRet fetch(String url, String bucket) throws QiniuException {
        return fetch(url, bucket, null);
    }

    /**
     * 抓取指定地址的文件，以指定名称保存在指定空间
     * 要求指定url可访问，大文件不建议使用此接口抓取。可先下载再上传
     *
     * @param url    待抓取的文件链接
     * @param bucket 文件抓取后保存的空间
     * @param key    文件抓取后保存的文件名
     * @return Response
     * @throws QiniuException 异常
     */
    public FetchRet fetch(String url, String bucket, String key) throws QiniuException {
        Response res = fetchResponse(url, bucket, key);
        FetchRet fetchRet = res.jsonToObject(FetchRet.class);
        res.close();
        return fetchRet;
    }

    public Response fetchResponse(String url, String bucket, String key) throws QiniuException {
        String resource = UrlSafeBase64.encodeToString(url);
        String to = encodedEntry(bucket, key);
        String path = String.format("/fetch/%s/to/%s", resource, to);
        Response res = ioPost(bucket, path);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * 主要对于大文件进行抓取
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     *
     * @param url    待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket 文件抓取后保存的空间
     * @param key    文件抓取后保存的文件名
     * @return Response
     * @throws QiniuException 异常
     */
    public Response asynFetch(String url, String bucket, String key) throws QiniuException {
        StringMap params = new StringMap().putNotNull("key", key);
        return asyncFetch(url, bucket, params);
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     * 提供更多参数的抓取 可以对抓取文件进行校验 和自定义抓取回调地址等
     *
     * @param url              待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket           文件抓取后保存的空间
     * @param key              文件抓取后保存的文件名
     * @param md5              文件md5,传入以后会在存入存储时对文件做校验，校验失败则不存入指定空间
     * @param etag             文件etag,传入以后会在存入存储时对文件做校验，校验失败则不存入指定空间
     * @param callbackurl      回调URL，详细解释请参考上传策略中的callbackUrl
     * @param callbackbody     回调Body，如果callbackurl不为空则必须指定。与普通上传一致支持魔法变量，
     * @param callbackbodytype 回调Body内容类型,默认为"application/x-www-form-urlencoded"，
     * @param callbackhost     回调时使用的Host
     * @param fileType         存储文件类型 0:正常存储(默认),1:低频存储
     * @return Response
     * @throws QiniuException 异常
     */
    public Response asynFetch(String url, String bucket, String key, String md5, String etag,
                              String callbackurl, String callbackbody, String callbackbodytype,
                              String callbackhost, int fileType) throws QiniuException {
        StringMap params = new StringMap()
                .putNotNull("key", key)
                .putNotNull("md5", md5)
                .putNotNull("etag", etag)
                .putNotNull("callbackurl", callbackurl)
                .putNotNull("callbackbody", callbackbody)
                .putNotNull("callbackbodytype", callbackbodytype)
                .putNotNull("callbackhost", callbackhost)
                .putNotNull("file_type", fileType);
        return asyncFetch(url, bucket, params);
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * 主要对于大文件进行抓取
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     *
     * @param url    待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket 文件抓取后保存的空间
     * @param params 其他参数
     * @return Response
     * @throws QiniuException 异常
     */
    public Response asyncFetch(String url, String bucket, StringMap params) throws QiniuException {
        if (params == null) params = new StringMap();
        params.put("url", url).put("bucket", bucket);
        String requestUrl = configHelper.apiHost(auth.accessKey, bucket) + "/sisyphus/fetch";
        byte[] bodyByte = Json.encode(params).getBytes(Constants.UTF_8);
        return client.post(requestUrl, bodyByte,
                auth.authorizationV2(requestUrl, "POST", bodyByte, "application/json"), Client.JsonMime);
    }

    /**
     * 查询异步抓取任务
     *
     * @param region      抓取任务所在bucket区域 华东 z0 华北 z1 华南 z2 北美 na0 东南亚 as0
     * @param fetchWorkId 抓取任务id
     * @return Response
     * @throws QiniuException 异常
     */
    public Response checkAsynFetchid(String region, String fetchWorkId) throws QiniuException {
        String path = String.format("http://api-%s.qiniu.com/sisyphus/fetch?id=%s", region, fetchWorkId);
        return client.get(path, auth.authorizationV2(path));
    }

    /**
     * 对于设置了镜像存储的空间，从镜像源站抓取指定名称的资源并存储到该空间中
     * 如果该空间中已存在该名称的资源，则会将镜像源站的资源覆盖空间中相同名称的资源
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @return Response
     * @throws QiniuException 异常
     */
    public Response prefetch(String bucket, String key) throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/prefetch/%s", resource);
        Response res = ioPost(bucket, path);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置空间的镜像源站
     *
     * @param bucket     空间名称
     * @param srcSiteUrl 镜像回源地址
     * @return Response
     * @throws QiniuException 异常
     */
    @Deprecated
    public Response setImage(String bucket, String srcSiteUrl) throws QiniuException {
        return setImage(bucket, srcSiteUrl, null);
    }

    /**
     * 设置空间的镜像源站
     *
     * @param bucket     空间名称
     * @param srcSiteUrl 镜像回源地址
     * @param host       镜像回源Host
     * @return Response
     * @throws QiniuException 异常
     */
    @Deprecated
    public Response setImage(String bucket, String srcSiteUrl, String host) throws QiniuException {
        String encodedSiteUrl = UrlSafeBase64.encodeToString(srcSiteUrl);
        String encodedHost = null;
        if (host != null && host.length() > 0) {
            encodedHost = UrlSafeBase64.encodeToString(host);
        }
        String path = String.format("/image/%s/from/%s", bucket, encodedSiteUrl);
        if (encodedHost != null) {
            path += String.format("/host/%s", encodedHost);
        }
        path = String.format("%s%s", configHelper.ucHost(), path);
        return post(path, null, ucInterceptors());
    }

    /**
     * 取消空间的镜像源站设置
     *
     * @param bucket 空间名称
     * @return Response
     * @throws QiniuException 异常
     */
    @Deprecated
    public Response unsetImage(String bucket) throws QiniuException {
        String path = String.format("%s/unimage/%s", configHelper.ucHost(), bucket);
        return post(path, null, ucInterceptors());
    }

    /**
     * 设置文件的存活时间
     *
     * @param bucket 空间名称
     * @param key    文件名称
     * @param days   存活时间，单位：天
     * @return Response
     * @throws QiniuException 异常
     */
    public Response deleteAfterDays(String bucket, String key, int days) throws QiniuException {
        return rsPost(bucket, String.format("/deleteAfterDays/%s/%d", encodedEntry(bucket, key), days), null);
    }

    /**
     * 设置 Bucket noIndexPage 属性<br>
     *
     * @param bucket 空间名
     * @param type   type 为 0 表示启用 indexPage，为 1 表示不启用indexPage
     * @return Response
     * @throws QiniuException 异常
     */
    public Response setIndexPage(String bucket, IndexPageType type) throws QiniuException {
        String url = String.format("%s/noIndexPage?bucket=%s&noIndexPage=%s",
                configHelper.ucHost(), bucket, type.getType());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 查询空间信息
     *
     * @param bucket 空间名
     * @return bucket 信息
     * @throws QiniuException 异常
     */
    public BucketInfo getBucketInfo(String bucket) throws QiniuException {
        Response res = getBucketInfoResponse(bucket);
        BucketInfo info = res.jsonToObject(BucketInfo.class);
        res.close();
        return info;
    }

    public Response getBucketInfoResponse(String bucket) throws QiniuException {
        String url = String.format("%s/v2/bucketInfo?bucket=%s", configHelper.ucHost(), bucket);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
        return res;
    }

    /**
     * 设置空间 referer 防盗链
     *
     * @param bucket    空间名
     * @param antiLeech 空间 referer 防盗链信息
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putReferAntiLeech(String bucket, BucketReferAntiLeech antiLeech) throws QiniuException {
        String url = String.format("%s/referAntiLeech?bucket=%s&%s",
                configHelper.ucHost(), bucket, antiLeech.asQueryString());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置存储空间内文件的生命周期规则
     *
     * @param bucket 空间名
     * @param rule   生命周期规则
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketLifecycleRule(String bucket, BucketLifeCycleRule rule) throws QiniuException {
        String url = String.format("%s/rules/add?bucket=%s&%s", configHelper.ucHost(), bucket, rule.asQueryString());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 删除特定存储空间上设定的规则
     *
     * @param bucket   空间名
     * @param ruleName 规则名
     * @return Response
     * @throws QiniuException 异常
     */
    public Response deleteBucketLifecycleRule(String bucket, String ruleName) throws QiniuException {
        String url = String.format("%s/rules/delete?bucket=%s&name=%s", configHelper.ucHost(), bucket, ruleName);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 更新特定存储空间上的生命周期规则
     *
     * @param bucket 空间名
     * @param rule   生命周期规则
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateBucketLifeCycleRule(String bucket, BucketLifeCycleRule rule) throws QiniuException {
        String url = String.format("%s/rules/update?bucket=%s&%s",
                configHelper.ucHost(), bucket, rule.asQueryString());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取指定空间上设置的生命周期规则
     *
     * @param bucket 空间名
     * @return 生命周期规则
     * @throws QiniuException 异常
     */
    public BucketLifeCycleRule[] getBucketLifeCycleRule(String bucket) throws QiniuException {
        Response res = getBucketLifeCycleRuleResponse(bucket);
        BucketLifeCycleRule[] rules;
        JsonElement element = Json.decode(res.bodyString(), JsonElement.class);
        if (element instanceof JsonNull) {
            rules = new BucketLifeCycleRule[0];
        } else {
            JsonArray array = (JsonArray) element;
            rules = new BucketLifeCycleRule[array.size()];
            for (int i = 0; i < array.size(); i++) {
                rules[i] = Json.decode(array.get(i), BucketLifeCycleRule.class);
            }
        }
        res.close();
        return rules;
    }

    public Response getBucketLifeCycleRuleResponse(String bucket) throws QiniuException {
        String url = String.format("%s/rules/get?bucket=%s", configHelper.ucHost(), bucket);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 增加事件通知规则
     *
     * @param bucket 空间名
     * @param rule   通知规则
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketEvent(String bucket, BucketEventRule rule) throws QiniuException {
        String url = String.format("%s/events/add?bucket=%s&%s", configHelper.ucHost(), bucket, rule.asQueryString());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 删除事件通知规则
     *
     * @param bucket   空间名
     * @param ruleName 规则名
     * @return Response
     * @throws QiniuException 异常
     */
    public Response deleteBucketEvent(String bucket, String ruleName) throws QiniuException {
        String url = String.format("%s/events/delete?bucket=%s&name=%s", configHelper.ucHost(), bucket, ruleName);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 更新事件通知规则
     *
     * @param bucket 空间名
     * @param rule   通知规则
     * @return Response
     * @throws QiniuException 异常
     */
    public Response updateBucketEvent(String bucket, BucketEventRule rule) throws QiniuException {
        String url = String.format("%s/events/update?bucket=%s&%s",
                configHelper.ucHost(), bucket, rule.asQueryString());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取事件通知规则
     *
     * @param bucket 空间名
     * @return 事件通知
     * @throws QiniuException 异常
     */
    public BucketEventRule[] getBucketEvents(String bucket) throws QiniuException {
        Response res = getBucketEventsResponse(bucket);
        BucketEventRule[] rules;
        JsonElement element = Json.decode(res.bodyString(), JsonElement.class);
        if (element instanceof JsonNull) {
            rules = new BucketEventRule[0];
        } else {
            JsonArray array = (JsonArray) element;
            rules = new BucketEventRule[array.size()];
            for (int i = 0; i < array.size(); i++) {
                rules[i] = Json.decode(array.get(i), BucketEventRule.class);
            }
        }
        res.close();
        return rules;
    }

    public Response getBucketEventsResponse(String bucket) throws QiniuException {
        String url = String.format("%s/events/get?bucket=%s", configHelper.ucHost(), bucket);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置 bucket 的 cors（跨域）规则
     *
     * @param bucket 空间名
     * @param rules  跨域）规则
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putCorsRules(String bucket, CorsRule[] rules) throws QiniuException {
        String url = String.format("%s/corsRules/set/%s", configHelper.ucHost(), bucket);
        Response res = post(url, Json.encode(rules).getBytes(Constants.UTF_8), ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取 bucket的cors（跨域）规则
     *
     * @param bucket 空间名
     * @return 跨域规则
     * @throws QiniuException 异常
     */
    public CorsRule[] getCorsRules(String bucket) throws QiniuException {
        Response res = getCorsRulesResponse(bucket);
        CorsRule[] rules;
        JsonElement element = Json.decode(res.bodyString(), JsonElement.class);
        if (element instanceof JsonNull) {
            rules = new CorsRule[0];
        } else {
            JsonArray array = (JsonArray) element;
            rules = new CorsRule[array.size()];
            for (int i = 0; i < array.size(); i++) {
                rules[i] = Json.decode(array.get(i), CorsRule.class);
            }
        }
        res.close();
        return rules;
    }

    public Response getCorsRulesResponse(String bucket) throws QiniuException {
        String url = String.format("%s/corsRules/get/%s", configHelper.ucHost(), bucket);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置原图保护模式
     *
     * @param bucket 空间名
     * @param mode   原图保护模式，1表示开启原图保护，0表示关闭，默认为0
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketAccessStyleMode(String bucket, AccessStyleMode mode) throws QiniuException {
        String url = String.format("%s/accessMode/%s/mode/%d", configHelper.ucHost(), bucket, mode.getType());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置 Bucket 的cache-control: max-age 属性<br>
     *
     * @param bucket 空间名
     * @param maxAge max-age，为 0 或者负数表示为默认值（31536000）
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketMaxAge(String bucket, long maxAge) throws QiniuException {
        String url = String.format("%s/maxAge?bucket=%s&maxAge=%d", configHelper.ucHost(), bucket, maxAge);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置bucket私有属性，0公有空间，1私有空间
     *
     * @param bucket 空间名
     * @param acl    私有属性
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketAccessMode(String bucket, AclType acl) throws QiniuException {
        String url = String.format("%s/private?bucket=%s&private=%s", configHelper.ucHost(), bucket, acl.getType());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 设置 bucket 私有属性，0: 公有空间，1: 私有空间<br>
     * 推荐使用putBucketAccessMode
     *
     * @param bucket 空间名
     * @param acl    私有属性
     * @return Response
     * @throws QiniuException 异常
     */
    @Deprecated
    public Response setBucketAcl(String bucket, AclType acl) throws QiniuException {
        return putBucketAccessMode(bucket, acl);
    }

    /**
     * 设置空间配额，配置解释请参考BucketQuota
     *
     * @param bucket      空间名
     * @param bucketQuota 空间配额
     * @return Response
     * @throws QiniuException 异常
     */
    public Response putBucketQuota(String bucket, BucketQuota bucketQuota) throws QiniuException {
        String url = String.format("%s/setbucketquota/%s/size/%d/count/%d",
                configHelper.ucHost(), bucket, bucketQuota.getSize(), bucketQuota.getCount());
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取空间配额
     *
     * @param bucket 空间名
     * @return BucketQuota
     * @throws QiniuException 异常
     */
    public BucketQuota getBucketQuota(String bucket) throws QiniuException {
        Response res = getBucketQuotaResponse(bucket);
        BucketQuota bucketQuota = res.jsonToObject(BucketQuota.class);
        res.close();
        return bucketQuota;
    }

    public Response getBucketQuotaResponse(String bucket) throws QiniuException {
        String url = String.format("%s/getbucketquota/%s", configHelper.ucHost(), bucket);
        Response res = post(url, null, ucInterceptors());
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    /**
     * 获取 Bucket 的默认源站域名
     *
     * @param bucket 空间名
     * @return 源站域名
     * @throws QiniuException 异常
     */
    public String getDefaultIoSrcHost(String bucket) throws QiniuException {
        return configHelper.ioSrcHost(auth.accessKey, bucket);
    }

    /*
     * 相关请求的方法列表
     * */
    private Response rsPost(String bucket, String path, byte[] body) throws QiniuException {
        check(bucket);
        String url = configHelper.rsHost(auth.accessKey, bucket) + path;
        return post(url, body);
    }

    private Response rsGet(String bucket, String path) throws QiniuException {
        check(bucket);
        String url = configHelper.rsHost(auth.accessKey, bucket) + path;
        return get(url);
    }

    private Response ioPost(String bucket, String path) throws QiniuException {
        check(bucket);
        String url = configHelper.ioHost(auth.accessKey, bucket) + path;
        return post(url, null);
    }

    private Response get(String url) throws QiniuException {
        StringMap headers = auth.authorizationV2(url, "GET", null, null);
        return client.get(url, headers);
    }

    private Response get(String url, Api.Interceptor[] interceptors) throws QiniuException {
        Api.Request request = new Api.Request(url);
        request.setAuthType(Api.Request.AuthTypeQiniu);
        return new Api(client, interceptors).requestWithInterceptor(request);
    }

    private Response post(String url, byte[] body) throws QiniuException {
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.FormMime);
        return client.post(url, body, headers, Client.FormMime);
    }

    private Response post(String url, byte[] body, Api.Interceptor[] interceptors) throws QiniuException {
        Api.Request request = new Api.Request(url);
        request.setAuthType(Api.Request.AuthTypeQiniu);
        request.setMethod(MethodType.POST);
        if (body == null) {
            body = new byte[0];
        }
        request.setBody(body, 0, body.length, Client.FormMime);

        return new Api(client, interceptors).requestWithInterceptor(request);
    }

    private void check(String bucket) throws QiniuException {
        if (StringUtils.isNullOrEmpty(bucket)) {
            throw new QiniuException(Response.createError(null, null, 0, "未指定操作的空间或操作体为空"));
        }
    }

    private Api.Interceptor[] ucInterceptors() throws QiniuException {
        String[] ucHosts = null;
        List<String> ucHostList = configHelper.ucHostsWithoutScheme();
        if (ucHostList != null) {
            ucHostList.remove(Configuration.defaultApiHost);
            ucHosts = ucHostList.toArray(new String[0]);
        }
        Api.Interceptor authInterceptor = new ApiInterceptorAuth.Builder()
                .setAuth(auth)
                .build();
        Api.Interceptor hostRetryInterceptor = new ApiInterceptorRetryHosts.Builder()
                .setRetryMax(config.retryMax)
                .setRetryInterval(Retry.staticInterval(config.retryInterval))
                .setHostProvider(HostProvider.arrayProvider(ucHosts))
                .setHostFreezeDuration(config.hostFreezeDuration)
                .build();
        return new Api.Interceptor[]{authInterceptor, hostRetryInterceptor};
    }

    /**
     * 批量文件管理请求
     * <p>
     * 如果遇到超时比较多，可减小单次 batch 操作的数量，或者在创建 BucketManager 时尝试增加超时时间；
     * 增加超时时间的具体方式如下：
     * Configuration cfg = new Configuration();
     * cfg.readTimeout = 120;
     * BucketManager bucketManager = new BucketManager(auth, cfg);
     * <p>
     * 如果 BucketManager 定义了 Client ，可以指定 Client 的超时时间。
     *
     * @param operations batch 操作信息
     * @return Response
     * @throws QiniuException 异常
     */
    public Response batch(BatchOperations operations) throws QiniuException {
        return rsPost(operations.execBucket(), "/batch", operations.toBody());
    }

    /**
     * 文件管理批量操作指令构建对象，单次 BatchOperations 的操作数最多为 1000（即 add 最多 1000 个），如果遇到超时，需要调小操作数量
     */
    public static class BatchOperations {
        private ArrayList<String> ops;
        private String execBucket = null;

        public BatchOperations() {
            this.ops = new ArrayList<String>();
        }

        /**
         * 添加chgm指令
         *
         * @param bucket      空间名
         * @param key         文件的 key
         * @param newMimeType 修改后的 MimeType
         * @return BatchOperations
         */
        public BatchOperations addChgmOp(String bucket, String key, String newMimeType) {
            return addChgmOp(bucket, key, newMimeType, null, null);
        }

        /**
         * 添加 chgm 指令
         * <a href="http://developer.qiniu.com/kodo/api/chgm"> 相关链接 </a>
         * newMimeType 和 metaData 必须有其一
         *
         * @param bucket      空间名
         * @param key         文件的 key
         * @param newMimeType 修改后的 MimeType [可选]
         * @param metas       需要修改的 metas，只包含需要更改的 metas，可增加 [可选]
         *                    服务接口中 key 必须包含 x-qn-meta- 前缀，SDK 会对 metas 中的 key 进行检测
         *                    - key 如果包含了 x-qn-meta- 前缀，则直接使用 key
         *                    - key 如果不包含了 x-qn-meta- 前缀，则内部会为 key 拼接 x-qn-meta- 前缀
         * @param condition   自定义条件信息；只有条件匹配才会执行修改操作 [可选]
         * @return BatchOperations
         */
        public BatchOperations addChgmOp(String bucket, String key, String newMimeType, Map<String, String> metas, Condition condition) {
            StringBuilder builder = new StringBuilder()
                    .append("/chgm/").append(encodedEntry(bucket, key));
            if (newMimeType != null && !newMimeType.isEmpty()) {
                builder.append("/mime/").append(UrlSafeBase64.encodeToString(newMimeType));
            }

            if (metas != null) {
                for (String k : metas.keySet()) {
                    if (k.startsWith("x-qn-meta-")) {
                        builder.append("/").append(k);
                    } else {
                        builder.append("/x-qn-meta-").append(k);
                    }
                    builder.append("/").append(UrlSafeBase64.encodeToString(metas.get(k)));
                }
            }

            if (condition != null && condition.encodedString() != null) {
                builder.append("/cond/").append(condition.encodedString());
            }
            ops.add(builder.toString());
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加 copy 指令
         * 如果目标文件名已被占用，则返回错误码 614，且不做任何覆盖操作；
         *
         * @param fromBucket  源空间名
         * @param fromFileKey 源文件的 key
         * @param toBucket    目标空间名
         * @param toFileKey   目标文件的 key
         * @return BatchOperations
         */
        public BatchOperations addCopyOp(String fromBucket, String fromFileKey, String toBucket, String toFileKey) {
            String from = encodedEntry(fromBucket, fromFileKey);
            String to = encodedEntry(toBucket, toFileKey);
            ops.add(String.format("copy/%s/%s", from, to));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加 copy 指令
         *
         * @param fromBucket  源空间名
         * @param fromFileKey 源文件的 key
         * @param toBucket    目标空间名
         * @param toFileKey   目标文件的 key
         * @param force       当目标文件已存在时，是否木盖目标文件
         *                    false: 如果目标文件名已被占用，则返回错误码 614，且不做任何覆盖操作；
         *                    true: 如果目标文件名已被占用，会强制覆盖目标文件
         * @return BatchOperations
         */
        public BatchOperations addCopyOp(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force) {
            String from = encodedEntry(fromBucket, fromFileKey);
            String to = encodedEntry(toBucket, toFileKey);
            ops.add(String.format("copy/%s/%s/force/%s", from, to, force));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加重命名指令
         *
         * @param fromBucket  源空间名
         * @param fromFileKey 源文件的 key
         * @param toFileKey   目标文件的 key
         * @return BatchOperations
         */
        public BatchOperations addRenameOp(String fromBucket, String fromFileKey, String toFileKey) {
            return addMoveOp(fromBucket, fromFileKey, fromBucket, toFileKey);
        }

        /**
         * 添加重命名指令
         *
         * @param fromBucket  源空间名
         * @param fromFileKey 源文件的 key
         * @param toFileKey   目标文件的 key
         * @param force       当目标文件已存在时，是否木盖目标文件
         *                    false: 如果目标文件名已被占用，则返回错误码 614，且不做任何覆盖操作；
         *                    true: 如果目标文件名已被占用，会强制覆盖目标文件
         * @return BatchOperations
         */
        public BatchOperations addRenameOp(String fromBucket, String fromFileKey, String toFileKey, boolean force) {
            return addMoveOp(fromBucket, fromFileKey, fromBucket, toFileKey, force);
        }

        /**
         * 添加move指令
         *
         * @param fromBucket 源空间名
         * @param fromKey    源文件的 keys
         * @param toBucket   目标空间名
         * @param toKey      目标文件的 keys
         * @return BatchOperations
         */
        public BatchOperations addMoveOp(String fromBucket, String fromKey, String toBucket, String toKey) {
            String from = encodedEntry(fromBucket, fromKey);
            String to = encodedEntry(toBucket, toKey);
            ops.add(String.format("move/%s/%s", from, to));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加move指令
         *
         * @param fromBucket 源空间名
         * @param fromKey    源文件的 keys
         * @param toBucket   目标空间名
         * @param toKey      目标文件的 keys
         * @param force      当目标文件已存在时，是否木盖目标文件
         *                   false: 如果目标文件名已被占用，则返回错误码 614，且不做任何覆盖操作；
         *                   true: 如果目标文件名已被占用，会强制覆盖目标文件
         * @return BatchOperations
         */
        public BatchOperations addMoveOp(String fromBucket, String fromKey, String toBucket, String toKey, boolean force) {
            String from = encodedEntry(fromBucket, fromKey);
            String to = encodedEntry(toBucket, toKey);
            ops.add(String.format("move/%s/%s/force/%s", from, to, force));
            setExecBucket(fromBucket);
            return this;
        }

        /**
         * 添加delete指令
         *
         * @param bucket 空间名
         * @param keys   文件的 keys
         * @return BatchOperations
         */
        public BatchOperations addDeleteOp(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("delete/%s", encodedEntry(bucket, key)));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加stat指令
         *
         * @param bucket 空间名
         * @param keys   文件的 keys
         * @return BatchOperations
         */
        public BatchOperations addStatOps(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("stat/%s", encodedEntry(bucket, key)));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加changeType指令
         *
         * @param bucket keys 所在 bucket
         * @param type   存储类型
         * @param keys   keys
         * @return BatchOperations
         */
        public BatchOperations addChangeTypeOps(String bucket, StorageType type, String... keys) {
            for (String key : keys) {
                ops.add(String.format("chtype/%s/type/%d", encodedEntry(bucket, key), type.ordinal()));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加 changeStatus 指令
         *
         * @param bucket keys 所在 bucket
         * @param status 存储状态
         * @param keys   keys
         * @return BatchOperations
         */
        public BatchOperations addChangeStatusOps(String bucket, int status, String... keys) {
            for (String key : keys) {
                ops.add(String.format("chstatus/%s/status/%d", encodedEntry(bucket, key), status));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加 deleteAfterDays 指令
         *
         * @param bucket keys 所在 bucket
         * @param days   天数
         * @param keys   keys
         * @return BatchOperations
         */
        public BatchOperations addDeleteAfterDaysOps(String bucket, int days, String... keys) {
            for (String key : keys) {
                ops.add(String.format("deleteAfterDays/%s/%d", encodedEntry(bucket, key), days));
            }
            setExecBucket(bucket);
            return this;
        }

        /**
         * 添加解冻归档存储指令
         *
         * @param bucket          keys 所在 bucket
         * @param freezeAfterDays 解冻有效时长，取值范围 1～7
         * @param keys            keys
         * @return BatchOperations
         */
        public BatchOperations addRestoreArchiveOps(String bucket, int freezeAfterDays, String... keys) {
            for (String key : keys) {
                ops.add(String.format("restoreAr/%s/freezeAfterDays/%d", encodedEntry(bucket, key), freezeAfterDays));
            }
            setExecBucket(bucket);
            return this;
        }

        public byte[] toBody() {
            String body = StringUtils.join(ops, "&op=", "op=");
            return StringUtils.utf8Bytes(body);
        }

        public BatchOperations merge(BatchOperations batch) {
            this.ops.addAll(batch.ops);
            setExecBucket(batch.execBucket());
            return this;
        }

        public BatchOperations clearOps() {
            this.ops.clear();
            return this;
        }

        private void setExecBucket(String bucket) {
            if (execBucket == null) {
                execBucket = bucket;
            }
        }

        public String execBucket() {
            return execBucket;
        }

        public int size() {
            return ops.size();
        }
    }

    public static final class Condition {
        private final String hash;
        private final String mime;
        private final Long fSize;
        private final Long putTime;

        private Condition(String hash, String mime, Long fSize, Long putTime) {
            this.hash = hash;
            this.mime = mime;
            this.fSize = fSize;
            this.putTime = putTime;
        }

        String encodedString() {
            StringBuilder builder = new StringBuilder();
            if (hash != null && !hash.isEmpty()) {
                builder.append("hash=" + hash + "&");
            }
            if (mime != null && !mime.isEmpty()) {
                builder.append("mime=" + mime + "&");
            }
            if (fSize != null) {
                builder.append("fsize=" + fSize + "&");
            }
            if (putTime != null) {
                builder.append("putTime=" + putTime + "&");
            }

            String encoded = builder.toString();
            if (encoded.isEmpty()) {
                return null;
            }

            if (encoded.endsWith("&")) {
                encoded = encoded.substring(0, encoded.length() - 1);
            }

            return UrlSafeBase64.encodeToString(encoded);
        }

        public static final class Builder {
            private String hash;
            private String mime;
            private Long fileSize;
            private Long putTime;

            public Builder() {
            }

            public Builder setHash(String hash) {
                this.hash = hash;
                return this;
            }

            public Builder setMime(String mime) {
                this.mime = mime;
                return this;
            }

            public Builder setFileSize(Long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public Builder setPutTime(Long putTime) {
                this.putTime = putTime;
                return this;
            }

            public Condition build() {
                return new Condition(hash, mime, fileSize, putTime);
            }
        }
    }

    /**
     * 创建文件列表迭代器
     */
    public class FileListIterator implements Iterator<FileInfo[]> {
        private String marker = null;
        private final String bucket;
        private final String delimiter;
        private final int limit;
        private final String prefix;

        public FileListIterator(String bucket, String prefix, int limit, String delimiter) {
            if (limit <= 0) {
                throw new IllegalArgumentException("limit must greater than 0");
            }
            if (limit > 1000) {
                throw new IllegalArgumentException("limit must not greater than 1000");
            }
            this.bucket = bucket;
            this.prefix = prefix;
            this.limit = limit;
            this.delimiter = delimiter;
        }


        @Override
        public boolean hasNext() {
            return "".equals(marker);
        }

        @Override
        public FileInfo[] next() {
            try {
                FileListing f = listFiles(bucket, prefix, marker, limit, delimiter);
                this.marker = f.marker == null ? "" : f.marker;
                return f.items;
            } catch (QiniuException e) {
                throw new UncheckedQiniuException(e, e.getMessage());
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

}
