package com.qiniu.storage;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class BucketTest {
    private BucketManager bucketManager = new BucketManager(TestConfig.testAuth);
    private BucketManager dummyBucketManager = new BucketManager(TestConfig.dummyAuth);

    @Test
    public void testBuckets() {
        try {
            String[] buckets = bucketManager.buckets();
            assertTrue(StringUtils.inStringArray(TestConfig.bucket, buckets));
        } catch (QiniuException e) {
            fail(e.getMessage());
        }

        try {
            dummyBucketManager.buckets();
            fail();
        } catch (QiniuException e) {
            assertEquals(401, e.code());
        }
    }

    @Test
    public void testList() {
        try {
            FileListing l = bucketManager.listFiles(TestConfig.bucket, null, null, 2, null);
            assertNotNull(l.items[0]);
            assertNotNull(l.marker);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testListIterator() {
        BucketManager.FileListIterator it = bucketManager
                .createFileListIterator(TestConfig.bucket, "", 20, null);

        assertTrue(it.hasNext());
        FileInfo[] items0 = it.next();
        assertNotNull(items0[0]);

        while (it.hasNext()) {
            FileInfo[] items = it.next();
            if (items.length > 1) {
                assertNotNull(items[0]);
            }
        }
    }

    @Test
    public void testStat() {
        try {
            FileInfo info = bucketManager.stat(TestConfig.bucket, TestConfig.key);
            assertEquals("FmYW4RYti94tr4ncaKzcTJz9M4Y9", info.hash);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }

        try {
            bucketManager.stat(TestConfig.bucket, "noFile");
            fail();
        } catch (QiniuException e) {
            assertEquals(612, e.code());
        }

        try {
            bucketManager.stat(TestConfig.bucket, null);
            fail();
        } catch (QiniuException e) {
            assertEquals(612, e.code());
        }

        try {
            bucketManager.stat("noBucket", "noFile");
            fail();
        } catch (QiniuException e) {
            assertEquals(631, e.code());
        }
    }

    @Test
    public void testDelete() {
        try {
            bucketManager.delete(TestConfig.bucket, "del");
            fail();
        } catch (QiniuException e) {
            assertEquals(612, e.code());
        }

        try {
            bucketManager.delete(TestConfig.bucket, null);
            fail();
        } catch (QiniuException e) {
            assertEquals(612, e.code());
        }

        try {
            bucketManager.delete("noBucket", null);
            fail();
        } catch (QiniuException e) {
            assertEquals(631, e.code());
        }
    }

    @Test
    public void testRename() {
        String key = "renameFrom" + Math.random();
        try {
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key);
            String key2 = "renameTo" + key;
            bucketManager.rename(TestConfig.bucket, key, key2);
            bucketManager.delete(TestConfig.bucket, key2);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCopy() {
        String key = "copyTo" + Math.random();
        try {
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key);
            bucketManager.delete(TestConfig.bucket, key);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testChangeMime() {
        try {
            bucketManager.changeMime(TestConfig.bucket, "java-sdk.html", "text.html");
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPrefetch() {
        try {
            bucketManager.prefetch(TestConfig.bucket, "java-sdk.html");
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testFetch() {
        try {
            bucketManager.fetch("http://developer.qiniu.com/docs/v6/sdk/java-sdk.html",
                    TestConfig.bucket, "fetch.html");
        } catch (QiniuException e) {
            fail();
        }
    }


    @Test
    public void testBatchCopy() {
        String key = "copyTo" + Math.random();
        BucketManager.Batch ops = new BucketManager.Batch().
                copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            assertEquals(200, bs[0].code);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
        ops = new BucketManager.Batch().delete(TestConfig.bucket, key);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            assertEquals(200, bs[0].code);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBatchMove() {
        String key = "moveFrom" + Math.random();
        try {
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
        String key2 = key + "to";
        StringMap x = new StringMap().put(key, key2);
        BucketManager.Batch ops = new BucketManager.Batch().move(TestConfig.bucket,
                key, TestConfig.bucket, key2);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            assertEquals(200, bs[0].code);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
        try {
            bucketManager.delete(TestConfig.bucket, key2);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testBatchRename() {
        String key = "rename" + Math.random();
        try {
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
        String key2 = key + "to";
        BucketManager.Batch ops = new BucketManager.Batch().rename(TestConfig.bucket, key, key2);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            assertEquals(200, bs[0].code);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
        try {
            bucketManager.delete(TestConfig.bucket, key2);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testBatchStat() {
        String[] array = {"java-sdk.html"};
        BucketManager.Batch ops = new BucketManager.Batch().stat(TestConfig.bucket, array);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            assertEquals(200, bs[0].code);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testBatch() {
        String[] array = {"java-sdk.html"};
        String key = "copyFrom" + Math.random();

        String key1 = "moveFrom" + Math.random();
        String key2 = "moveTo" + Math.random();

        String key3 = "moveFrom" + Math.random();
        String key4 = "moveTo" + Math.random();

        try {
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key1);
            bucketManager.copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key3);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }

        BucketManager.Batch ops = new BucketManager.Batch()
                .copy(TestConfig.bucket, TestConfig.key, TestConfig.bucket, key)
                .move(TestConfig.bucket, key1, TestConfig.bucket, key2)
                .rename(TestConfig.bucket, key3, key4)
                .stat(TestConfig.bucket, array)
                .stat(TestConfig.bucket, array[0]);
        try {
            Response r = bucketManager.batch(ops);
            BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
            for (BatchStatus b : bs) {
                assertEquals(200, b.code);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            fail();
        }
    }
}
