package com.qiniu.storage;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.model.*;
import com.qiniu.util.StringUtils;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class BucketTest extends TestCase {
    private BucketManager bucketManager;
    private BucketManager bucketManagerNa0;
    private BucketManager dummyBucketManager;

    @Override
    protected void setUp() throws Exception {
        //default config
        Configuration cfg = new Configuration();
        //cfg.useHttpsDomains = true;

        this.bucketManager = new BucketManager(TestConfig.testAuth, cfg);

        //na0 config
        Configuration cfgNa0 = new Configuration(Zone.zoneNa0());
        this.bucketManagerNa0 = new BucketManager(TestConfig.testAuth, cfgNa0);

        //dummy config
        this.dummyBucketManager = new BucketManager(TestConfig.dummyAuth, new Configuration());
    }

    @Test
    public void testBuckets() {
        try {
            String[] buckets = bucketManager.buckets();
            assertTrue(StringUtils.inStringArray(TestConfig.testBucket_z0, buckets));
            assertTrue(StringUtils.inStringArray(TestConfig.testBucket_na0, buckets));
        } catch (QiniuException e) {
            fail(e.response.toString());
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
            String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
            for (String bucket : buckets) {
                FileListing l = bucketManager.listFiles(bucket, null, null, 2, null);
                assertNotNull(l.items[0]);
                assertNotNull(l.marker);
            }
        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    public void testListUseDelimiter() {
        try {
            Map<String, String> bucketKeyMap = new HashMap<String, String>();
            //bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
            bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

            for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
                String bucket = entry.getKey();
                String key = entry.getValue();
                bucketManager.copy(bucket, key, bucket, "test/" + key, true);
                bucketManager.copy(bucket, key, bucket, "test/1/" + key, true);
                bucketManager.copy(bucket, key, bucket, "test/2/" + key, true);
                bucketManager.copy(bucket, key, bucket, "test/3/" + key, true);
                FileListing l = bucketManager.listFiles(bucket, "test/", null, 10, "/");
                assertEquals(1, l.items.length);
                assertEquals(3, l.commonPrefixes.length);
            }
        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    public void testListIterator() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            BucketManager.FileListIterator it = bucketManager.createFileListIterator(bucket, "", 20, null);

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
    }

    @Test
    public void testListIteratorWithDefaultLimit() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            BucketManager.FileListIterator it = bucketManager.createFileListIterator(bucket, "");

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
    }

    @Test
    public void testStat() {
        //test exists
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            try {
                FileInfo info = bucketManager.stat(bucket, key);
                assertNotNull(info.hash);
                assertNotNull(info.mimeType);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }

        //test bucket not exits or file not exists
        Map<String[], Integer> entryCodeMap = new HashMap<String[], Integer>();
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_KEY_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.testKey_z0},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);


        for (Map.Entry<String[], Integer> entry : entryCodeMap.entrySet()) {
            String bucket = entry.getKey()[0];
            String key = entry.getKey()[1];
            int code = entry.getValue();
            try {
                bucketManager.stat(bucket, key);
                fail();
            } catch (QiniuException e) {
                assertEquals(code, e.code());
            }
        }
    }

    @Test
    public void testDelete() {
        Map<String[], Integer> entryCodeMap = new HashMap<String[], Integer>();
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_KEY_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, null},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, null},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);

        for (Map.Entry<String[], Integer> entry : entryCodeMap.entrySet()) {
            String bucket = entry.getKey()[0];
            String key = entry.getKey()[1];
            int code = entry.getValue();
            try {
                bucketManager.delete(bucket, key);
                fail();
            } catch (QiniuException e) {
                assertEquals(code, e.code());
            }
        }
    }

    @Test
    public void testRename() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String renameFromKey = "renameFrom" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, renameFromKey);
                String renameToKey = "renameTo" + key;
                bucketManager.rename(bucket, renameFromKey, renameToKey);
                bucketManager.delete(bucket, renameToKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testCopy() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyToKey = "copyTo" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, copyToKey);
                bucketManager.delete(bucket, copyToKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testChangeMime() {
        List<String[]> cases = new ArrayList<String[]>();
        cases.add(new String[]{TestConfig.testBucket_z0, TestConfig.testKey_z0, "image/png"});
        cases.add(new String[]{TestConfig.testBucket_na0, TestConfig.testKey_na0, "image/png"});

        for (String[] icase : cases) {
            String bucket = icase[0];
            String key = icase[1];
            String mime = icase[2];
            try {
                bucketManager.changeMime(bucket, key, mime);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testPrefetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            try {
                bucketManager.setImage(bucket, "http://developer.qiniu.com/");
                bucketManager.prefetch(bucket, "kodo/sdk/java");
                bucketManager.unsetImage(bucket);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testFetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            try {
                String resUrl = "http://devtools.qiniu.com/qiniu.png";
                String resKey = "qiniu.png";
                String resHash = "FpHyF0kkil3sp-SaXXX8TBJY3jDh";
                FetchRet fRet = bucketManager.fetch(resUrl, bucket, resKey);
                assertEquals(resHash, fRet.hash);

                //no key specified, use hash as file key
                fRet = bucketManager.fetch(resUrl, bucket);
                assertEquals(resHash, fRet.hash);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testFetchNa0() {
        try {
            String resUrl = "http://devtools.qiniu.com/qiniu.png";
            String resKey = "qiniu.png";
            String resHash = "FpHyF0kkil3sp-SaXXX8TBJY3jDh";
            FetchRet fRet = bucketManagerNa0.fetch(resUrl, TestConfig.testBucket_na0, resKey);
            assertEquals(resHash, fRet.hash);

            //no key specified, use hash as file key
            fRet = bucketManagerNa0.fetch(resUrl, TestConfig.testBucket_na0);
            assertEquals(resHash, fRet.hash);
        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    @Test
    public void testBatchCopy() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyToKey = "copyTo" + Math.random();
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().
                    addCopyOp(bucket, key, bucket, copyToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertEquals(200, bs[0].code);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            ops = new BucketManager.BatchOperations().addDeleteOp(bucket, copyToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertEquals(200, bs[0].code);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatchMove() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String moveFromKey = "moveFrom" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, moveFromKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            String moveToKey = key + "to";
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addMoveOp(bucket, moveFromKey, bucket, moveToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertEquals(200, bs[0].code);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            try {
                bucketManager.delete(bucket, moveToKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatchRename() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String renameFromKey = "renameFrom" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, renameFromKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            String renameToKey = "renameTo" + key;
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addRenameOp(bucket, renameFromKey, renameToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertEquals(200, bs[0].code);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
            try {
                bucketManager.delete(bucket, renameToKey);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatchStat() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String[] keyArray = new String[100];
            for (int i = 0; i < keyArray.length; i++) {
                keyArray[i] = key;
            }
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addStatOps(bucket, keyArray);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertEquals(200, bs[0].code);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatchCopyChgmDelete() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();

            //make 100 copies
            String[] keyArray = new String[100];
            for (int i = 0; i < keyArray.length; i++) {
                keyArray[i] = String.format("%s-copy-%d", key, i);
            }

            BucketManager.BatchOperations ops = new BucketManager.BatchOperations();
            for (int i = 0; i < keyArray.length; i++) {
                ops.addCopyOp(bucket, key, bucket, keyArray[i]);
            }

            try {
                Response response = bucketManager.batch(ops);
                Assert.assertEquals(200, response.statusCode);

                //clear ops
                ops.clearOps();

                //batch chane mimetype
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addChgmOp(bucket, keyArray[i], "image/png");
                }
                response = bucketManager.batch(ops);
                Assert.assertEquals(200, response.statusCode);

                //clear ops
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addDeleteOp(bucket, keyArray[i]);
                }
                response = bucketManager.batch(ops);
                Assert.assertEquals(200, response.statusCode);

            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatch() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String[] array = {key};
            String copyFromKey = "copyFrom" + Math.random();

            String moveFromKey = "moveFrom" + Math.random();
            String moveToKey = "moveTo" + Math.random();

            String moveFromKey2 = "moveFrom" + Math.random();
            String moveToKey2 = "moveTo" + Math.random();

            try {
                bucketManager.copy(bucket, key, bucket, moveFromKey);
                bucketManager.copy(bucket, key, bucket, moveFromKey2);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }

            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addCopyOp(bucket, key, bucket, copyFromKey)
                    .addMoveOp(bucket, moveFromKey, bucket, moveToKey)
                    .addRenameOp(bucket, moveFromKey2, moveToKey2)
                    .addStatOps(bucket, array)
                    .addStatOps(bucket, array[0]);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                for (BatchStatus b : bs) {
                    assertEquals(200, b.code);
                }
            } catch (QiniuException e) {
                fail(e.response.toString());
            }

            BucketManager.BatchOperations opsDel = new BucketManager.BatchOperations()
                    .addDeleteOp(bucket, copyFromKey, moveFromKey, moveToKey, moveFromKey2, moveToKey2);

            try {
                bucketManager.batch(opsDel);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testSetAndUnsetImage() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            String srcSiteUrl = "http://developer.qiniu.com/";
            String host = "developer.qiniu.com";
            try {
                Response setResp = bucketManager.setImage(bucket, srcSiteUrl);
                assertEquals(200, setResp.statusCode);

                setResp = bucketManager.setImage(bucket, srcSiteUrl, host);
                assertEquals(200, setResp.statusCode);

                Response unsetResp = bucketManager.unsetImage(bucket);
                assertEquals(200, unsetResp.statusCode);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    @Test
    public void testDeleteAfterDays() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String keyForDelete = "keyForDelete" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, keyForDelete);
                Response response = bucketManager.deleteAfterDays(bucket, key, 10);
                Assert.assertEquals(200, response.statusCode);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }
}
