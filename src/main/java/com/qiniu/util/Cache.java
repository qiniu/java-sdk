package com.qiniu.util;

import com.qiniu.common.Constants;
import com.qiniu.storage.persistent.FileRecorder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 包含内存缓存和磁盘缓存
 * 磁盘缓存会被缓存在一个文件中
 */
public class Cache<T> {

    // 缓存被持久化为一个文件，此文件的文件名为 version，version 默认为：v1
    private final String version;

    // 存储对象的类型
    private final Class<T> objectClass;

    // 内部
    private boolean isFlushing = false;

    private final ConcurrentHashMap<String, T> memCache = new ConcurrentHashMap<>();
    private final FileRecorder diskCache;

    private Cache(Class<T> objectClass, String cacheDir, String version) {
        this.objectClass = objectClass;
        this.version = version;

        FileRecorder fileRecorder = null;
        try {
            if (objectClass != null && cacheDir != null && !cacheDir.isEmpty()) {
                fileRecorder = new FileRecorder(cacheDir + "/" + objectClass.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.diskCache = fileRecorder;

        this.load();
    }

    private void load() {
        if (this.diskCache == null || objectClass == null) {
            return;
        }

        byte[] cacheData = this.diskCache.get(this.version);
        if (cacheData == null || cacheData.length == 0) {
            return;
        }

        try {
            HashMap<String, Object> cacheJson = Json.decode(new String(cacheData), HashMap.class);
            for (String key : cacheJson.keySet()) {
                try {
                    Object jsonMap = cacheJson.get(key);
                    String jsonString = Json.encode(jsonMap);
                    T object = Json.decode(jsonString, this.objectClass);
                    this.memCache.put(key, object);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.diskCache.del(this.version);
        }
    }

    public T cacheForKey(String cacheKey) {
        return this.memCache.get(cacheKey);
    }

    public void cache(String cacheKey, T object) {
        if (StringUtils.isNullOrEmpty(cacheKey) || object == null) {
            return;
        }

        synchronized (this) {
            this.memCache.put(cacheKey, object);
        }

        this.flush();
    }

    private void flush() {
        if (this.diskCache == null) {
            return;
        }

        Map<String, T> flushCache = null;
        synchronized (this) {
            if (this.isFlushing) {
                return;
            }

            this.isFlushing = true;
            flushCache = new HashMap<>(this.memCache);
        }

        if (flushCache.isEmpty()) {
            return;
        }

        String jsonString = Json.encode(flushCache);
        if (jsonString == null || jsonString.isEmpty()) {
            return;
        }

        byte[] cacheData = jsonString.getBytes();
        if (cacheData.length == 0) {
            return;
        }

        this.diskCache.set(this.version, cacheData);

        synchronized (this) {
            isFlushing = false;
        }
    }

    public void clearMemoryCache() {
        this.memCache.clear();
    }

    public static class Builder<T> {

        // 缓存被持久化为一个文件，此文件的文件名为 version，version 默认为：v1
        private String version = "v1";

        // 存储路径
        private String cacheDir = Constants.CACHE_DIR;

        // 存储对象的类型
        private final Class<T> objectClass;

        public Builder(Class<T> objectClass) {
            this.objectClass = objectClass;
        }

        public Builder<T> setCacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder<T> setVersion(String version) {
            this.version = version;
            return this;
        }

        public Cache<T> builder() {
            return new Cache<>(this.objectClass, cacheDir, this.version);
        }
    }
}
