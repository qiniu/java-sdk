package test.com.qiniu.util;

import com.qiniu.util.Cache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheTest {

    @Test
    public void testCache() {
        Info info = new Info();
        info.foo = "foo";
        info.bar = 1;

        String key = "info_key";
        Cache cache = new Cache.Builder(Info.class)
                .setVersion("v1")
                .builder();

        cache.cache(key, info);


        // 1. 测试内存缓存
        Info memInfo = (Info) cache.cacheForKey(key);
        assertEquals("foo", memInfo.foo);

        // 2. 测试删除内存缓存
        cache.clearMemoryCache();
        memInfo = (Info) cache.cacheForKey(key);
        assertEquals(null, memInfo);

        // 3. 测试 load
        cache = new Cache.Builder(Info.class)
                .setVersion("v1")
                .builder();
        memInfo = (Info) cache.cacheForKey(key);
        assertEquals("foo", memInfo.foo);
    }

    static class Info {
        String foo;
        int bar;
    }
}
