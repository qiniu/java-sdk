package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.*;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BucketTest2 {

    List<Integer> batchStatusCode = Arrays.asList(200, 298);
    private BucketManager bucketManager;
    private BucketManager dummyBucketManager;
    private UploadManager uploadManager;

    /**
     * 初始化
     *
     * @throws Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        Configuration cfg = new Configuration();
        cfg.region = Region.autoRegion();
        // cfg.useHttpsDomains = false;
        this.bucketManager = new BucketManager(TestConfig.testAuth, cfg);
        this.uploadManager = new UploadManager(cfg);
        this.dummyBucketManager = new BucketManager(TestConfig.dummyAuth, new Configuration());
    }

    /**
     * 测试列举空间名
     */
    @Test
    @Tag("IntegrationTest")
    public void testBuckets() {
        try {
            String[] buckets = bucketManager.buckets();
            assertTrue(StringUtils.inStringArray(TestConfig.testBucket_z0, buckets));
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }

        try {
            dummyBucketManager.buckets();
            fail();
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(401)));
        }
    }

    /**
     * 测试列举空间域名
     */
    @Test
    @Tag("IntegrationTest")
    public void testDomains() {
        try {
            String[] domains = bucketManager.domainList(TestConfig.testBucket_z0);
            assertNotNull(domains);
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(401)));
        }
    }

    /**
     * 测试list接口，limit=2
     */
    @Test
    @Tag("IntegrationTest")
    public void testList() {
        try {
            String[] buckets = new String[]{TestConfig.testBucket_z0};
            for (String bucket : buckets) {
                FileListing l = bucketManager.listFiles(bucket, null, null, 2, null);
                assertNotNull(l.items[0]);
                assertNotNull(l.marker);
            }
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }

    /**
     * 测试list接口的delimiter
     */
    @Test
    @Tag("IntegrationTest")
    public void testListUseDelimiter() {
        try {
            Map<String, String> bucketKeyMap = new HashMap<String, String>();
            bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

            for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
                String bucket = entry.getKey();
                String key = entry.getValue();
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/1/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/2/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/3/" + key, true);
                FileListing l = bucketManager.listFiles(bucket, "testListUseDelimiter", null, 10, "/");
                assertEquals(1, l.commonPrefixes.length);
            }
        } catch (QiniuException e) {
            fail(e.response.toString());
        }
    }

    /**
     * 测试文件迭代器
     */
    @Test
    @Tag("IntegrationTest")
    public void testListIterator() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
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

    /**
     * 测试文件迭代器
     */
    @Test
    @Tag("IntegrationTest")
    public void testListIteratorWithDefaultLimit() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
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
    @Tag("IntegrationTest")
    public void testListV2() {
        try {
            String[] buckets = new String[]{TestConfig.testBucket_z0};
            for (String bucket : buckets) {
                String prefix = "sdfisjfisjei473ysfGYDEJDSDJWEDJNFD23rje";
                FileListing l = bucketManager.listFilesV2(bucket, prefix, null, 2, null);
                assertTrue(l.items.length == 0);
                assertNull(l.marker);
            }

            for (String bucket : buckets) {
                FileListing l = bucketManager.listFilesV2(bucket, null, null, 2, null);
                assertNotNull(l.items[0]);
                assertNotNull(l.marker);
            }
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testListMarkerV2() {
        try {
            String marker = null;
            int count = 0;
            do {
                FileListing l = bucketManager.listFilesV2(TestConfig.testBucket_z0, "pi", marker, 2, null);
                marker = l.marker;
                for (FileInfo f : l.items) {
                    assertNotNull(f.key);
                }
                count++;
            } while (!StringUtils.isNullOrEmpty(marker));
            assertTrue(count > 0);
        } catch (QiniuException e) {
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }

    /**
     * 测试stat
     */
    @Test
    @Tag("IntegrationTest")
    public void testStat() {
        String ruleName = "javaStatusRule";
        String copyKey = TestConfig.testBucket_z0 + "_status_copy";

        try {
            bucketManager.deleteBucketLifecycleRule(TestConfig.testBucket_z0, ruleName);
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        try {
            bucketManager.copy(TestConfig.testBucket_z0, TestConfig.testKey_z0, TestConfig.testBucket_z0, copyKey, true);
            bucketManager.changeType(TestConfig.testBucket_z0, copyKey, StorageType.Archive);
            bucketManager.restoreArchive(TestConfig.testBucket_z0, copyKey, 1);
            FileInfo info = bucketManager.stat(TestConfig.testBucket_z0, copyKey);
            assertNotNull(info.hash);
            assertNotNull(info.mimeType);
            assertNotNull(info.restoreStatus);

            bucketManager.delete(TestConfig.testBucket_z0, copyKey);
            BucketLifeCycleRule rule = new BucketLifeCycleRule(ruleName, "");
            rule.setToLineAfterDays(1);
            rule.setToArchiveAfterDays(2);
            rule.setToDeepArchiveAfterDays(3);
            rule.setDeleteAfterDays(4);
            bucketManager.putBucketLifecycleRule(TestConfig.testBucket_z0, rule);
            bucketManager.copy(TestConfig.testBucket_z0, TestConfig.testKey_z0, TestConfig.testBucket_z0, copyKey, true);
            bucketManager.deleteAfterDays(TestConfig.testBucket_z0, copyKey, 1);
            info = bucketManager.stat(TestConfig.testBucket_z0, copyKey);
            assertNotNull(info.hash);
            assertNotNull(info.mimeType);
            assertNotNull(info.expiration);
//            assertNotNull(info.transitionToIA);
//            assertNotNull(info.transitionToArchive);
//            assertNotNull(info.transitionToDeepArchive);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail("status change type fail:" + e);
        } finally {
            try {
                bucketManager.deleteBucketLifecycleRule(TestConfig.testBucket_z0, ruleName);
            } catch (QiniuException e) {
                e.printStackTrace();
            }
        }

        try {
            FileInfo info = bucketManager.stat(TestConfig.testBucket_z0, copyKey);
            assertNotNull(info.hash);
            assertNotNull(info.mimeType);
            assertNotNull(info.expiration);
        } catch (QiniuException e) {
            fail("status fail:" + e);
        }

        // test exists
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            try {
                FileInfo info = bucketManager.stat(bucket, key);
                assertNotNull(info.hash);
                assertNotNull(info.mimeType);
            } catch (QiniuException e) {
                fail(bucket + ":" + key + "==> " + e.response);
            }
        }

        // test bucket not exits or file not exists
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
                if (e.response != null) {
                    System.out.println(e.code() + "\n" + e.response.getInfo());
                }
                System.out.println(code + ",  " + e.code());
                assertEquals(code, e.code());
            }
        }
    }

    /**
     * 测试删除
     */
    @Test
    @Tag("IntegrationTest")
    public void testDelete() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyKey = "delete" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, copyKey, true);
                bucketManager.delete(bucket, copyKey);
            } catch (QiniuException e) {
                fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }

        Map<String[], Integer> entryCodeMap = new HashMap<String[], Integer>();
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_KEY_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, null}, TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, null}, TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);

        for (Map.Entry<String[], Integer> entry : entryCodeMap.entrySet()) {
            String bucket = entry.getKey()[0];
            String key = entry.getKey()[1];
            int code = entry.getValue();
            try {
                bucketManager.delete(bucket, key);
                fail(bucket + ":" + key + "==> " + "delete failed");
            } catch (QiniuException e) {
                assertEquals(code, e.code());
            }
        }
    }

    /**
     * 测试移动/重命名
     */
    @Test
    @Tag("IntegrationTest")
    public void testRename() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

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
                fail(bucket + ":" + key + "==> " + e.response);
            }
        }
    }

    /**
     * 测试复制
     */
    @Test
    @Tag("IntegrationTest")
    public void testCopy() {
        Response response;
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyToKey = "copyTo" + Math.random();
            try {
                response = bucketManager.copy(bucket, key, bucket, copyToKey);
                System.out.println(response.statusCode);
                bucketManager.delete(bucket, copyToKey);
            } catch (QiniuException e) {
                e.printStackTrace();
                fail(bucket + ":" + key + "==> " + e.response);
            }
        }
    }

    /**
     * 测试修改文件MimeType
     */
    @Test
    @Tag("IntegrationTest")
    public void testChangeMime() {
        List<String[]> cases = new ArrayList<String[]>();
        cases.add(new String[]{TestConfig.testBucket_z0, TestConfig.testKey_z0, "image/png"});

        for (String[] icase : cases) {
            String bucket = icase[0];
            String key = icase[1];
            String mime = icase[2];
            try {
                bucketManager.changeMime(bucket, key, mime);
            } catch (QiniuException e) {
                fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }
    }

    /**
     * 测试修改文件元信息
     */
    @Test
    @Tag("IntegrationTest")
    public void testChangeHeaders() {
        List<String[]> cases = new ArrayList<String[]>();
        cases.add(new String[]{TestConfig.testBucket_z0, TestConfig.testKey_z0});

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
                fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }
    }

    /**
     * 测试镜像源
     */
    // TODO
    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPrefetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            try {
                bucketManager.setImage(bucket, "https://developer.qiniu.com/");
                // 有/image接口似乎有延迟，导致prefetch概率失败
                // "Host":"iovip.qbox.me" "X-Reqid":"q2IAABkZC1dUxIkV"
                // "Host":"iovip-na0.qbox.me" "X-Reqid":"XyMAAHgSE98nxYkV"
                bucketManager.prefetch(bucket, "kodo/sdk/1239/java");
                bucketManager.unsetImage(bucket);
            } catch (QiniuException e) {
                fail(bucket + "==>" + e.response.toString());
            }
        }
    }

    /**
     * 测试同步Fetch
     */
    @Test
    @Tag("IntegrationTest")
    public void testFetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            try {
                String resUrl = "http://devtools.qiniu.com/qiniu.png";
                String resKey = "qiniu.png";
                String resHash = "FpHyF0kkil3sp-SaXXX8TBJY3jDh";
                FetchRet fRet = bucketManager.fetch(resUrl, bucket, resKey);
                assertEquals(resHash, fRet.hash);

                // no key specified, use hash as file key
                fRet = bucketManager.fetch(resUrl, bucket);
                assertEquals(resHash, fRet.hash);
            } catch (QiniuException e) {
                e.printStackTrace();
                // use e.response.toString() may get NullPointException
                // when java.net.SocketTimeoutException: timeout
                fail(e.getMessage());
            }
        }
    }

    /**
     * 测试获取bucketinfo
     */
    @Test
    @Tag("IntegrationTest")
    public void testBucketInfo() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            try {
                BucketInfo info = bucketManager.getBucketInfo(bucket);
                assertNotNull(info, "info is not null.");

                bucketManager.getBucketInfo(TestConfig.dummyBucket);

            } catch (QiniuException e) {
                assertEquals(612, e.response.statusCode);
            }
        }
    }

    /**
     * 测试能够获取bucket源站域名并能用于下载
     */
    @Test
    @Tag("IntegrationTest")
    public void testDefaultIoSrcDomainDownload() {
        String bucket = TestConfig.testBucket_z0;
        String key = "test_bucket_domain_file_key";

        // 获取默认源站域名
        String domain = "";
        try {
            domain = bucketManager.getDefaultIoSrcHost(bucket);
            assertNotNull(domain, "domain is not null.");
        } catch (QiniuException e) {
            fail(e);
        }

        // 构造测试数据
        byte[] uploadData = new byte[1024];
        new Random().nextBytes(uploadData);

        // 将测试数据上传到测试文件
        try {
            StringMap map = new StringMap();
            map.put("insertOnly", "1");
            uploadManager.put(
                    new ByteArrayInputStream(uploadData),
                    key, TestConfig.testAuth.uploadToken(bucket), map, null);
        } catch (QiniuException e) {
            fail(e);
        }

        // 测试文件是否存在
        try {
            FileInfo info = bucketManager.stat(bucket, key);
            assertNotNull(info, "info is not null.");
        } catch (QiniuException e) {
            fail(e);
        }

        // 构造下载链接
        String url = "";
        try {
            url = new DownloadUrl(domain, false, key).buildURL();
            url = TestConfig.testAuth.privateDownloadUrl(url, 3600);
        } catch (QiniuException e) {
            fail(e);
        }

        // 下载测试文件
        byte[] downloadData;
        Client client = new Client();
        try {
            downloadData = client.get(url).body();
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
        assertArrayEquals(uploadData, downloadData);

        // 删除测试文件
        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException e) {
            fail(e);
        }
    }

    /**
     * 测试设置空间referer防盗链
     */
    @Test
    @Tag("IntegrationTest")
    public void testPutReferAntiLeech() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            BucketReferAntiLeech leech = new BucketReferAntiLeech();
            Response response;
            BucketInfo bucketInfo;
            try {
                // 测试白名单
                leech.setMode(1);
                leech.setPattern("www.qiniu.com");
                leech.setAllowEmptyReferer(false);
                System.out.println(leech.asQueryString());
                response = bucketManager.putReferAntiLeech(bucket, leech);
                assertEquals(200, response.statusCode);
                bucketInfo = bucketManager.getBucketInfo(bucket);
                assertEquals(1, bucketInfo.getAntiLeechMode());
                assertArrayEquals((new String[]{"www.qiniu.com"}), bucketInfo.getReferWhite());
                assertEquals(false, bucketInfo.isNoRefer());

                // 测试黑名单
                leech.setMode(2);
                leech.setPattern("www.baidu.com");
                leech.setAllowEmptyReferer(true);
                System.out.println(leech.asQueryString());
                response = bucketManager.putReferAntiLeech(bucket, leech);
                assertEquals(200, response.statusCode);
                bucketInfo = bucketManager.getBucketInfo(bucket);
                assertEquals(2, bucketInfo.getAntiLeechMode());
                assertArrayEquals((new String[]{"www.baidu.com"}), bucketInfo.getReferBlack());
                assertEquals(true, bucketInfo.isNoRefer());

                // 测试关闭
                leech = new BucketReferAntiLeech();
                System.out.println(leech.asQueryString());
                response = bucketManager.putReferAntiLeech(bucket, leech);
                assertEquals(200, response.statusCode);
                bucketInfo = bucketManager.getBucketInfo(bucket);
                assertEquals(0, bucketInfo.getAntiLeechMode());
                assertNull(bucketInfo.getReferBlack(), "ReferBlack should be Null");
                assertNull(bucketInfo.getReferWhite(), "ReferWhtie should be Null");
                assertEquals(false, bucketInfo.isNoRefer());

            } catch (Exception e) {
                if (e instanceof QiniuException) {
                    QiniuException ex = (QiniuException) e;
                    fail(ex.response.toString());
                }
            }
        }
    }

    /**
     * 测试设置空间生命周期规则
     */
    @Test
    @Tag("IntegrationTest")
    public void testBucketLifeCycleRule() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            Response response;
            BucketLifeCycleRule rule;
            BucketLifeCycleRule[] rules;
            try {
                // clear
                clearBucketLifeCycleRule(bucket);

                // 追加规则
                rule = new BucketLifeCycleRule("a", "x");
                System.out.println(rule.asQueryString());
                response = bucketManager.putBucketLifecycleRule(bucket, rule);
                assertEquals(200, response.statusCode);

                // 更新规则（name不存在）
                try {
                    rule = new BucketLifeCycleRule("x", "a");
                    response = bucketManager.updateBucketLifeCycleRule(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 更新规则
                rule = new BucketLifeCycleRule("a", null);
                System.out.println(rule.asQueryString());
                response = bucketManager.updateBucketLifeCycleRule(bucket, rule);
                assertEquals(200, response.statusCode);

                // 重复设置（name、prefix重复）
                try {
                    bucketManager.putBucketLifecycleRule(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 重复设置（name重复）
                try {
                    rule = new BucketLifeCycleRule("a", "b");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketLifecycleRule(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 重复设置（prefix重复）
                try {
                    rule = new BucketLifeCycleRule("b", null);
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketLifecycleRule(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 追加规则
                rule = new BucketLifeCycleRule("b", "ab");
                System.out.println(rule.asQueryString());
                response = bucketManager.putBucketLifecycleRule(bucket, rule);
                assertEquals(200, response.statusCode);

                // 追加规则
                rule = new BucketLifeCycleRule("c", "abc");
                System.out.println(rule.asQueryString());
                response = bucketManager.putBucketLifecycleRule(bucket, rule);
                assertEquals(200, response.statusCode);

                // clear
                clearBucketLifeCycleRule(bucket);

                // 再获取规则
                rules = bucketManager.getBucketLifeCycleRule(bucket);
                assertEquals(0, rules.length);

            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    private void clearBucketLifeCycleRule(String bucket) throws QiniuException {
        // 获取规则
        BucketLifeCycleRule[] rules = bucketManager.getBucketLifeCycleRule(bucket);
        try {
            for (BucketLifeCycleRule r : rules) {
                System.out.println("name=" + r.getName());
                System.out.println("prefix=" + r.getPrefix());
                assertNotEquals("x", r.getPrefix());
            }
        } finally {
            // 删除规则
            for (BucketLifeCycleRule r : rules) {
                bucketManager.deleteBucketLifecycleRule(bucket, r.getName());
            }
        }
    }

    /**
     * 测试事件通知
     */
    @Test
    @Tag("IntegrationTest")
    public void testBucketEvent() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        String[] keys = new String[]{TestConfig.testKey_z0, TestConfig.testKey_na0};
        for (int i = 0; i < buckets.length; i++) {
            String bucket = buckets[i];
            String key = keys[i];
            Response response;
            BucketEventRule rule;
            BucketEventRule[] rules;
            try {
                // clear
                clearBucketEvent(bucket);

                // 追加Event（invalid events）
                try {
                    rule = new BucketEventRule("a", new String[]{}, new String[]{});
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 追加Event（error:callbackURL must starts with http:// or https://）
                try {
                    rule = new BucketEventRule("a", new String[]{"put", "mkfile"}, new String[]{});
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 追加Event
                rule = new BucketEventRule("a",
                        new String[]{"put", "mkfile", "delete", "copy", "move", "append", "disable", "enable",
                                "deleteMarkerCreate"},
                        new String[]{"https://requestbin.fullcontact.com/1dsqext1?inspect",
                                "https://requestbin.fullcontact.com/160bunp1?inspect"});
                System.out.println(rule.asQueryString());
                response = bucketManager.putBucketEvent(bucket, rule);
                assertEquals(200, response.statusCode);
                System.out.println(response.url());
                System.out.println(response.reqId);

                // 重复追加Event（error:event name exists）
                try {
                    rule.setName("a");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 追加Event
                rule.setName("b");
                rule.setPrefix(key);
                System.out.println(rule.asQueryString());
                response = bucketManager.putBucketEvent(bucket, rule);
                assertEquals(200, response.statusCode);

                // 重复追加Event（error:event prefix and suffix exists）
                try {
                    rule.setName("b");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 触发时间，回调成功与否 不检测
                response = bucketManager.copy(bucket, key, bucket, key + "CopyByEvent", true);
                assertEquals(200, response.statusCode);

                // 更新Event（error:event name not found）
                try {
                    rule.setName("c");
                    rule.setPrefix("c");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.updateBucketEvent(bucket, rule);
                    fail();
                } catch (QiniuException e) {
                    assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }

                // 更新Event
                rule.setName("b");
                rule.setPrefix("c");
                rule.setEvents(new String[]{"disable", "enable", "deleteMarkerCreate"});
                System.out.println(rule.asQueryString());
                response = bucketManager.updateBucketEvent(bucket, rule);
                assertEquals(200, response.statusCode);

                // clear
                clearBucketEvent(bucket);

                // 再获取Event
                rules = bucketManager.getBucketEvents(bucket);
                assertEquals(0, rules.length);

            } catch (QiniuException e) {
                fail("" + e.response);
            }
        }
    }

    private void clearBucketEvent(String bucket) throws QiniuException {
        // 获取Event
        BucketEventRule[] rules = bucketManager.getBucketEvents(bucket);
        for (BucketEventRule r : rules) {
            System.out.println("name=" + r.getName());
            System.out.println("prefix=" + r.getPrefix());
            System.out.println("suffix=" + r.getSuffix());
            System.out.println("event=" + Arrays.asList(r.getEvents()));
            System.out.println("callbackUrls=" + Arrays.asList(r.getCallbackUrls()));
        }
        // 删除Event
        for (BucketEventRule r : rules) {
            bucketManager.deleteBucketEvent(bucket, r.getName());
        }
    }

    /**
     * 测试跨域规则
     */
    @Test
    @Tag("IntegrationTest")
    public void testCorsRules() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            CorsRule rule1 = new CorsRule(new String[]{"*"}, new String[]{""});
            CorsRule rule2 = new CorsRule(new String[]{"*"}, new String[]{"GET", "POST"});
            CorsRule rule3 = new CorsRule(new String[]{""}, new String[]{"GET", "POST"});
            List<CorsRule[]> rulesList = new ArrayList<>();
            rulesList.add(corsRules(rule1));
            rulesList.add(corsRules(rule2));
            rulesList.add(corsRules(rule3));
            try {
                for (CorsRule[] rules : rulesList) {
                    Response response;
                    String bodyString, bodyString2;
                    // 设置跨域规则
                    bodyString = Json.encode(rules);
                    System.out.println(bodyString);
                    response = bucketManager.putCorsRules(bucket, rules);
                    assertEquals(200, response.statusCode);
                    // 获取跨域规则
                    rules = bucketManager.getCorsRules(bucket);
                    bodyString2 = Json.encode(rules);
                    System.out.println(bodyString2);
                    assertEquals(bodyString, bodyString2);
                }
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    private CorsRule[] corsRules(CorsRule... rules) {
        return rules.clone();
    }

    /**
     * 测试设置回源规则
     */
    @Test
    @Disabled
    @Tag("IntegrationTest")
    // TODO
    public void testPutBucketSourceConfig() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            try {

            } catch (Exception e) {

            }
        }
    }

    /**
     * 测试设置max-age属性
     */
    @Test
    @Tag("IntegrationTest")
    public void testPutBucketMaxAge() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            final long[] maxAges = {Integer.MIN_VALUE, -54321, -1, 0, 1, 8, 1234567, 11111111, Integer.MAX_VALUE};
            try {
                for (long maxAge : maxAges) {
                    // 设置max-age
                    Response response = bucketManager.putBucketMaxAge(bucket, maxAge);
                    assertEquals(200, response.statusCode);
                    // 获取max-age
                    BucketInfo bucketInfo = bucketManager.getBucketInfo(bucket);
                    long expect = maxAge;
                    long actual = bucketInfo.getMaxAge();
                    System.out.println("expect=" + expect);
                    System.out.println("actual=" + actual);
                    assertEquals(expect, actual);
                }
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试设置max-age属性<br>
     * UC有缓存，这种方法不合适于测试
     */
    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testPutBucketMaxAge2() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        String[] buckets = new String[]{TestConfig.testBucket_z0};
        String[] urls = new String[]{TestConfig.testUrl_z0, TestConfig.testUrl_na0};
        for (int i = 0; i < buckets.length; i++) {
            String bucket = buckets[i];
            String url = urls[i];
            final long[] maxAges = {Integer.MIN_VALUE, -54321, -1, 0, 1, 8, 1234567, 11111111, Integer.MAX_VALUE};
            try {
                for (long maxAge : maxAges) {
                    // 设置max-age
                    System.out.println("maxAge=" + maxAge);
                    Response response = bucketManager.putBucketMaxAge(bucket, maxAge);
                    assertEquals(200, response.statusCode, msg);
                    // 有缓存时间，停几秒
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 测试Cache-Control是否设置成功
                    String actual = respHeader(url, "Cache-Control");
                    String expect = maxAge <= 0 ? "31536000" : Long.toString(maxAge);
                    System.out.println("url=" + url);
                    System.out.println("expect=" + expect);
                    System.out.println("actual=" + actual);
                    assertEquals(msg, "[public, max-age=" + expect + "]", actual);
                }
            } catch (IOException e) {
                if (e instanceof QiniuException) {
                    fail(msg + ((QiniuException) e).response.toString());
                }
            }
        }
    }

    /**
     * 测试设置空间私有化、公有化
     */
    @Test
    @Tag("IntegrationTest")
    public void testPutBucketAccessMode() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            Response response;
            try {
                // 测试转私有空间
                response = bucketManager.putBucketAccessMode(bucket, AclType.PRIVATE);
                assertEquals(200, response.statusCode);
                BucketInfo info = bucketManager.getBucketInfo(bucket);
                assertEquals(1, info.getPrivate());

                // 测试转公有空间
                response = bucketManager.putBucketAccessMode(bucket, AclType.PUBLIC);
                assertEquals(200, response.statusCode);
                info = bucketManager.getBucketInfo(bucket);
                assertEquals(0, info.getPrivate());

            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
        // 测试空间不存在情况
        try {
            bucketManager.putBucketAccessMode(TestConfig.dummyBucket, AclType.PRIVATE);
        } catch (QiniuException e) {
            assertEquals(631, e.response.statusCode);
        }
    }

    /**
     * 测试设置、获取空间配额
     */
    @Test
    @Tag("IntegrationTest")
    public void testBucketQuota() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
        for (String bucket : buckets) {
            try {
                testBucketQuota(bucket, -2, -2);
            } catch (QiniuException e) {
                assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
            }
            try {
                testBucketQuota(bucket, 0, 0);
                testBucketQuota(bucket, 100, 100);
                testBucketQuota(bucket, 0, 100);
                testBucketQuota(bucket, 100, -1);
                testBucketQuota(bucket, -1, -1);
            } catch (QiniuException e) {
                assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
            }
        }
    }

    private void testBucketQuota(String bucket, long size, long count) throws QiniuException {
        Response response = bucketManager.putBucketQuota(bucket, new BucketQuota(size, count));
        assertEquals(200, response.statusCode);
        BucketQuota bucketQuota = bucketManager.getBucketQuota(bucket);
        if (size != 0) {
            assertEquals(size, bucketQuota.getSize());
        }
        if (count != 0) {
            assertEquals(count, bucketQuota.getCount());
        }
    }

    /**
     * 测试批量复制
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchCopy() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyToKey = "copyTo" + Math.random();
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().addCopyOp(bucket, key, bucket,
                    copyToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
            } catch (QiniuException e) {
                fail("" + e.response);
            }
            ops = new BucketManager.BatchOperations().addDeleteOp(bucket, copyToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试批量移动
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchMove() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

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
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().addMoveOp(bucket, moveFromKey,
                    bucket, moveToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
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

    /**
     * 测试批量重命名
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchRename() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

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
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().addRenameOp(bucket, renameFromKey,
                    renameToKey);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                System.out.println(bs[0].code);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
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

    /**
     * 测试批量stat
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchStat() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String[] keyArray = new String[100];
            for (int i = 0; i < keyArray.length; i++) {
                keyArray[i] = key;
            }
            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().addStatOps(bucket, keyArray);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试批量修改文件MimeType
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchChangeType() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String okey = entry.getValue();
            String key = "batchChangeType" + Math.random();
            String key2 = "batchChangeType" + Math.random();
            String[] keyArray = new String[100];
            keyArray[0] = key;
            keyArray[1] = key2;

            BucketManager.BatchOperations opsCopy = new BucketManager.BatchOperations()
                    .addCopyOp(bucket, okey, bucket, key).addCopyOp(bucket, okey, bucket, key2);

            try {
                bucketManager.batch(opsCopy);
            } catch (QiniuException e) {
                fail("batch copy failed: " + e.response.toString());
            }

            BucketManager.BatchOperations ops = new BucketManager.BatchOperations().addChangeTypeOps(bucket,
                    StorageType.INFREQUENCY, keyArray);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                assertTrue(batchStatusCode.contains(bs[0].code), "200 or 298");
            } catch (QiniuException e) {
                fail(e.response.toString());
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

    /**
     * 测试批量操作
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatchCopyChgmDelete() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();

            // make 100 copies
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
                assertTrue(batchStatusCode.contains(response.statusCode), "200 or 298");

                // clear ops
                ops.clearOps();

                // batch chane mimetype
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addChgmOp(bucket, keyArray[i], "image/png");
                }
                response = bucketManager.batch(ops);
                assertTrue(batchStatusCode.contains(response.statusCode), "200 or 298");

                // clear ops
                for (int i = 0; i < keyArray.length; i++) {
                    ops.addDeleteOp(bucket, keyArray[i]);
                }
                response = bucketManager.batch(ops);
                assertTrue(batchStatusCode.contains(response.statusCode), "200 or 298");

            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试批量操作
     */
    @Test
    @Tag("IntegrationTest")
    public void testBatch() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

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
                e.printStackTrace();
                fail(e.response.toString());
            }

            BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                    .addCopyOp(bucket, key, bucket, copyFromKey).addMoveOp(bucket, moveFromKey, bucket, moveToKey)
                    .addRenameOp(bucket, moveFromKey2, moveToKey2).addStatOps(bucket, array)
                    .addStatOps(bucket, array[0]);
            try {
                Response r = bucketManager.batch(ops);
                BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                for (BatchStatus b : bs) {
                    assertTrue(batchStatusCode.contains(b.code), "200 or 298");
                }
            } catch (QiniuException e) {
                fail(e.response.toString());
            }

            BucketManager.BatchOperations opsDel = new BucketManager.BatchOperations().addDeleteOp(bucket, copyFromKey,
                    moveFromKey, moveToKey, moveFromKey2, moveToKey2);

            try {
                bucketManager.batch(opsDel);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试设置、取消空间镜像源
     */
    // TODO
    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testSetAndUnsetImage() {
        String[] buckets = new String[]{TestConfig.testBucket_z0};
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

    /**
     * 测试文件生命周期
     */
    @Test
    @Tag("IntegrationTest")
    public void testDeleteAfterDays() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String keyForDelete = "keyForDelete" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, keyForDelete);
                Response response = bucketManager.deleteAfterDays(bucket, keyForDelete, 10);
                assertEquals(200, response.statusCode);
            } catch (QiniuException e) {
                fail(e.response.toString());
            }
        }
    }

    /**
     * 测试修改文件类型
     */
    @Test
    @Tag("IntegrationTest")
    public void testChangeFileType() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);

        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String keyToChangeType = "keyToChangeType" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, keyToChangeType);
                Response response = bucketManager.changeType(bucket, keyToChangeType, StorageType.INFREQUENCY);
                assertEquals(200, response.statusCode);
                // stat
                FileInfo fileInfo = bucketManager.stat(bucket, keyToChangeType);
                assertEquals(StorageType.INFREQUENCY.ordinal(), fileInfo.type);
                // delete the temp file
                bucketManager.delete(bucket, keyToChangeType);
            } catch (QiniuException e) {
                fail(bucket + ":" + key + " > " + keyToChangeType + " >> " + StorageType.INFREQUENCY + " ==> "
                        + e.response.toString());
            }
        }
    }

    /**
     * 测试noIndexPage
     *
     * @throws QiniuException
     */
    @Test
    @Tag("IntegrationTest")
    public void testIndexPage() throws QiniuException {
        bucketManager.setIndexPage(TestConfig.testBucket_z0, IndexPageType.HAS);
        BucketInfo info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
        assertEquals(0, info.getNoIndexPage());

        bucketManager.setIndexPage(TestConfig.testBucket_z0, IndexPageType.NO);
        info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
        assertEquals(1, info.getNoIndexPage());

        try {
            bucketManager.setIndexPage(TestConfig.dummyBucket, IndexPageType.HAS);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得响应头中key字段的值
     *
     * @param url
     * @param key
     * @return
     * @throws IOException
     */
    String respHeader(String url, String key) throws IOException {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        okhttp3.Response response = call.execute();
        List<String> values = response.headers().values(key);
        return values.toString();
    }

}
