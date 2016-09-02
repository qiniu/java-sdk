package com.qiniu.storage;

import com.qiniu.common.Config;
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
 * @link http://developer.qiniu.com/docs/v6/api/reference/rs/
 */
public final class BucketManager {
    private final Auth auth;
    private final Client client;

    public BucketManager(Auth auth) {
        this.auth = auth;
        client = new Client();
    }

    /**
     * EncodedEntryURI格式
     *
     * @param bucket
     * @param key
     * @return urlsafe_base64_encode(Bucket:Key)
     */
    public static String entry(String bucket, String key) {
        return entry(bucket, key, true);
    }


    /**
     * EncodedEntryURI格式
     * 当 mustHaveKey 为 false， 且 key 为 null 时，返回 urlsafe_base64_encode(Bucket);
     * 其它条件下返回  urlsafe_base64_encode(Bucket:Key)
     *
     * @param bucket
     * @param key
     * @param mustHaveKey
     * @return urlsafe_base64_encode(entry)
     */
    public static String entry(String bucket, String key, boolean mustHaveKey) {
        String en = bucket + ":" + key;
        if (!mustHaveKey && (key == null)) {
            en = bucket;
        }
        return UrlSafeBase64.encodeToString(en);
    }

    /**
     * 获取账号下所有空间名列表
     *
     * @return bucket 列表
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
        return new FileListIterator(bucket, prefix, 100, null);
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

        String url = Config.RSF_HOST + "/list?" + map.formString();
        Response r = get(url);
        return r.jsonToObject(FileListing.class);
    }

    /**
     * 获取指定空间、文件名的状态
     *
     * @param bucket
     * @param key
     * @return
     * @throws QiniuException
     */
    public FileInfo stat(String bucket, String key) throws QiniuException {
        Response r = rsGet("/stat/" + entry(bucket, key));
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
        rsPost("/delete/" + entry(bucket, key));
    }

    /**
     * 修改指定空间、文件的文件名
     *
     * @param bucket
     * @param oldname
     * @param newname
     * @throws QiniuException
     */
    public void rename(String bucket, String oldname, String newname) throws QiniuException {
        move(bucket, oldname, bucket, newname);
    }

    /**
     * 复制文件。要求空间在同一账号下。
     *
     * @param from_bucket
     * @param from_key
     * @param to_bucket
     * @param to_key
     * @throws QiniuException
     */
    public void copy(String from_bucket, String from_key, String to_bucket, String to_key) throws QiniuException {
        String from = entry(from_bucket, from_key);
        String to = entry(to_bucket, to_key);
        String path = "/copy/" + from + "/" + to;
        rsPost(path);
    }

    /**
     * 复制文件。要求空间在同一账号下, 可以添加force参数为true强行复制文件。
     *
     * @param from_bucket
     * @param from_key
     * @param to_bucket
     * @param to_key
     * @param force
     * @throws QiniuException
     */
    public void copy(String from_bucket, String from_key, String to_bucket,
                     String to_key, boolean force) throws QiniuException {
        String from = entry(from_bucket, from_key);
        String to = entry(to_bucket, to_key);
        String path = "/copy/" + from + "/" + to + "/force/" + force;
        rsPost(path);
    }

    /**
     * 移动文件。要求空间在同一账号下。
     *
     * @param from_bucket
     * @param from_key
     * @param to_bucket
     * @param to_key
     * @throws QiniuException
     */
    public void move(String from_bucket, String from_key, String to_bucket, String to_key) throws QiniuException {
        String from = entry(from_bucket, from_key);
        String to = entry(to_bucket, to_key);
        String path = "/move/" + from + "/" + to;
        rsPost(path);
    }

    /**
     * 移动文件。要求空间在同一账号下, 可以添加force参数为true强行移动文件。
     *
     * @param from_bucket
     * @param from_key
     * @param to_bucket
     * @param to_key
     * @param force
     * @throws QiniuException
     */
    public void move(String from_bucket, String from_key, String to_bucket,
                     String to_key, boolean force) throws QiniuException {
        String from = entry(from_bucket, from_key);
        String to = entry(to_bucket, to_key);
        String path = "/move/" + from + "/" + to + "/force/" + force;
        rsPost(path);
    }

    /**
     * 修改完文件mimeTYpe
     *
     * @param bucket
     * @param key
     * @param mime
     * @throws QiniuException
     */
    public void changeMime(String bucket, String key, String mime) throws QiniuException {
        String resource = entry(bucket, key);
        String encode_mime = UrlSafeBase64.encodeToString(mime);
        String path = "/chgm/" + resource + "/mime/" + encode_mime;
        rsPost(path);
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
        String to = entry(bucket, key, false);
        String path = "/fetch/" + resource + "/to/" + to;
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
        String resource = entry(bucket, key);
        String path = "/prefetch/" + resource;
        ioPost(path);
    }

    /**
     * 批量执行文件管理相关操作
     *
     * @param operations
     * @return
     * @throws QiniuException
     * @see Batch
     */
    public Response batch(Batch operations) throws QiniuException {
        return rsPost("/batch", operations.toBody());
    }

    private Response rsPost(String path, byte[] body) throws QiniuException {
        String url = Config.RS_HOST + path;
        return post(url, body);
    }

    private Response rsPost(String path) throws QiniuException {
        return rsPost(path, null);
    }

    private Response rsGet(String path) throws QiniuException {
        String url = Config.RS_HOST + path;
        return get(url);
    }

    private Response ioPost(String path) throws QiniuException {
        String url = Config.IO_HOST + path;
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
    public static class Batch {
        private ArrayList<String> ops;

        public Batch() {
            this.ops = new ArrayList<String>();
        }

        public Batch copy(String from_bucket, String from_key, String to_bucket, String to_key) {
            String from = entry(from_bucket, from_key);
            String to = entry(to_bucket, to_key);
            ops.add("copy" + "/" + from + "/" + to);
            return this;
        }

        public Batch rename(String from_bucket, String from_key, String to_key) {
            return move(from_bucket, from_key, from_bucket, to_key);
        }

        public Batch move(String from_bucket, String from_key, String to_bucket, String to_key) {
            String from = entry(from_bucket, from_key);
            String to = entry(to_bucket, to_key);
            ops.add("move" + "/" + from + "/" + to);
            return this;
        }

        public Batch delete(String bucket, String... keys) {
            for (String key : keys) {
                ops.add("delete" + "/" + entry(bucket, key));
            }
            return this;
        }

        public Batch stat(String bucket, String... keys) {
            for (String key : keys) {
                ops.add("stat" + "/" + entry(bucket, key));
            }
            return this;
        }

        public byte[] toBody() {
            String body = StringUtils.join(ops, "&op=", "op=");
            return StringUtils.utf8Bytes(body);
        }

        public Batch merge(Batch batch) {
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
                throw new IllegalArgumentException("limit must great than 0");
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
