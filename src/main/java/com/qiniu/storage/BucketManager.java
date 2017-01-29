package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 主要涉及了空间资源管理及批量操作接口的实现，具体的接口规格可以参考
 *
 * @link http://developer.qiniu.com/kodo/api/rs
 */
public final class BucketManager {
    /**
     * Auth 对象
     * 该类需要使用QBox鉴权，所以需要指定Auth对象
     */
    private final Auth auth;

    /**
     * Configuration 对象
     * 该类相关的域名配置，解析配置，HTTP请求超时时间设置等
     */

    private final Configuration configuration;

    /**
     * HTTP Client 对象
     * 该类需要通过该对象来发送HTTP请求
     */
    private final Client client;

    /**
     * 构建一个新的 BucketManager 对象
     *
     * @param auth Auth对象
     * @param cfg  Configuration对象
     */
    public BucketManager(Auth auth, Configuration cfg) {
        this.auth = auth;
        this.configuration = cfg.clone();
        client = new Client(cfg.dns, cfg.dnsHostFirst, cfg.proxy,
                cfg.connectTimeout, cfg.responseTimeout, cfg.writeTimeout);
    }

    /**
     * EncodedEntryURI格式，其中 bucket+":"+key 称之为 entry
     *
     * @param bucket
     * @param key
     * @return UrlSafeBase64.encodeToString(entry)
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
     */
    public static String encodedEntry(String bucket) {
        return encodedEntry(bucket, null);
    }


    /**
     * 获取账号下所有空间名称列表
     *
     * @return 空间名称列表
     */
    public String[] buckets() throws QiniuException {
        Response r = rsGet("/buckets");
        return r.jsonToObject(String[].class);
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

    /**
     * 根据前缀获取文件列表
     *
     * @param bucket    空间名
     * @param prefix    文件名前缀
     * @param marker    上一次获取文件列表时返回的 marker
     * @param limit     每次迭代的长度限制，最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return
     * @throws QiniuException
     */
    public FileListing listFiles(String bucket, String prefix, String marker, int limit, String delimiter)
            throws QiniuException {
        StringMap map = new StringMap().put("bucket", bucket).putNotEmpty("marker", marker)
                .putNotEmpty("prefix", prefix).putNotEmpty("delimiter", delimiter).putWhen("limit", limit, limit > 0);

        String url = configuration.zone.rsfHost(auth.accessKey, bucket) + "/list?" + map.formString();
        Response r = get(url);
        return r.jsonToObject(FileListing.class);
    }

    /**
     * 获取空间中文件的属性
     *
     * @param bucket  空间名称
     * @param fileKey 文件名称
     * @return 文件属性
     * @throws QiniuException
     */
    public FileInfo stat(String bucket, String fileKey) throws QiniuException {
        Response r = rsGet(String.format("/stat/%s", encodedEntry(bucket, fileKey)));
        return r.jsonToObject(FileInfo.class);
    }

    /**
     * 删除指定空间、文件名的文件
     *
     * @param bucket
     * @param key
     * @throws QiniuException
     */
    public void delete(String bucket, String key) throws QiniuException {
        rsPost(String.format("/delete/%s", encodedEntry(bucket, key)));
    }

    /**
     * 修改完文件mimeTYpe
     *
     * @param bucket
     * @param key
     * @param mime
     * @throws QiniuException
     */
    public void changeMime(String bucket, String key, String mime)
            throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String encode_mime = UrlSafeBase64.encodeToString(mime);
        String path = "/chgm/" + resource + "/mime/" + encode_mime;
        rsPost(path);
    }

    /**
     * 重命名空间中的文件
     *
     * @param bucket
     * @param oldFileKey
     * @param newFileKey
     * @throws QiniuException
     */
    public void rename(String bucket, String oldFileKey, String newFileKey)
            throws QiniuException {
        move(bucket, oldFileKey, bucket, newFileKey);
    }


    /**
     * 重命名空间中的文件
     *
     * @param bucket
     * @param oldFileKey
     * @param newFileKey
     * @throws QiniuException
     */
    public void rename(String bucket, String oldFileKey, String newFileKey, boolean force)
            throws QiniuException {
        move(bucket, oldFileKey, bucket, newFileKey, force);
    }

