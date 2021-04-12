package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.storage.ApiQueryRegion;
import com.qiniu.storage.Configuration;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.util.List;

public class ApiQueryRegionTest {

    @Test
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

                Assert.assertTrue(response.getResponse() + "", response.isOK());

                String regionId = response.getDefaultRegionId();
                System.out.println("query region regionId:" + regionId);
                Assert.assertNotNull(response.getDataMap() + "", regionId);

                Long ttl = response.getDefaultRegionTTL();
                System.out.println("query region ttl:" + ttl);
                Assert.assertNotNull(response.getDataMap() + "", ttl);

                List<String> upHosts = response.getDefaultRegionUpHosts();
                System.out.println("query region upHosts:" + upHosts);
                Assert.assertTrue(response.getDataMap() + "", upHosts.size() > 0);
            } catch (QiniuException e) {
                e.printStackTrace();
            }
        }
    }
}
