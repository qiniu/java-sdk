package test.com.qiniu;

import com.qiniu.common.AutoZone;
import com.qiniu.common.Zone;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;

public final class TestConfig {

    //dummy: ak, sk, ...
    public static final String dummyAccessKey = "abcdefghklmnopq";
    public static final String dummySecretKey = "1234567890";
    public static final Auth dummyAuth = Auth.create(dummyAccessKey, dummySecretKey);
    public static final String dummyBucket = "no-such-dummy-bucket";
    public static final String dummyKey = "no-such-dummy-key";
    public static final String dummyDomain = "dummy.qiniudn.com";
    public static final String dummyUptoken = "ak:token:putpolicy";
    public static final String dummyInvalidUptoken = "invalidtoken";

    //test: ak, sk, auth
    public static final String testAccessKey = System.getenv("QINIU_ACCESS_KEY");
    public static final String testSecretKey = System.getenv("QINIU_SECRET_KEY");
    // 内部测试环境 AK/SK
    public static final String innerAccessKey = System.getenv("testAK");
    public static final String innerSecretKey = System.getenv("testSK");

    //sms: ak, sk, auth
    public static final String smsAccessKey = "test";
    public static final String smsSecretKey = "test";

    public static final String testDefaultKey = "do_not_delete/1.png";
    public static final String getTestDefaultMp4FileKey = "do_not_delete/1.mp4";
    public static final String testMp4FileKey = "do_not_delete/1.mp4";

    //z0
    public static final String testBucket_z0 = "javasdk";
    public static final String testKey_z0 = "do_not_delete/1.png";
    public static final String testDomain_z0 = "javasdk.peterpy.cn";
    public static final String testUrl_z0 = "http://" + testDomain_z0 + "/" + testKey_z0;
    public static final String testDomain_z0_timeStamp = "javasdk-timestamp.peterpy.cn";
    public static final String testUrl_z0_timeStamp = "http://" + testDomain_z0_timeStamp + "/" + testKey_z0;

    public static final String testPipeline = "sdktest";
    //na0
    public static final String testBucket_na0 = "java-sdk-na0";
    public static final String testKey_na0 = "do_not_delete/1.png";
    public static final String testChineseKey_na0 = "do_not_delete/水 果-iphone.png";
    public static final String testDomain_na0 = "javasdk-na0.peterpy.cn";
    public static final String testDomain_na0_timeStamp = "javasdk-na0-timestamp.peterpy.cn";
    public static final String testUrl_na0 = "http://" + testDomain_na0 + "/" + testKey_na0;
    public static final String testPrivateKey_na0 = "水果.png";
    public static final String testPrivateBucket_na0 = "private-na0";
    public static final String testPrivateBucketDomain_na0 = "private-na0.sdk.qiniu-solutions.com";

    //sg
    public static final String testBucket_as0 = "sdk-as0";
    //code
    public static final int ERROR_CODE_BUCKET_NOT_EXIST = 631;
    public static final int ERROR_CODE_KEY_NOT_EXIST = 612;
    public static final String testLinkingAppid = System.getenv("QINIU_LINKING_APPID");
    public static Auth testAuth = Auth.create(testAccessKey, testSecretKey);

    private TestConfig() {
    }

    public static boolean isTravis() {
        return "travis".equals(System.getenv("QINIU_TEST_ENV"));
    }

    public static TestFile[] getTestFileArray() {
        return getTestFileArray(testDefaultKey, "image/png");
    }

