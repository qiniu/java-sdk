package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.storage.*;
import com.qiniu.util.Timestamp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiQueryRegionTest {

    @Test
    @Tag("IntegrationTest")
    public void testQ() {
        Api.Request request = new Api.Request("https://123.com:80/a/b/c?d=e");
        System.out.println(request);
    }

    @Test
    @Tag("IntegrationTest")
    public void testQuery() {

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile testFile : files) {
            String bucket = testFile.getBucketName();
            String key = testFile.getKey();
            String token = TestConfig.testAuth.uploadToken(bucket, key, 3600, null);

            Configuration configuration = new Configuration();
            Client client = new Client(configuration);
            ApiQueryRegion api = new ApiQueryRegion(client);
            ApiQueryRegion.Request request = new ApiQueryRegion.Request(null, token);
            try {
                ApiQueryRegion.Response response = api.request(request);
                System.out.println("query region:" + response.getResponse());
                System.out.println("query region data:" + response.getDataMap());

                assertTrue(response.isOK(), response.getResponse() + "");

                String regionId = response.getDefaultRegionId();
                System.out.println("query region regionId:" + regionId);
                assertNotNull(regionId, response.getDataMap() + "");

                Long ttl = response.getDefaultRegionTTL();
                System.out.println("query region ttl:" + ttl);
                assertNotNull(ttl, response.getDataMap() + "");

                List<String> upHosts = response.getDefaultRegionUpHosts();
                System.out.println("query region upHosts:" + upHosts);
                assertTrue(upHosts.size() > 0, response.getDataMap() + "");
            } catch (QiniuException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testQueryWithRetry() {

        int retryMax = 2;
        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile testFile : files) {
            String bucket = testFile.getBucketName();
            String accessKey = TestConfig.testAuth.accessKey;

            Configuration configuration = new Configuration();
            Client client = new Client(configuration);
            ApiQueryRegion api = new ApiQueryRegion(client, new Api.Config.Builder()
                    .setRequestDebugLevel(ApiInterceptorDebug.LevelPrintDetail)
                    .setResponseDebugLevel(ApiInterceptorDebug.LevelPrintDetail)
                    .setHostRetryMax(retryMax)
                    .setRetryInterval(1000)
                    .setSingleHostRetryMax(retryMax)
                    .setHostProvider(HostProvider.ArrayProvider("mock.uc.com", Configuration.defaultUcHost))
                    .build());
            ApiQueryRegion.Request request = new ApiQueryRegion.Request("https://mock.uc.com", accessKey, bucket);

            try {
                long st = Timestamp.second();
                ApiQueryRegion.Response response = api.request(request);
                System.out.println("query region:" + response.getResponse());
                System.out.println("query region data:" + response.getDataMap());
                long en = Timestamp.second();

                assertEquals(en - st, retryMax, "retry interval or retry max error");

                assertTrue(response.isOK(), response.getResponse() + "");

                String regionId = response.getDefaultRegionId();
                System.out.println("query region regionId:" + regionId);
                assertNotNull(regionId, response.getDataMap() + "");

                Long ttl = response.getDefaultRegionTTL();
                System.out.println("query region ttl:" + ttl);
                assertNotNull(ttl, response.getDataMap() + "");

                List<String> upHosts = response.getDefaultRegionUpHosts();
                System.out.println("query region upHosts:" + upHosts);
                assertTrue(upHosts.size() > 0, response.getDataMap() + "");
            } catch (QiniuException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }
}
