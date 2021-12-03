package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.storage.ApiQueryRegion;
import com.qiniu.storage.Configuration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class ApiQueryRegionTest {

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
            }
        }
    }
}