    public static TestFile[] getTestFileArray(String fileSaveKey, String fileMimeType) {
        if (StringUtils.isNullOrEmpty(fileSaveKey)) {
            fileSaveKey = testDefaultKey;
        }
        if (StringUtils.isNullOrEmpty(fileMimeType)) {
            fileMimeType = "application/octet-stream";
        }

        TestFile na0 = new TestFile();
        na0.key = fileSaveKey;
        na0.mimeType = fileMimeType;
        na0.bucketName = testBucket_na0;
        na0.testDomain = testDomain_na0;
        na0.testUrl = "http://" + testDomain_na0 + "/" + fileSaveKey;
        na0.testDomainTimeStamp = testDomain_na0_timeStamp;
        na0.testUrlTimeStamp = "http://" + testDomain_na0_timeStamp + "/" + fileSaveKey;
        na0.regionId = "na0";
        na0.region = Region.regionNa0();

        TestFile z0 = new TestFile();
        z0.key = fileSaveKey;
        z0.mimeType = fileMimeType;
        z0.bucketName = testBucket_z0;
        z0.testDomain = testDomain_z0;
        z0.testUrl = "http://" + testDomain_z0 + "/" + fileSaveKey;
        z0.testDomainTimeStamp = testDomain_z0_timeStamp;
        z0.testUrlTimeStamp = "http://" + testDomain_z0_timeStamp + "/" + fileSaveKey;
        z0.regionId = "z0";
        z0.region = Region.region0();

        TestFile z0_auto = new TestFile();
        z0_auto.key = fileSaveKey;
        z0_auto.mimeType = fileMimeType;
        z0_auto.bucketName = testBucket_z0;
        z0_auto.testDomain = testDomain_z0;
        z0_auto.testUrl = "http://" + testDomain_z0 + "/" + fileSaveKey;
        z0_auto.testDomainTimeStamp = testDomain_z0_timeStamp;
        z0_auto.testUrlTimeStamp = "http://" + testDomain_z0_timeStamp + "/" + fileSaveKey;
        z0_auto.regionId = "z0";
        z0_auto.region = Region.region0();

        TestFile fog = new TestFile();
        fog.key = fileSaveKey;
        fog.mimeType = fileMimeType;
        fog.bucketName = "java-sdk-fog-cn-east1";
//        fog.testDomain = "javasdk-fog-cn-east1.peterpy.cn";
//        fog.testUrl = "http://" + fog.testDomain + "/" + fileSaveKey;
//        fog.testDomainTimeStamp = "javasdk-fog-cn-east1-timestamp.peterpy.cn";
//        fog.testUrlTimeStamp = "http://" + fog.testDomainTimeStamp + "/" + fileSaveKey;
        fog.regionId = "fog-cn-east-1";
        fog.region = Region.regionFogCnEast1();

        TestFile fog1 = new TestFile();
        fog1.key = fileSaveKey;
        fog1.mimeType = fileMimeType;
        fog1.bucketName = "java-sdk-fog-cn-east1";
//        fog1.testDomain = "javasdk-fog-cn-east1.peterpy.cn";
//        fog1.testUrl = "http://" + fog.testDomain + "/" + fileSaveKey;
//        fog1.testDomainTimeStamp = "javasdk-fog-cn-east1-timestamp.peterpy.cn";
//        fog1.testUrlTimeStamp = "http://" + fog.testDomainTimeStamp + "/" + fileSaveKey;
        fog1.regionId = "fog-cn-east-1";
        fog1.region = toRegion(Zone.zoneFogCnEast1());

        if (isTravis()) {
            return new TestFile[]{na0};
        } else {
//            return new TestFile[]{fog, fog1, z0, na0};
            return new TestFile[]{na0, z0_auto};
        }
    }

    private static Region toRegion(Zone zone) {
        if (zone instanceof AutoZone) {
            AutoZone autoZone = (AutoZone) zone;
            return Region.autoRegion(autoZone.ucServer);
        }
        return new Region.Builder()
                .region(zone.getRegion())
                .accUpHost(getHosts(zone.getUpHttps(null), zone.getUpHttp(null)))
                .srcUpHost(getHosts(zone.getUpBackupHttps(null), zone.getUpBackupHttp(null)))
                .iovipHost(getHost(zone.getIovipHttps(null), zone.getIovipHttp(null)))
                .rsHost(getHost(zone.getRsHttps(), zone.getRsHttp()))
                .rsfHost(getHost(zone.getRsfHttps(), zone.getRsfHttp()))
                .apiHost(getHost(zone.getApiHttps(), zone.getApiHttp()))
                .build();
    }

    private static String getHost(String https, String http) {
        return toDomain(https);
    }

    private static String[] getHosts(String https, String http) {
        // http, s1 would not be null
        String s1 = toDomain(http);
        String s2 = toDomain(https);
        if (s2 != null && !s2.equalsIgnoreCase(s1)) {
            return new String[]{s1, s2};
        }
        return new String[]{s1};
    }

    private static String toDomain(String d1) {
        if (StringUtils.isNullOrEmpty(d1)) {
            return null;
        }
        int s = d1.indexOf("://");
        if (s > -1) {
            return d1.substring(s + 3);
        }
        return d1;
    }

    public static class TestFile {
        // 文件名
        String key;
        // 文件 mimeType
        String mimeType;
        // 文件所在 bucket 名
        String bucketName;
        // 测试 url
        String testUrl;
        // 测试带时间戳的 url
        String testUrlTimeStamp;
        // 测试 domain
        String testDomain;
        // 测试带时间戳的 domain
        String testDomainTimeStamp;
        // region id
        String regionId;
        // 文件所在 region
        Region region;

        public String getKey() {
            return key;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getBucketName() {
            return bucketName;
        }

        public String getTestUrl() {
            return testUrl;
        }

        public String getTestUrlTimeStamp() {
            return testUrlTimeStamp;
        }

        public String getTestDomain() {
            return testDomain;
        }

        public String getTestDomainTimeStamp() {
            return testDomainTimeStamp;
        }

        public String getRegionId() {
            return regionId;
        }

        public Region getRegion() {
            return region;
        }

        public boolean isFog() {
            return regionId.equals("fog-cn-east-1");
        }
    }

}
