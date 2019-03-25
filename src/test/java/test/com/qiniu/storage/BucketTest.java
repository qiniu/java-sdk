package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Region;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.*;
import com.qiniu.util.StringUtils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BucketTest {
    private BucketManager bucketManager;
    private BucketManager dummyBucketManager;

    List<Integer> batchStatusCode = Arrays.asList(200, 298);

    /**
     * 初始化
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Configuration cfg = new Configuration(Region.autoRegion());
        this.bucketManager = new BucketManager(TestConfig.testAuth, cfg);
        this.dummyBucketManager = new BucketManager(TestConfig.dummyAuth, new Configuration());
    }

    /**
     * 测试列举空间名
     * 检测默认空间是否在返回列表中
     * 检测401
     */
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

    /**
     * 测试列举空间域名
     * 检测结果是否为null
     */
    @Test
    public void testDomains() {
        try {
            String[] domains = bucketManager.domainList(TestConfig.testBucket_z0);
            Assert.assertNotNull(domains);
        } catch (QiniuException e) {
            Assert.assertEquals(401, e.code());
        }
    }

    /**
     * 测试list接口，limit=2
     * 检测返回结果items、marker是否为Null
     */
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

    /**
     * 测试list接口的delimiter
     * 检测结果是否符合预期
     */
    @Test
    public void testListUseDelimiter() {
        try {
            Map<String, String> bucketKeyMap = new HashMap<String, String>();
            bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
            bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);

            for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
                String bucket = entry.getKey();
                String key = entry.getValue();
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/1/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/2/" + key, true);
                bucketManager.copy(bucket, key, bucket, "testListUseDelimiter/3/" + key, true);
                FileListing l = bucketManager.listFiles(bucket, "testListUseDelimiter", null, 10, "/");
                Assert.assertEquals(1, l.commonPrefixes.length);
            }
        } catch (QiniuException e) {
            Assert.fail(e.response.toString());
        }
    }

    /**
     * 测试文件迭代器
     */
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

    /**
     * 测试文件迭代器
     */
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

    /**
     * 测试stat
     * 检测正常情况返回值hash、mimeType
     * 检测bucket或key不存在情况返回值
     */
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

    /**
     * 测试删除
     * 检测正常情况
     * 检测bucket或key不存在情况返回值
     */
    @Test
    public void testDelete() {
        Map<String, String> bucketKeyMap = new HashMap<String, String>();
        bucketKeyMap.put(TestConfig.testBucket_z0, TestConfig.testKey_z0);
        bucketKeyMap.put(TestConfig.testBucket_na0, TestConfig.testKey_na0);
        for (Map.Entry<String, String> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            String key = entry.getValue();
            String copyKey = "delete" + Math.random();
            try {
                bucketManager.copy(bucket, key, bucket, copyKey, true);
                bucketManager.delete(bucket, copyKey);
            } catch (QiniuException e) {
                Assert.fail(bucket + ":" + key + "==> " + e.response.toString());
            }
        }

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

    /**
     * 测试移动/重命名
     * 检测正常情况
     */
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

    /**
     * 测试复制
     * 检测正常情况
     */
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

    /**
     * 测试修改文件MimeType
     * 检测正常情况
     */
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

    /**
     * 测试修改文件元信息
     * 检测正常情况
     */
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

    /**
     * 测试镜像源
     * 检测设置镜像源
     * 检测镜像资源更新
     * 检测取消镜像源
     */
    // TODO
    //@Test
    public void testPrefetch() {
        String[] buckets = new String[]{TestConfig.testBucket_z0, TestConfig.testBucket_na0};
        for (String bucket : buckets) {
            try {
                bucketManager.setImage(bucket, "https://developer.qiniu.com/");
                // 有/image接口似乎有延迟，导致prefetch概率失败
                // "Host":"iovip.qbox.me" "X-Reqid":"q2IAABkZC1dUxIkV"
                // "Host":"iovip-na0.qbox.me" "X-Reqid":"XyMAAHgSE98nxYkV"
                bucketManager.prefetch(bucket, "kodo/sdk/1239/java");
                bucketManager.unsetImage(bucket);
            } catch (QiniuException e) {
                Assert.fail(bucket + "==>" + e.response.toString());
            }
        }
    }

    /**
     * 测试同步Fetch
     * 检测正常情况
     */
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
    
    /**
     * 测试获取bucketinfo
     * @throws QiniuException
     */
    @Test
    public void testBucketInfo() throws QiniuException {
        try {
            BucketInfo info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
            System.out.println(info.getRegion());
            System.out.println(info.getPrivate());
            Assert.assertNotNull("info.getRegion() is not null.", info.getRegion());
            Assert.assertNotNull("info.getPrivate() is not null.", info.getPrivate());
            
            bucketManager.getBucketInfo(TestConfig.dummyBucket);
            
        } catch (QiniuException e) {
        	e.printStackTrace();
        	Assert.assertEquals(612, e.response.statusCode);
        }
    }
    
    /**
     * 测试设置空间referer防盗链
     */
    @Test
 // TODO
    public void testPutReferAntiLeech() {
    	BucketReferAntiLeech leech = new BucketReferAntiLeech();
    	Response response;
    	try {
    		System.out.println(leech.asQueryString());
    		response = bucketManager.putReferAntiLeech(TestConfig.testBucket_z0, leech);
    		Assert.assertEquals(200, response.statusCode);
    		
    		leech.setAllowEmptyReferer(false);
    		System.out.println(leech.asQueryString());
    		response = bucketManager.putReferAntiLeech(TestConfig.testBucket_z0, leech);
    		Assert.assertEquals(200, response.statusCode);
    		
    		leech.setAllowEmptyReferer(false);
    		leech.setMode(1);
    		leech.setPattern("www.qiniu.com");
    		System.out.println(leech.asQueryString());
    		response = bucketManager.putReferAntiLeech(TestConfig.testBucket_z0, leech);
    		Assert.assertEquals(200, response.statusCode);
    		System.out.println(response.url());
    		System.out.println(response.reqId);
    		
		} catch (Exception e) {
			if (e instanceof QiniuException) {
				QiniuException ex = (QiniuException) e;
				Assert.fail(ex.response.toString());
			}
		}
    }
    
    /**
     * 测试设置空间生命周期规则
     */
    @Test
    // TODO
    public void testBucketLifeCycleRule() {
    	try {
    		
    	} catch (Exception e) {
    		
    	}
    }
    
    /**
     * 测试设置事件通知
     */
    @Test
    // TODO
    public void testBucketEvent() {
    	try {
    		
    	} catch (Exception e) {
    		
    	}
    }
    
    /**
     * 测试跨域规则
     */
    @Test
    // TODO
    public void testCorsRules() {
    	try {
    		
    	} catch (Exception e) {
    		
    	}
    }
    
    /**
     * 测试设置回源规则
     */
    @Test
    // TODO
    public void testPutBucketSourceConfig() {
    	try {
    		
    	} catch (Exception e) {
    		
    	}
    }
    
    /**
     * 测试设置原图保护模式
     * 检测开启原图保护能否200，检测主页能否返回401
     * 检测关闭原图保护能否200，检测主页能否返回200
     */
    @Test
    public void testPutBucketAccessStyleMode() {
    	Client client = new Client();
    	Response response;
    	try {
    		response = bucketManager.putBucketAccessStyleMode(TestConfig.testBucket_z0, AccessStyleMode.OPEN);
    		Assert.assertEquals(200, response.statusCode);
    		
    		try {
        		client.get(TestConfig.testUrl_z0);
    		} catch (QiniuException e) {
    			e.printStackTrace();
    			Assert.assertEquals(401, e.response.statusCode);
    		}
    		
    		response = bucketManager.putBucketAccessStyleMode(TestConfig.testBucket_z0, AccessStyleMode.CLOSE);
    		Assert.assertEquals(200, response.statusCode);
    		
    		response = client.get(TestConfig.testUrl_z0);
    		Assert.assertEquals(200, response.statusCode);
    		
    	} catch (QiniuException e) {
    		Assert.fail(e.response.toString());
		}
    }
    
    /**
     * 测试设置max-age属性
     */
    @Test
    // TODO
    public void testPutBucketMaxAge() {
    	Client client = new Client();
    	Response response;
    	try {
    		long maxAges[] = {Integer.MIN_VALUE, -54321, -1, 0, 1, 8, 1234567, Integer.MAX_VALUE};
    		for (int i = 0; i < maxAges.length; i ++) {
    			long maxAge = maxAges[i];
    			System.out.println("maxAge=" + maxAge);
    			response = bucketManager.putBucketMaxAge(TestConfig.testBucket_z0, maxAge);
    			Assert.assertEquals(200, response.statusCode);
    			
        		response = client.get(TestConfig.testUrl_z0);
        		String value = respHeader(TestConfig.testUrl_z0, "Cache-Control");
        		System.out.println(value);
//        		if (maxAge <= 0) {
//        			Assert.assertEquals(31536000, value);
//        		} else {
//        			Assert.assertEquals(maxAge, value);
//        		}
    		}
    	} catch (IOException e) {
    		if (e instanceof QiniuException) {
    			Assert.fail(((QiniuException) e).response.toString());
    		}
    	}
    }
    
    /**
     * 测试设置空间私有化、公有化
     */
    @Test
    public void testPutBucketAccessMode() {
    	Response response;
        try {
        	response = bucketManager.putBucketAccessMode(TestConfig.testBucket_z0, AclType.PRIVATE);
        	Assert.assertEquals(200, response.statusCode);
            BucketInfo info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
            Assert.assertEquals(1, info.getPrivate());

            response = bucketManager.putBucketAccessMode(TestConfig.testBucket_z0, AclType.PUBLIC);
            Assert.assertEquals(200, response.statusCode);
            info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
            Assert.assertEquals(0, info.getPrivate());
        } catch (QiniuException e) {
        	Assert.fail(e.response.toString());
        }
        try {
        	bucketManager.putBucketAccessMode(TestConfig.dummyBucket, AclType.PRIVATE);
        } catch (QiniuException e) {
            Assert.assertEquals(631, e.response.statusCode);
        }
    }
    
    /**
     * 测试设置、获取空间配额
     */
    @Test
    public void testBucketQuota() {
    	BucketQuota bucketQuota;
    	Response response;
    	try {
    		response = bucketManager.putBucketQuota(TestConfig.testBucket_z0, new BucketQuota(-1, -1));
    		Assert.assertEquals(200, response.statusCode);
    		bucketQuota = bucketManager.getBucketQuota(TestConfig.testBucket_z0);
    		Assert.assertEquals(-1, bucketQuota.getSize());
    		Assert.assertEquals(-1, bucketQuota.getCount());
    		
    		// TODO
    		
    		/**
    		 * https://jira.qiniu.io/browse/KODO-7233
    		 * 修复BUG ing
    		 */
    		//response = bucketManager.putBucketQuota(TestConfig.testBucket_z0, new BucketQuota(0, 0));
    		//Assert.assertEquals(200, response.statusCode);
    		
    	} catch (QiniuException e) {
    		Assert.fail(e.response.toString());
		}
    }

    /**
     * 测试批量复制
     */
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

    /**
     * 测试批量移动
     */
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

    /**
     * 测试批量重命名
     */
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

    /**
     * 测试批量stat
     */
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

    /**
     * 测试批量修改文件MimeType
     */
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

    /**
     * 测试批量操作
     */
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

    /**
     * 测试批量操作
     */
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

    /**
     * 测试设置、取消空间镜像源
     */
    //TODO
    //@Test
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

    /**
     * 测试文件生命周期
     */
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

    /**
     * 测试修改文件类型
     */
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

    /**
<<<<<<< HEAD
=======
     * 测试设置空间私有化、公有化
     *
     * @throws QiniuException
     */
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
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试noIndexPage
     *
     * @throws QiniuException
     */
    @Test
    public void testIndexPage() throws QiniuException {
        bucketManager.setIndexPage(TestConfig.testBucket_z0, IndexPageType.HAS);
        BucketInfo info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
        Assert.assertEquals(0, info.getNoIndexPage());

        bucketManager.setIndexPage(TestConfig.testBucket_z0, IndexPageType.NO);
        info = bucketManager.getBucketInfo(TestConfig.testBucket_z0);
        Assert.assertEquals(1, info.getNoIndexPage());

        try {
            bucketManager.setIndexPage(TestConfig.dummyBucket, IndexPageType.HAS);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获得响应头中key字段的值
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