    /**
     * 复制文件。要求空间在同一账号下。
     *
     * @param fromBucket
     * @param fromFileKey
     * @param toBucket
     * @param toFileKey
     * @throws QiniuException
     */
    public void copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey)
            throws QiniuException {
        copy(fromBucket, fromFileKey, toBucket, toFileKey, false);
    }

    /**
     * 复制文件。要求空间在同一账号下, 可以添加force参数为true强行复制文件。
     *
     * @param fromBucket
     * @param fromFileKey
     * @param toBucket
     * @param toFileKey
     * @param force
     * @throws QiniuException
     */
    public void copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force)
            throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/copy/%s/%s/force/%s", from, to, force);
        rsPost(path);
    }

    /**
     * 移动文件。要求空间在同一账号下。
     *
     * @param fromBucket
     * @param fromFileKey
     * @param toBucket
     * @param toFileKey
     * @param force
     * @throws QiniuException
     */
    public void move(String fromBucket, String fromFileKey, String toBucket,
                     String toFileKey, boolean force) throws QiniuException {
        String from = encodedEntry(fromBucket, fromFileKey);
        String to = encodedEntry(toBucket, toFileKey);
        String path = String.format("/move/%s/%s/force/%s", from, to, force);
        rsPost(path);
    }

    /**
     * 移动文件。要求空间在同一账号下, 可以添加force参数为true强行移动文件。
     *
     * @param fromBucket
     * @param fromFileKey
     * @param toBucket
     * @param toFileKey
     * @throws QiniuException
     */
    public void move(String fromBucket, String fromFileKey, String toBucket, String toFileKey) throws QiniuException {
        move(fromBucket, fromFileKey, toBucket, toFileKey, false);
    }


    /**
     * 抓取指定地址的文件，已指定名称保存在指定空间。
     * 要求指定url可访问。
     * 大文件不建议使用此接口抓取。可先下载再上传。
     *
     * @param url
     * @param bucket
     * @throws QiniuException
     */
    public DefaultPutRet fetch(String url, String bucket) throws QiniuException {
        return fetch(url, bucket, null);
    }

    /**
     * 抓取指定地址的文件，已指定名称保存在指定空间。
     * 要求指定url可访问。
     * 大文件不建议使用此接口抓取。可先下载再上传。
     *
     * @param url
     * @param bucket
     * @param key
     * @throws QiniuException
     */
    public DefaultPutRet fetch(String url, String bucket, String key) throws QiniuException {
        String resource = UrlSafeBase64.encodeToString(url);
        String to = encodedEntry(bucket, key);
        String path = String.format("/fetch/%s/to/%s", resource, to);
        Response r = ioPost(path);
        return r.jsonToObject(DefaultPutRet.class);
    }

    /**
     * 对于设置了镜像存储的空间，从镜像源站抓取指定名称的资源并存储到该空间中。
     * 如果该空间中已存在该名称的资源，则会将镜像源站的资源覆盖空间中相同名称的资源
     *
     * @param bucket
     * @param key
     * @throws QiniuException
     */
    public void prefetch(String bucket, String key) throws QiniuException {
        String resource = encodedEntry(bucket, key);
        String path = String.format("/prefetch/%s", resource);
        ioPost(path);
    }

    /**
     * 批量执行文件管理相关操作
     *
     * @param operations
     * @return
     * @throws QiniuException
     * @see BatchOperations
     */
    public Response batch(BatchOperations operations) throws QiniuException {
        return rsPost("/batch", operations.toBody());
    }

    private Response rsPost(String path, byte[] body) throws QiniuException {
        String url = configuration.zone.rsHost() + path;
        return post(url, body);
    }

    private Response rsPost(String path) throws QiniuException {
        return rsPost(path, null);
    }

    private Response rsGet(String path) throws QiniuException {
        String url = configuration.zone.rsHost() + path;
        return get(url);
    }

    private Response ioPost(String path) throws QiniuException {
        String url = configuration.zone.ioHost() + path;
        return post(url, null);
    }

    private Response get(String url) throws QiniuException {
        StringMap headers = auth.authorization(url);
        return client.get(url, headers);
    }

    private Response post(String url, byte[] body) throws QiniuException {
        StringMap headers = auth.authorization(url, body, Client.FormMime);
        return client.post(url, body, headers, Client.FormMime);
    }

    /**
     * 文件管理操作指令
     */
    public static class BatchOperations {
        private ArrayList<String> ops;

        public BatchOperations() {
            this.ops = new ArrayList<String>();
        }

        public BatchOperations addCopyOp(String fromBucket, String fromFileKey, String toBucket, String toFileKey) {
            String from = encodedEntry(fromBucket, fromFileKey);
            String to = encodedEntry(toBucket, toFileKey);
            ops.add(String.format("copy/%s/%s", from, to));
            return this;
        }


        public BatchOperations addRenameOp(String fromBucket, String fromFileKey, String toFileKey) {
            return addMoveOp(fromBucket, fromFileKey, fromBucket, toFileKey);
        }

        public BatchOperations addMoveOp(String fromBucket, String fromKey, String toBucket, String toKey) {
            String from = encodedEntry(fromBucket, fromKey);
            String to = encodedEntry(toBucket, toKey);
            ops.add(String.format("move/%s/%s", from, to));
            return this;
        }

        public BatchOperations addDeleteOp(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("delete/%s", encodedEntry(bucket, key)));
            }
            return this;
        }

        public BatchOperations addStatOps(String bucket, String... keys) {
            for (String key : keys) {
                ops.add(String.format("stat/%s", encodedEntry(bucket, key)));
            }
            return this;
        }

        public byte[] toBody() {
            String body = StringUtils.join(ops, "&op=", "op=");
            return StringUtils.utf8Bytes(body);
        }

        public BatchOperations merge(BatchOperations batch) {
            this.ops.addAll(batch.ops);
            return this;
        }
    }

    /**
     * 获取文件列表迭代器
     */
    public class FileListIterator implements Iterator<FileInfo[]> {
        private String marker = null;
        private String bucket;
        private String delimiter;
        private int limit;
        private String prefix;
        private QiniuException exception = null;

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

        public QiniuException error() {
            return exception;
        }

        @Override
        public boolean hasNext() {
            return exception == null && !"".equals(marker);
        }

        @Override
        public FileInfo[] next() {
            try {
                FileListing f = listFiles(bucket, prefix, marker, limit, delimiter);
                this.marker = f.marker == null ? "" : f.marker;
                return f.items;
            } catch (QiniuException e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
