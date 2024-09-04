package test.com.qiniu.iam.apis;

import com.qiniu.storage.Api;
import test.com.qiniu.TestConfig;

public class ApiTestConfig {
    static final String groupAlias = "JavaTestGroup";
    static final String userAlias = "JavaTestUser";
    static final String userPWD = "JavaTestUserPWD";
    static final String baseUrl = "api.qiniu.com";
    static final Api.Config config = new Api.Config.Builder()
            .setAuth(TestConfig.testAuth)
            .setRequestDebugLevel(Api.Config.DebugLevelDetail)
            .setResponseDebugLevel(Api.Config.DebugLevelDetail)
            .build();
}
