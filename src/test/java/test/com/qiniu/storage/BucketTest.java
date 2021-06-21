package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.*;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BucketTest {

    List<Integer> batchStatusCode = Arrays.asList(200, 298);
    private BucketManager dummyBucketManager;

    /**
     * 初始化
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Configuration cfg = new Configuration(Zone.autoZone());
        cfg.useHttpsDomains = false;
        this.dummyBucketManager = new BucketManager(TestConfig.dummyAuth, new Configuration());
    }

    /**
     * 测试列举空间名
     */
    @Test
    public void testBuckets() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    String[] buckets = bucketManager.buckets();
                    Assert.assertTrue(StringUtils.inStringArray(file.getBucketName(), buckets));
                } catch (QiniuException e) {
                    Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
                }
            }
        });

        try {
            dummyBucketManager.buckets();
            Assert.fail();
        } catch (QiniuException e) {
            Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(401)));
        }
    }

    /**
     * 测试列举空间域名
     */
    @Test
    public void testDomains() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    String[] domains = bucketManager.domainList(file.getBucketName());
                    Assert.assertNotNull(domains);
                } catch (QiniuException e) {
                    Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(401)));
                }
            }
        });
    }

    /**
     * 测试list接口，limit=2
     */
    @Test
    public void testList() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    FileListing l = bucketManager.listFiles(file.getBucketName(), null, null, 2, null);
                    Assert.assertNotNull(l.items[0]);
                    Assert.assertNotNull(l.marker);
                } catch (QiniuException e) {
                    Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
                }
            }
        });
    }

    @Test
    public void testListV2() throws Exception {
        try {
            testFileWithHandler(new TestFileHandler() {
                @Override
                public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                    String prefix = "sdfisjfisjei473ysfGYDEJDSDJWEDJNFD23rje";
                    FileListing l = bucketManager.listFilesV2(file.getBucketName(), prefix, null, 2, null);
                    Assert.assertTrue(l.items.length == 0);
                    Assert.assertNull(l.marker);
                }
            });

            testFileWithHandler(new TestFileHandler() {
                @Override
                public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                    FileListing l = bucketManager.listFilesV2(file.getBucketName(), null, null, 2, null);
                    Assert.assertNotNull(l.items[0]);
                    Assert.assertNotNull(l.marker);
                }
            });
        } catch (QiniuException e) {
            Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }


    @Test
    public void testListMarkerV2() throws Exception {
        try {
            testFileWithHandler(new TestFileHandler() {
                @Override
                public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                    String marker = null;
                    int count = 0;
                    do {
                        FileListing l = bucketManager.listFilesV2(file.getBucketName(), "pi", marker, 2, null);
                        marker = l.marker;
                        for (FileInfo f : l.items) {
                            Assert.assertNotNull(f.key);
                        }
                        count++;
                    } while (!StringUtils.isNullOrEmpty(marker));
                    Assert.assertTrue(count > 0);
                }
            });

        } catch (QiniuException e) {
            Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }

    /**
     * 测试list接口的delimiter
     */
    @Test
    public void testListUseDelimiter() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    String bucket = file.getBucketName();
                    String key = file.getKey();
                    Response r = bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/" + key, true);
                    bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/1/" + key, true);
                    bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/2/" + key, true);
                    bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/3/" + key, true);
                    FileListing l = bucketManager.listFiles(bucket, "testListUseDelimiter", null, 10, "/");
                    Assert.assertEquals(1, l.commonPrefixes.length);
                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    /**
     * 测试文件迭代器
     */
    @Test
    public void testListIterator() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                BucketManager.FileListIterator it = bucketManager.createFileListIterator(file.getBucketName(), "", 20, null);
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
        });
    }

    /**
     * 测试文件迭代器
     */
    @Test
    public void testListIteratorWithDefaultLimit() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                BucketManager.FileListIterator it = bucketManager.createFileListIterator(file.getBucketName(), "");

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
        });
    }

    /**
     * 测试stat
     */
    @Test
    public void testStat() throws Exception {
        //test exists
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                try {
                    FileInfo info = bucketManager.stat(bucket, key);
                    Assert.assertNotNull(info.hash);
                    Assert.assertNotNull(info.mimeType);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
                }
            }
        });

        //test bucket not exits or file not exists
        Map<String[], Integer> entryCodeMap = new HashMap<String[], Integer>();
        entryCodeMap.put(new String[]{TestConfig.testBucket_z0, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_KEY_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.testKey_z0},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);
        entryCodeMap.put(new String[]{TestConfig.dummyBucket, TestConfig.dummyKey},
                TestConfig.ERROR_CODE_BUCKET_NOT_EXIST);

        for (Map.Entry<String[], Integer> entry : entryCodeMap.entrySet()) {
            Configuration cfg = new Configuration(Zone.autoZone());
            cfg.useHttpsDomains = false;
            BucketManager bucketManager = new BucketManager(TestConfig.testAuth, cfg);
            String bucket = entry.getKey()[0];
            String key = entry.getKey()[1];
            int code = entry.getValue();
            try {
                bucketManager.stat(bucket, key);
                Assert.fail();
            } catch (QiniuException e) {
                if (e.response != null) {
                    System.out.println(e.code() + "\n" + e.response.getInfo());
                }
                System.out.println(code + ",  " + e.code());
                Assert.assertEquals(code, e.code());
            }
        }
    }

    /**
     * 测试删除
     */
    @Test
    public void testDelete() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String copyKey = "delete" + Math.random();
                try {
                    bucketManager.copy(bucket, key, bucket, copyKey, true);
                    bucketManager.delete(bucket, copyKey);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
                }
            }
        });

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
            Configuration cfg = new Configuration(Zone.autoZone());
            cfg.useHttpsDomains = false;
            BucketManager bucketManager = new BucketManager(TestConfig.testAuth, cfg);
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

    /**
     * 测试移动/重命名
     */
    @Test
    public void testRename() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String renameFromKey = "renameFrom" + Math.random();
                String renameToKey = "renameTo" + key;
                try {
                    try {
                        bucketManager.delete(bucket, renameFromKey);
                    } catch (Exception ex) {
                        // do nothing
                    }
                    try {
                        bucketManager.delete(bucket, renameToKey);
                    } catch (Exception ex) {
                        // do nothing
                    }
                    bucketManager.copy(bucket, key, bucket, renameFromKey);
                    bucketManager.rename(bucket, renameFromKey, renameToKey);
                    bucketManager.delete(bucket, renameToKey);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + "==> " + e.response);
                } finally {
                    try {
                        bucketManager.delete(bucket, renameFromKey);
                    } catch (Exception ex) {
                        // do nothing
                    }
                    try {
                        bucketManager.delete(bucket, renameToKey);
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
            }
        });
    }

    /**
     * 测试复制
     */
    @Test
    public void testCopy() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {

                Response response;
                String bucket = file.getBucketName();
                String key = file.getKey();
                String copyToKey = "copyTo" + Math.random();
                try {
                    response = bucketManager.copy(bucket, key, bucket, copyToKey);
                    System.out.println(response.statusCode);
                    bucketManager.delete(bucket, copyToKey);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
                }
            }
        });
    }

    /**
     * 测试修改文件MimeType
     */
    @Test
    public void testChangeMime() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String mime = file.getMimeType();
                try {
                    bucketManager.changeMime(bucket, key, mime);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
                }
            }
        });
    }

    /**
     * 测试修改文件元信息
     */
    @Test
    public void testChangeHeaders() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
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
        });
    }

    /**
     * 测试镜像源
     */
    // TODO
