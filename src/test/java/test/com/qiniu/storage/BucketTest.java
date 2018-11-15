package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.*;
import com.qiniu.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("ConstantConditions")
public class BucketTest {
    private BucketManager bucketManager;
    private BucketManager bucketManagerNa0;
    private BucketManager dummyBucketManager;

    ArrayList<Integer> batchStatusCode = new ArrayList<Integer>() {
        {
            this.add(200);
            this.add(298);
        }
    };

    @Before
    protected void setUp() throws Exception {
        //default config
        Configuration cfg = new Configuration();
        cfg.useHttpsDomains = false;
//        cfg.useHttpsDomains = true;
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
            Assert.assertTrue(StringUtils.inStringArray(TestConfig.testBucket_z0, buckets));
            Assert.assertTrue(StringUtils.inStringArray(TestConfig.testBucket_na0, buckets));
        } catch (QiniuException e) {
            Assert.fail(e.response.toString());
        }

        try {
            dummyBucketManager.buckets();
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertEquals(401, e.code());
        }
    }

    @Test
    public void testDomains() {
        try {
            String[] domains = bucketManager.domainList(TestConfig.testBucket_z0);
            Assert.assertNotNull(domains);
        } catch (QiniuException e) {
            Assert.assertEquals(401, e.code());
        }
    }

    @Test
    public void testList() {
        try {
            String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
            for (String bucket : buckets) {
                FileListing l = bucketManager.listFiles(bucket, null, null, 2, null);
                Assert.assertNotNull(l.items[0]);
                Assert.assertNotNull(l.marker);
            }
        } catch (QiniuException e) {
            Assert.fail(e.response.toString());
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
                Assert.assertEquals(1, l.items.length);
                Assert.assertEquals(3, l.commonPrefixes.length);
            }
        } catch (QiniuException e) {
            Assert.fail(e.response.toString());
        }
    }

    @Test
    public void testListIterator() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            BucketManager.FileListIterator it = bucketManager.createFileListIterator(bucket, "", 20, null);

            Assert.assertTrue(it.hasNext());
            FileInfo[] items0 = it.next();
            Assert.assertNotNull(items0[0]);

            while (it.hasNext()) {
                FileInfo[] items = it.next();
                if (items.length > 1) {
                    Assert.assertNotNull(items[0]);
                }
            }
        }
    }

    @Test
    public void testListIteratorWithDefaultLimit() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            BucketManager.FileListIterator it = bucketManager.createFileListIterator(bucket, "");

            Assert.assertTrue(it.hasNext());
            FileInfo[] items0 = it.next();
            Assert.assertNotNull(items0[0]);

            while (it.hasNext()) {
                FileInfo[] items = it.next();
                if (items.length > 1) {
                    Assert.assertNotNull(items[0]);
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
                Assert.assertNotNull(info.hash);
                Assert.assertNotNull(info.mimeType);
            } catch (QiniuException e) {
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
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
                Assert.fail();
            } catch (QiniuException e) {
                Assert.assertEquals(code, e.code());
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
                Assert.fail(bucket + ":" + key + "==> " + "delete failed");
            } catch (QiniuException e) {
                Assert.assertEquals(code, e.code());
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
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
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
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
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
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }
    }

    @Test
    public void testChangeHeaders() {
        List<String[]> cases = new ArrayList<String[]>();
        cases.add(new String[]{TestConfig.testBucket_z0, TestConfig.testKey_z0});
        cases.add(new String[]{TestConfig.testBucket_na0, TestConfig.testKey_na0});

        for (String[] icase : cases) {
            String bucket = icase[0];
            String key = icase[1];
            try {
                Map<String, String> headers = new HashMap<>();
                Date d = new Date();
                SimpleDateFormat dateFm = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
                System.out.println(dateFm.format(d));
                headers.put("Content-Type", "image/png");
                headers.put("Last-Modified", dateFm.format(d));
                bucketManager.changeHeaders(bucket, key, headers);
            } catch (QiniuException e) {
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }
    }


    @Test
    public void testPrefetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            try {
                bucketManager.setImage(bucket, "https://developer.qiniu.com/");
                bucketManager.prefetch(bucket, "kodo/sdk/1239/java");
                bucketManager.unsetImage(bucket);
            } catch (QiniuException e) {
                Assert.fail(bucket + "==>" + e.response.toString());
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
                Assert.assertEquals(resHash, fRet.hash);

                //no key specified, use hash as file key
                fRet = bucketManager.fetch(resUrl, bucket);
                Assert.assertEquals(resHash, fRet.hash);
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
            Assert.assertEquals(resHash, fRet.hash);

            //no key specified, use hash as file key
            fRet = bucketManagerNa0.fetch(resUrl, TestConfig.testBucket_na0);
            Assert.assertEquals(resHash, fRet.hash);
        } catch (QiniuException e) {
            Assert.fail(e.response.toString());
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
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            }
            ops = new BucketManager.BatchOperations().addDeleteOp(bucket, copyToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.fail(e.response.toString());
            }
            String moveToKey = key + "to";
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addMoveOp(bucket, moveFromKey, bucket, moveToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            }
            try {
                bucketManager.delete(bucket, moveToKey);
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.fail(e.response.toString());
            }
            String renameToKey = "renameTo" + key;
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addRenameOp(bucket, renameFromKey, renameToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            }
            try {
                bucketManager.delete(bucket, renameToKey);
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            }
        }
    }

    @Test
    public void testBatchChangeType() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String okey = entry.getValue();
            String key = "batchChangeType" + Math.random();
            String key2 = "batchChangeType" + Math.random();
            String[] keyArray = new String[100];
            keyArray[0] = key;
            keyArray[1] = key2;


            BucketManager.BatchOperations opsCopy = new BucketManager.BatchOperations().
                    addCopyOp(bucket, okey, bucket, key).addCopyOp(bucket, okey, bucket, key2);

            try {
                bucketManager.batch(opsCopy);
            } catch (QiniuException e) {
                Assert.fail("batch copy failed: " + e.response.toString());
            }

            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addChangeTypeOps(bucket, StorageType.INFREQUENCY, keyArray);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            } finally {
                try {
                    bucketManager.delete(bucket, key);
                    bucketManager.delete(bucket, key2);
                } catch (QiniuException e) {
                    e.printStackTrace();
                }
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
                Assert.assertTrue("200 or 298", batchStatusCode.contains(response.statusCode));

                //clear ops
                ops.clearOps();

                //batch chane mimetype
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addChgmOp(bucket, keyArray[i], "image/png");
                }
                response = bucketManager.batch(ops);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(response.statusCode));

                //clear ops
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addDeleteOp(bucket, keyArray[i]);
                }
                response = bucketManager.batch(ops);
                Assert.assertTrue("200 or 298", batchStatusCode.contains(response.statusCode));

            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.fail(e.response.toString());
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
                    Assert.assertTrue("200 or 298", batchStatusCode.contains(b.code));
                }
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
            }

            BucketManager.BatchOperations opsDel = new BucketManager.BatchOperations()
                    .addDeleteOp(bucket, copyFromKey, moveFromKey, moveToKey, moveFromKey2, moveToKey2);

            try {
                bucketManager.batch(opsDel);
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.assertEquals(200, setResp.statusCode);

                setResp = bucketManager.setImage(bucket, srcSiteUrl, host);
                Assert.assertEquals(200, setResp.statusCode);

                Response unsetResp = bucketManager.unsetImage(bucket);
                Assert.assertEquals(200, unsetResp.statusCode);
            } catch (QiniuException e) {
                Assert.fail(e.response.toString());
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
                Assert.fail(e.response.toString());
            }
        }
    }

    @Test
    public void testChangeFileType() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String keyToChangeType = "keyToChangeType" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, keyToChangeType);
                Response response = bucketManager.changeType(bucket, keyToChangeType,
                        StorageType.INFREQUENCY);
                Assert.assertEquals(200, response.statusCode);
                //stat
                FileInfo fileInfo = bucketManager.stat(bucket, keyToChangeType);
                Assert.assertEquals(StorageType.INFREQUENCY.ordinal(), fileInfo.type);
                //delete the temp file
                bucketManager.delete(bucket, keyToChangeType);
            } catch (QiniuException e) {
                Assert.fail(bucket + ":" + key + " > " + keyToChangeType + " >> "
                        + StorageType.INFREQUENCY + " ==> " + e.response.toString());
            }
        }
    }

    @Test
    public void testAcl() throws QiniuException {
        bucketManager.setBucketAcl("javasdk", AclType.PRIVATE);
        BucketInfo info = bucketManager.getBucketInfo("javasdk");
        Assert.assertEquals(1, info.getPrivate());

        bucketManager.setBucketAcl("javasdk", AclType.PUBLIC);
        info = bucketManager.getBucketInfo("javasdk");
        Assert.assertEquals(0, info.getPrivate());

        try {
            bucketManager.setBucketAcl("javsfsdfsfsdfsdfsdfasdk1", AclType.PRIVATE);
//            Assert.fail(" 空间 javasdk2 不存在，理应报错 ");   // kodo 实际响应 200
        } catch (QiniuException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testBucketInfo() throws QiniuException {
        BucketInfo info = bucketManager.getBucketInfo("javasdk");
        System.out.println(info.getRegion());
        System.out.println(info.getZone());
        System.out.println(info.getPrivate());
        try {
            BucketInfo info2 = bucketManager.getBucketInfo("javasdk2");
            Assert.fail(" 空间 javasdk2 不存在，理应报错 ");
        } catch (QiniuException e) {
            if (e.response != null) {
                System.out.println(e.response.getInfo());
                throw e;
            }
        }
    }

    @Test
    public void testIndexPage() throws QiniuException {
        bucketManager.setIndexPage("javasdk", IndexPageType.HAS);
        BucketInfo info = bucketManager.getBucketInfo("javasdk");
        Assert.assertEquals(0, info.getNoIndexPage());

        bucketManager.setIndexPage("javasdk", IndexPageType.NO);
        info = bucketManager.getBucketInfo("javasdk");
        Assert.assertEquals(1, info.getNoIndexPage());

        try {
            bucketManager.setIndexPage("javasdk2", IndexPageType.HAS);
//            Assert.fail(" 空间 javasdk2 不存在，理应报错 ");   // kodo 实际响应 200
        } catch (QiniuException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