//    @Test
    public void testPrefetch() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    bucketManager.setImage(file.getBucketName(), "https://developer.qiniu.com/");
                    // 有/image接口似乎有延迟，导致prefetch概率失败
                    // "Host":"iovip.qbox.me" "X-Reqid":"q2IAABkZC1dUxIkV"
                    // "Host":"iovip-na0.qbox.me" "X-Reqid":"XyMAAHgSE98nxYkV"
                    bucketManager.prefetch(file.getBucketName(), "kodo/sdk/1239/java");
                    bucketManager.unsetImage(file.getBucketName());
                } catch (QiniuException e) {
                    Assert.fail(file.getBucketName() + "==>" + e.response.toString());
                }
            }
        });
    }

    /**
     * 测试同步Fetch
     */
    @Test
    public void testFetch() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                // 雾存储不支持 fetch
                if (file.isFog()) {
                    return;
                }

                try {
                    String resUrl = "http://devtools.qiniu.com/qiniu.png";
                    String resKey = "qiniu.png";
                    String resHash = "FpHyF0kkil3sp-SaXXX8TBJY3jDh";
                    FetchRet fRet = bucketManager.fetch(resUrl, file.getBucketName(), resKey);
                    Assert.assertEquals(resHash, fRet.hash);

                    //no key specified, use hash as file key
                    fRet = bucketManager.fetch(resUrl, file.getBucketName());
                    Assert.assertEquals(resHash, fRet.hash);
                } catch (QiniuException e) {
                    e.printStackTrace();
                    // use e.response.toString() may get NullPointException
                    // when java.net.SocketTimeoutException: timeout
                    Assert.fail(e.getMessage());
                }
            }
        });
    }

    /**
     * 测试获取bucketinfo
     */
    @Test
    public void testBucketInfo() throws Exception {
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {
                    BucketInfo info = bucketManager.getBucketInfo(file.getBucketName());
                    Assert.assertNotNull("info is not null.", info);

                    bucketManager.getBucketInfo(TestConfig.dummyBucket);

                } catch (QiniuException e) {
                    Assert.assertEquals(612, e.response.statusCode);
                }
            }
        });
    }

    /**
     * 测试设置空间referer防盗链
     */
    @Test
    public void testPutReferAntiLeech() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                BucketReferAntiLeech leech = new BucketReferAntiLeech();
                Response response;
                BucketInfo bucketInfo;
                try {
                    // 测试白名单
                    leech.setMode(1);
                    leech.setPattern("www.qiniu.com");
                    leech.setAllowEmptyReferer(false);
                    System.out.println(leech.asQueryString());
                    response = bucketManager.putReferAntiLeech(file.getBucketName(), leech);
                    Assert.assertEquals(200, response.statusCode);
                    bucketInfo = bucketManager.getBucketInfo(file.getBucketName());
                    Assert.assertEquals(1, bucketInfo.getAntiLeechMode());
                    Assert.assertArrayEquals((new String[]{"www.qiniu.com"}), bucketInfo.getReferWhite());
                    Assert.assertEquals(false, bucketInfo.isNoRefer());

                    // 测试黑名单
                    leech.setMode(2);
                    leech.setPattern("www.baidu.com");
                    leech.setAllowEmptyReferer(true);
                    System.out.println(leech.asQueryString());
                    response = bucketManager.putReferAntiLeech(file.getBucketName(), leech);
                    Assert.assertEquals(200, response.statusCode);
                    bucketInfo = bucketManager.getBucketInfo(file.getBucketName());
                    Assert.assertEquals(2, bucketInfo.getAntiLeechMode());
                    Assert.assertArrayEquals((new String[]{"www.baidu.com"}), bucketInfo.getReferBlack());
                    Assert.assertEquals(true, bucketInfo.isNoRefer());

                    // 测试关闭
                    leech = new BucketReferAntiLeech();
                    System.out.println(leech.asQueryString());
                    response = bucketManager.putReferAntiLeech(file.getBucketName(), leech);
                    Assert.assertEquals(200, response.statusCode);
                    bucketInfo = bucketManager.getBucketInfo(file.getBucketName());
                    Assert.assertEquals(0, bucketInfo.getAntiLeechMode());
                    Assert.assertNull("ReferBlack should be Null", bucketInfo.getReferBlack());
                    Assert.assertNull("ReferWhtie should be Null", bucketInfo.getReferWhite());
                    Assert.assertEquals(false, bucketInfo.isNoRefer());

                } catch (Exception e) {
                    if (e instanceof QiniuException) {
                        QiniuException ex = (QiniuException) e;
                        Assert.fail(ex.response.toString());
                    }
                }
            }
        });
    }

    /**
     * 测试设置空间生命周期规则
     */
    @Test
    public void testBucketLifeCycleRule() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                Response response;
                BucketLifeCycleRule rule;
                BucketLifeCycleRule[] rules;
                String bucket = file.getBucketName();
                try {
                    // clear
                    clearBucketLifeCycleRule(bucket, bucketManager);

                    // 追加规则
                    rule = new BucketLifeCycleRule("a", "x");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketLifecycleRule(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // 更新规则（name不存在）
                    try {
                        rule = new BucketLifeCycleRule("x", "a");
                        response = bucketManager.updateBucketLifeCycleRule(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 更新规则
                    rule = new BucketLifeCycleRule("a", null);
                    System.out.println(rule.asQueryString());
                    response = bucketManager.updateBucketLifeCycleRule(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // 重复设置（name、prefix重复）
                    try {
                        bucketManager.putBucketLifecycleRule(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 重复设置（name重复）
                    try {
                        rule = new BucketLifeCycleRule("a", "b");
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketLifecycleRule(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 重复设置（prefix重复）
                    try {
                        rule = new BucketLifeCycleRule("b", null);
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketLifecycleRule(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 追加规则
                    rule = new BucketLifeCycleRule("b", "ab");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketLifecycleRule(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // 追加规则
                    rule = new BucketLifeCycleRule("c", "abc");
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketLifecycleRule(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // clear
                    clearBucketLifeCycleRule(bucket, bucketManager);

                    // 再获取规则
                    rules = bucketManager.getBucketLifeCycleRule(bucket);
                    Assert.assertEquals(0, rules.length);

                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    private void clearBucketLifeCycleRule(String bucket, BucketManager bucketManager) throws QiniuException {
        // 获取规则
        BucketLifeCycleRule[] rules = bucketManager.getBucketLifeCycleRule(bucket);
        try {
            for (BucketLifeCycleRule r : rules) {
                System.out.println("name=" + r.getName());
                System.out.println("prefix=" + r.getPrefix());
                Assert.assertNotEquals("x", r.getPrefix());
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
    public void testBucketEvent() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {

                String bucket = file.getBucketName();
                String key = file.getKey();
                Response response;
                BucketEventRule rule;
                BucketEventRule[] rules;
                try {
                    // clear
                    clearBucketEvent(bucket, bucketManager);

                    // 追加Event（invalid events）
                    try {
                        rule = new BucketEventRule("a", new String[]{}, new String[]{});
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketEvent(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 追加Event（error:callbackURL must starts with http:// or https://）
                    try {
                        rule = new BucketEventRule("a", new String[]{"put", "mkfile"}, new String[]{});
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketEvent(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 追加Event
                    rule = new BucketEventRule(
                            "a",
                            new String[]{"put", "mkfile", "delete", "copy", "move", "append", "disable",
                                    "enable", "deleteMarkerCreate"},
                            new String[]{"https://requestbin.fullcontact.com/1dsqext1?inspect",
                                    "https://requestbin.fullcontact.com/160bunp1?inspect"}
                    );
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);
                    System.out.println(response.url());
                    System.out.println(response.reqId);

                    // 重复追加Event（error:event name exists）
                    try {
                        rule.setName("a");
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketEvent(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 追加Event
                    rule.setName("b");
                    rule.setPrefix(key);
                    System.out.println(rule.asQueryString());
                    response = bucketManager.putBucketEvent(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // 重复追加Event（error:event prefix and suffix exists）
                    try {
                        rule.setName("b");
                        System.out.println(rule.asQueryString());
                        response = bucketManager.putBucketEvent(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 触发时间，回调成功与否 不检测
                    response = bucketManager.copy(bucket, key, bucket, key + "CopyByEvent", true);
                    Assert.assertEquals(200, response.statusCode);

                    // 更新Event（error:event name not found）
                    try {
                        rule.setName("c");
                        rule.setPrefix("c");
                        System.out.println(rule.asQueryString());
                        response = bucketManager.updateBucketEvent(bucket, rule);
                        Assert.fail();
                    } catch (QiniuException e) {
                        Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                    }

                    // 更新Event
                    rule.setName("b");
                    rule.setPrefix("c");
                    rule.setEvents(new String[]{"disable", "enable", "deleteMarkerCreate"});
                    System.out.println(rule.asQueryString());
                    response = bucketManager.updateBucketEvent(bucket, rule);
                    Assert.assertEquals(200, response.statusCode);

                    // clear
                    clearBucketEvent(bucket, bucketManager);

                    // 再获取Event
                    rules = bucketManager.getBucketEvents(bucket);
                    Assert.assertEquals(0, rules.length);

                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    private void clearBucketEvent(String bucket, BucketManager bucketManager) throws QiniuException {

        // 获取Event
        BucketEventRule[] rules = bucketManager.getBucketEvents(bucket);
        for (BucketEventRule r : rules) {
            System.out.println("name=" + r.getName());
            System.out.println("prefix=" + r.getPrefix());
            System.out.println("suffix=" + r.getSuffix());
            System.out.println("event=" + Arrays.asList(r.getEvents()).toString());
            System.out.println("callbackUrls=" + Arrays.asList(r.getCallbackUrls()).toString());
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
    public void testCorsRules() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
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
                        Assert.assertEquals(200, response.statusCode);
                        // 获取跨域规则
                        rules = bucketManager.getCorsRules(bucket);
                        bodyString2 = Json.encode(rules);
                        System.out.println(bodyString2);
                        Assert.assertEquals(bodyString, bodyString2);
                    }
                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    private CorsRule[] corsRules(CorsRule... rules) {
        return rules.clone();
    }

    /**
     * 测试设置回源规则
     */
    @Test
    // TODO
    public void testPutBucketSourceConfig() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                try {

                } catch (Exception e) {

                }
            }
        });
    }

    /**
     * 测试设置原图保护模式
     */
    @Test
    public void testPutBucketAccessStyleMode() throws Exception {

        final Random r = new Random();
        final String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                // 雾存储没有域名
                if (file.isFog()) {
                    return;
                }

                String bucket = file.getBucketName();
                String url = file.getTestUrl();
                System.out.println(bucket + "  -- " + url);
                Client client = new Client();
                Response response;
                try {
                    // 测试开启原图保护
                    response = bucketManager.putBucketAccessStyleMode(bucket, AccessStyleMode.OPEN);
                    System.out.println(response);
                    Assert.assertEquals(200, response.statusCode);

                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        response = client.get(url + "?v" + r.nextDouble());
                        Assert.fail(msg + url + "should be 401" + ": " + response.statusCode + " reqId:" + response.reqId);
                    } catch (QiniuException e) {
                        System.out.println(e.response);
                        System.out.println(e.response.statusCode);
                        Assert.assertEquals(msg + url, 401, e.response.statusCode);
                    }

                    // 测试关闭原图保护
                    response = bucketManager.putBucketAccessStyleMode(bucket, AccessStyleMode.CLOSE);
                    System.out.println(response);
                    Assert.assertEquals(msg + url, 200, response.statusCode);

                    // 关闭原图保护后，有一定延迟，直接访问会401 ...
                    //response = client.get(url + "?v" + r.nextDouble());
                    //Assert.assertEquals(msg + url, 200, response.statusCode);

                } catch (QiniuException e) {
                    e.printStackTrace();
                    Assert.fail(e.response.toString());
                } finally {
                    try {
                        bucketManager.putBucketAccessStyleMode(bucket, AccessStyleMode.CLOSE);
                    } catch (QiniuException e) {
                        try {
                            bucketManager.putBucketAccessStyleMode(bucket, AccessStyleMode.CLOSE);
                        } catch (QiniuException e1) {
                            // do nothing
                        }
                    }
                }
            }
        });
    }

    /**
     * 测试设置max-age属性
     */
    @Test
    public void testPutBucketMaxAge() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                final long[] maxAges = {Integer.MIN_VALUE, -54321, -1, 0, 1, 8, 1234567, 11111111, Integer.MAX_VALUE};
                try {
                    for (long maxAge : maxAges) {
                        // 设置max-age
                        Response response = bucketManager.putBucketMaxAge(bucket, maxAge);
                        Assert.assertEquals(200, response.statusCode);
                        // 获取max-age
                        BucketInfo bucketInfo = bucketManager.getBucketInfo(bucket);
                        long expect = maxAge;
                        long actual = bucketInfo.getMaxAge();
                        System.out.println("expect=" + expect);
                        System.out.println("actual=" + actual);
                        Assert.assertEquals(expect, actual);
                    }
                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    /**
     * 测试设置max-age属性<br>
     * UC有缓存，这种方法不合适于测试
     */
    @Deprecated
    public void testPutBucketMaxAge2() throws Exception {

        final String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";
        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String url = file.getTestUrl();
                final long[] maxAges = {Integer.MIN_VALUE, -54321, -1, 0, 1, 8, 1234567, 11111111, Integer.MAX_VALUE};
                try {
                    for (long maxAge : maxAges) {
                        // 设置max-age
                        System.out.println("maxAge=" + maxAge);
                        Response response = bucketManager.putBucketMaxAge(bucket, maxAge);
                        Assert.assertEquals(msg, 200, response.statusCode);
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
                        Assert.assertEquals(msg, "[public, max-age=" + expect + "]", actual);
                    }
                } catch (IOException e) {
                    if (e instanceof QiniuException) {
                        Assert.fail(msg + ((QiniuException) e).response.toString());
                    }
                }
            }
        });
    }

    /**
     * 测试设置空间私有化、公有化
     */
    @Test
    public void testPutBucketAccessMode() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                Response response;
                try {
                    // 测试转私有空间
                    response = bucketManager.putBucketAccessMode(bucket, AclType.PRIVATE);
                    Assert.assertEquals(200, response.statusCode);
                    BucketInfo info = bucketManager.getBucketInfo(bucket);
                    Assert.assertEquals(1, info.getPrivate());

                    // 测试转公有空间
                    response = bucketManager.putBucketAccessMode(bucket, AclType.PUBLIC);
                    Assert.assertEquals(200, response.statusCode);
                    info = bucketManager.getBucketInfo(bucket);
                    Assert.assertEquals(0, info.getPrivate());

                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }

                // 测试空间不存在情况
                try {
                    bucketManager.putBucketAccessMode(TestConfig.dummyBucket, AclType.PRIVATE);
                } catch (QiniuException e) {
                    Assert.assertEquals(631, e.response.statusCode);
                }
            }
        });
    }

    /**
     * 测试设置、获取空间配额
     */
    @Test
    public void testBucketQuota() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws Exception {
                String bucket = file.getBucketName();
                try {
                    testBucketQuota(bucketManager, bucket, -2, -2);
                } catch (QiniuException e) {
                    Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(400)));
                }
                try {
                    testBucketQuota(bucketManager, bucket, 0, 0);
                    testBucketQuota(bucketManager, bucket, 100, 100);
                    testBucketQuota(bucketManager, bucket, 0, 100);
                    testBucketQuota(bucketManager, bucket, 100, -1);
                    testBucketQuota(bucketManager, bucket, -1, -1);
                } catch (QiniuException e) {
                    Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
                }
            }
        });
    }

    private void testBucketQuota(BucketManager bucketManager, String bucket, long size, long count) throws Exception {
        Response response = bucketManager.putBucketQuota(bucket, new BucketQuota(size, count));
        Assert.assertEquals(200, response.statusCode);
        BucketQuota bucketQuota = bucketManager.getBucketQuota(bucket);
        if (size != 0) {
            Assert.assertEquals(size, bucketQuota.getSize());
        }
        if (count != 0) {
            Assert.assertEquals(count, bucketQuota.getCount());
        }
    }

    /**
     * 测试批量复制
     */
    @Test
    public void testBatchCopy() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
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
        });
    }

    /**
     * 测试批量移动
     */
    @Test
    public void testBatchMove() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
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
        });
    }

    /**
     * 测试批量重命名
     */
    @Test
    public void testBatchRename() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String renameFromKey = "renameFrom" + Math.random();
                try {
                    bucketManager.delete(bucket, renameFromKey);
                } catch (Exception e) {
                    // do nothing
                }
                try {
                    bucketManager.copy(bucket, key, bucket, renameFromKey);
                } catch (QiniuException e) {
                    try {
                        bucketManager.delete(bucket, renameFromKey);
                    } catch (Exception e1) {
                        // do nothing
                    }
                    Assert.fail(e.response.toString());
                }
                String renameToKey = "renameTo" + key;
                BucketManager.BatchOperations ops = new BucketManager.BatchOperations()
                        .addRenameOp(bucket, renameFromKey, renameToKey);
                try {
                    bucketManager.delete(bucket, renameToKey);
                } catch (QiniuException e) {
                    // do nothing
                }
                try {
                    Response r = bucketManager.batch(ops);
                    BatchStatus[] bs = r.jsonToObject(BatchStatus[].class);
                    System.out.println(bs[0].code);
                    Assert.assertTrue("200 or 298", batchStatusCode.contains(bs[0].code));
                } catch (QiniuException e) {
                    Assert.fail(e.response + "");
                } finally {
                    try {
                        bucketManager.delete(bucket, renameToKey);
                    } catch (QiniuException e) {
                        // do nothing
                    }
                    try {
                        bucketManager.delete(bucket, renameFromKey);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        });
    }

    /**
     * 测试批量stat
     */
    @Test
    public void testBatchStat() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
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
        });
    }

    /**
     * 测试批量修改文件MimeType
     */
    @Test
    public void testBatchChangeType() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String okey = file.getKey();
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
        });
    }

    /**
     * 测试批量操作
     */
    @Test
    public void testBatchCopyChgmDelete() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();

                //make 50 copies
                String[] keyArray = new String[50];
                for (int i = 0; i < keyArray.length; i++) {
                    keyArray[i] = String.format("%s-copy-%d", key, i);
                }

                BucketManager.BatchOperations ops = new BucketManager.BatchOperations();

                for (int i = 0; i < keyArray.length; i++) {
                    ops.addDeleteOp(bucket, keyArray[i]);
                }
                try {
                    bucketManager.batch(ops);
                } catch (Exception e) {
                    // do nothing
                }

                ops.clearOps();
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
                    ops.clearOps();
                    for (int i = 0; i < keyArray.length; i++) {
                        ops.addDeleteOp(bucket, keyArray[i]);
                    }
                    response = bucketManager.batch(ops);
                    Assert.assertTrue("200 or 298", batchStatusCode.contains(response.statusCode));
                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                } finally {
                    ops.clearOps();
                    for (int i = 0; i < keyArray.length; i++) {
                        ops.addDeleteOp(bucket, keyArray[i]);
                    }
                    try {
                        bucketManager.batch(ops);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        });
    }

    /**
     * 测试批量操作
     */
    @Test
    public void testBatch() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
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
        });
    }

    /**
     * 测试设置、取消空间镜像源
     */
    //TODO
    //@Test
    public void testSetAndUnsetImage() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
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
        });
    }

    /**
     * 测试文件生命周期
     */
    @Test
    public void testDeleteAfterDays() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String keyForDelete = "keyForDelete" + Math.random();
                try {
                    bucketManager.copy(bucket, key, bucket, keyForDelete);
                    Response response = bucketManager.deleteAfterDays(bucket, keyForDelete, 10);
                    Assert.assertEquals(200, response.statusCode);
                } catch (QiniuException e) {
                    Assert.fail(e.response.toString());
                }
            }
        });
    }

    /**
     * 测试修改文件类型
     */
    @Test
    public void testChangeFileType() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                // 雾存储不支持 changeType
                if (file.isFog()) {
                    return;
                }

                String bucket = file.getBucketName();
                String key = file.getKey();
                String keyToChangeType = "keyToChangeType" + Math.random();
                for (int i = 1; i < StorageType.values().length; i++) { // please begin with 1, not 0
                    StorageType storageType = StorageType.values()[i];
                    try {
                        bucketManager.copy(bucket, key, bucket, keyToChangeType, true);
                        Response response = bucketManager.changeType(bucket, keyToChangeType, storageType);
                        Assert.assertEquals(200, response.statusCode);
                        //stat
                        FileInfo fileInfo = bucketManager.stat(bucket, keyToChangeType);
                        Assert.assertEquals(storageType.ordinal(), fileInfo.type);
                        //delete the temp file
                        bucketManager.delete(bucket, keyToChangeType);
                    } catch (QiniuException e) {
                        Assert.fail(bucket + ":" + key + " > " + keyToChangeType + " >> "
                                + storageType + " ==> " + e.response.toString());
                    }
                }
            }
        });
    }

    /**
     * 测试解冻归档存储
     */
    @Test
    public void testRestoreArchive() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String keyToTest = "keyToChangeType" + Math.random();
                try {
                    // if stat, delete
                    try {
                        Response resp = bucketManager.statResponse(bucket, keyToTest);
                        if (resp.statusCode == 200) bucketManager.delete(bucket, keyToTest);
                    } catch (QiniuException ex) {
                        System.out.println("file " + keyToTest + " not exists, ok.");
                    }

                    // copy and changeType to Archive
                    bucketManager.copy(bucket, key, bucket, keyToTest, true);
                    Response response = bucketManager.changeType(bucket, keyToTest, StorageType.Archive);
                    Assert.assertEquals(200, response.statusCode);

                    // restoreArchive
                    response = bucketManager.restoreArchive(bucket, keyToTest, 1);
                    Assert.assertEquals(200, response.statusCode);

                    //test for 400 Bad Request {"error":"invalid freeze after days"}
                    try {
                        response = bucketManager.restoreArchive(bucket, keyToTest, 8);
                    } catch (QiniuException ex) {
                        Assert.assertEquals(400, ex.response.statusCode);
                        System.out.println(ex.response.bodyString());
                    }

                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + " > " + keyToTest + " >> " + e.response.toString());
                }
            }
        });
    }

    @Test
    public void testBatchRestoreArchive() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                String bucket = file.getBucketName();
                String key = file.getKey();
                String keyToTestPrefix = "batch_restore_archive";

                BucketManager.BatchOperations deleteOps = new BucketManager.BatchOperations();
                try {
                    List<String> keys = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        keys.add(keyToTestPrefix + "_" + i);
                    }

                    for (String keyToTest : keys) {
                        // if stat, delete
                        try {
                            Response resp = bucketManager.statResponse(bucket, keyToTest);
                            if (resp.statusCode == 200) bucketManager.delete(bucket, keyToTest);
                        } catch (QiniuException ex) {
                            System.out.println("file " + keyToTest + " not exists, ok.");
                        }
                    }

                    // copy and changeType to Archive
                    BucketManager.BatchOperations copyOps = new BucketManager.BatchOperations();
                    BucketManager.BatchOperations copyAfterArchiveOps = new BucketManager.BatchOperations();
                    BucketManager.BatchOperations changeTypeOps = new BucketManager.BatchOperations();
                    BucketManager.BatchOperations restoreArchiveOps = new BucketManager.BatchOperations();
                    for (String keyToTest : keys) {
                        copyOps.addCopyOp(bucket, key, bucket, keyToTest);
                        copyAfterArchiveOps.addCopyOp(bucket, keyToTest, bucket, keyToTest + "_copy");
                        deleteOps.addDeleteOp(bucket, keyToTest, keyToTest + "_copy");
                        changeTypeOps.addChangeTypeOps(bucket, StorageType.Archive, keyToTest);
                        restoreArchiveOps.addRestoreArchiveOps(bucket, 1, keyToTest);
                    }
                    Response copyResponse = bucketManager.batch(copyOps);
                    Assert.assertEquals(200, copyResponse.statusCode);

                    Response changeTypeResponse = bucketManager.batch(changeTypeOps);
                    Assert.assertEquals(200, changeTypeResponse.statusCode);

                    // 验证归档不可 copy
                    try {
                        Response copyAfterArchiveResponse = bucketManager.batch(copyAfterArchiveOps);
                        String bodyString = copyAfterArchiveResponse.bodyString();
                        Assert.assertNotEquals(200, "batch copy can't be success" + bodyString);
                    } catch (QiniuException ex) {
                        Assert.assertEquals(400, ex.response.statusCode);
                        System.out.println(ex.response.bodyString());
                    }

                    // restoreArchive
                    Response restoreResponse = bucketManager.batch(restoreArchiveOps);
                    String bodyString = restoreResponse.bodyString();
                    System.out.println(bodyString);
                    Assert.assertEquals(200, restoreResponse.statusCode);

                    //test for 400 Bad Request {"error":"invalid freeze after days"}
                    try {
                        restoreResponse = bucketManager.batch(restoreArchiveOps);
                        bodyString = restoreResponse.bodyString();
                        System.out.println(bodyString);
                    } catch (QiniuException ex) {
                        Assert.assertEquals(400, ex.response.statusCode);
                        System.out.println(ex.response.bodyString());
                    }

                    long checkStart = new Date().getTime();
                    boolean shouldCheck = true;
                    boolean success = false;
                    while (shouldCheck) {
                        // 验证解归档可 copy
                        try {
                            Response copyAfterArchiveResponse = bucketManager.batch(copyAfterArchiveOps);
                            bodyString = copyAfterArchiveResponse.bodyString();
                            System.out.println(bodyString);
                            // 可以 copy 但文件已存在
                            if (bodyString.contains("\"code\":614")) {
                                success = true;
                                break;
                            }
                        } catch (QiniuException ex) {
                            System.out.println(ex.response.bodyString());
                            if (ex.response.statusCode == 400) {
                                success = true;
                                break;
                            }
                        }

                        long current = new Date().getTime();
                        if (current - checkStart > 1000 * 60 * 5.5) {
                            shouldCheck = false;
                        }

                        try {
                            Thread.sleep(1000 * 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Assert.assertTrue("can copy after restore archive", success);
                } catch (QiniuException e) {
                    Assert.fail(bucket + ":" + key + " > " + keyToTestPrefix + " >> " + e.response.toString());
                } finally {
                    try {
                        Response response = bucketManager.batch(deleteOps);
                        String bodyString = response.bodyString();
                        System.out.println(bodyString);
                        Assert.assertTrue(bodyString, response.statusCode == 200 || response.statusCode == 298);
                    } catch (QiniuException ex) {
                        Assert.assertEquals(400, ex.response.statusCode);
                        System.out.println(ex.response.bodyString());
                    }
                }
            }
        });
    }

    /**
     * 测试noIndexPage
     *
     * @throws QiniuException
     */
    @Test
    public void testIndexPage() throws Exception {

        testFileWithHandler(new TestFileHandler() {
            @Override
            public void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws IOException {
                bucketManager.setIndexPage(file.getBucketName(), IndexPageType.HAS);
                BucketInfo info = bucketManager.getBucketInfo(file.getBucketName());
                Assert.assertEquals(0, info.getNoIndexPage());

                bucketManager.setIndexPage(file.getBucketName(), IndexPageType.NO);
                info = bucketManager.getBucketInfo(file.getBucketName());
                Assert.assertEquals(1, info.getNoIndexPage());
            }
        });
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


    private void testFileWithHandler(TestFileHandler handler) throws Exception {
        if (handler == null) {
            return;
        }

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            Configuration cfg = new Configuration(file.getRegion());
            BucketManager bucketManager = new BucketManager(TestConfig.testAuth, cfg);
            handler.testFile(file, bucketManager);
        }
    }

    private interface TestFileHandler {
        void testFile(TestConfig.TestFile file, BucketManager bucketManager) throws Exception;
    }
}
