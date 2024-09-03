package test.com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.iam.apis.*;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

public class UserApiTest {

    @Test
    @Tag("IntegrationTest")
    public void testUsers() {
        String userAlias = "JavaTestUser";
        String userPWD = "JavaTestUserPWD";
        String baseUrl = "api.qiniu.com";
        Api.Config config = new Api.Config.Builder()
                .setAuth(TestConfig.testAuth)
                .setRequestDebugLevel(Api.Config.DebugLevelDetail)
                .setResponseDebugLevel(Api.Config.DebugLevelDetail)
                .build();

        // 先删除，流程开始先清理历史数据
        ApiDeleteUser.Request deleteRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
        ApiDeleteUser deleteApi = new ApiDeleteUser(null, config);
        try {
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
            e.printStackTrace();
        }

        try {
            // 1. 创建
            ApiCreateUser.Request.CreateIamUserParam createParam = new ApiCreateUser.Request.CreateIamUserParam();
            createParam.setAlias(userAlias);
            createParam.setPassword(userPWD);
            ApiCreateUser.Request createRequest = new ApiCreateUser.Request(baseUrl, createParam);
            ApiCreateUser createApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createResponse = createApi.request(createRequest);
            assertNotNull(createResponse, "1. 创建 User 失败：" + createResponse);
            assertEquals(createResponse.getResponse().statusCode, 200, "1.1 创建 User 失败：" + createResponse);
            ApiCreateUser.Response.CreatedIamUserData createdUserData = createResponse.getData().getData();
            assertEquals(createdUserData.getAlias(), userAlias, "1.2 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getId(), "1.3 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getRootUid(), "1.4 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getIuid(), "1.5 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getAlias(), "1.6 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getCreatedAt(), "1.7 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getUpdatedAt(), "1.8 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getLastLoginTime(), "1.9 创建 User 失败：" + createResponse);
            assertNotNull(createdUserData.getEnabled(), "1.10 创建 User 失败：" + createResponse);

            // 2. 获取某个
            ApiGetUser.Request getRequest = new ApiGetUser.Request(baseUrl, userAlias);
            ApiGetUser getApi = new ApiGetUser(null, config);
            ApiGetUser.Response getResponse = getApi.request(getRequest);
            assertNotNull(getResponse, "2. 获取 User 失败：" + getResponse);
            assertEquals(createResponse.getResponse().statusCode, 200, "2.1 获取 User 失败：" + getResponse);
            ApiGetUser.Response.GetIamUserData getUserData = getResponse.getData().getData();
            assertEquals(getUserData.getAlias(), userAlias, "2.2 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getId(), "2.3 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getRootUid(), "2.4 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getIuid(), "2.5 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getAlias(), "2.6 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getCreatedAt(), "2.7 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getUpdatedAt(), "2.8 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getLastLoginTime(), "2.9 获取 User 失败：" + getResponse);
            assertNotNull(getUserData.getEnabled(), "2.10 获取 User 失败：" + getResponse);
            assertEquals(getUserData.getId(), createdUserData.getId(), "2.11 获取 User 失败：" + getResponse);
            assertEquals(getUserData.getRootUid(), createdUserData.getRootUid(), "2.12 获取 User 失败：" + getResponse);
            assertEquals(getUserData.getIuid(), createdUserData.getIuid(), "2.13 获取 User 失败：" + getResponse);
            assertEquals(getUserData.getAlias(), createdUserData.getAlias(), "2.14 获取 User 失败：" + getResponse);
            assertEquals(getUserData.getEnabled(), createdUserData.getEnabled(), "2.15 获取 User 失败：" + getResponse);

            // 3. 修改
            ApiModifyUser.Request.ModifyIamUserParam modifyParam = new ApiModifyUser.Request.ModifyIamUserParam();
            modifyParam.setEnabled(false);
            ApiModifyUser.Request modifyRequest = new ApiModifyUser.Request(baseUrl, userAlias, modifyParam);
            ApiModifyUser modifyApi = new ApiModifyUser(null, config);
            ApiModifyUser.Response modifyResponse = modifyApi.request(modifyRequest);
            assertTrue(modifyResponse.isOK(), "3.1 修改 User 失败：" + getResponse);
            ApiModifyUser.Response.ModifiedIamUserData modifyUserData = modifyResponse.getData().getData();
            assertEquals(modifyUserData.getId(), createdUserData.getId(), "3.2 修改 User 失败：" + getResponse);
            assertEquals(modifyUserData.getRootUid(), createdUserData.getRootUid(), "3.3 修改 User 失败：" + getResponse);
            assertEquals(modifyUserData.getIuid(), createdUserData.getIuid(), "3.4 修改 User 失败：" + getResponse);
            assertEquals(modifyUserData.getAlias(), createdUserData.getAlias(), "3.5 修改 User 失败：" + getResponse);
            assertNotNull(modifyUserData.getCreatedAt(), "3.6 修改 User 失败：" + getResponse);
            assertNotNull(modifyUserData.getUpdatedAt(), "3.7 修改 User 失败：" + getResponse);
            assertNotNull(modifyUserData.getLastLoginTime(), "3.8 修改 User 失败：" + getResponse);
            assertEquals(modifyUserData.getEnabled(), false, "3.9 修改 User 失败：" + getResponse);

            // 4. 列举所有
            ApiGetUsers.Request getUsersRequest = new ApiGetUsers.Request(baseUrl)
                    .setAlias(userAlias);
            ApiGetUsers getUsersApi = new ApiGetUsers(null, config);
            ApiGetUsers.Response getUsersResponse = getUsersApi.request(getUsersRequest);
            assertTrue(getUsersResponse.isOK(), "4.1 列举 User 失败：" + getResponse);
            ApiGetUsers.Response.GetIamUsersData getUsersDataList = getUsersResponse.getData().getData();
            assertEquals(1, (int) getUsersDataList.getCount(), "4.2 列举 User 失败：" + getResponse);
            ApiGetUsers.Response.GetIamUser getUsersData = getUsersDataList.getList()[0];
            assertEquals(getUsersData.getId(), modifyUserData.getId(), "4.3 修改 User 失败：" + getResponse);
            assertEquals(getUsersData.getRootUid(), modifyUserData.getRootUid(), "4.4 修改 User 失败：" + getResponse);
            assertEquals(getUsersData.getIuid(), modifyUserData.getIuid(), "4.5 修改 User 失败：" + getResponse);
            assertEquals(getUsersData.getAlias(), modifyUserData.getAlias(), "4.6 修改 User 失败：" + getResponse);
            assertNotNull(getUsersData.getCreatedAt(), "4.7 修改 User 失败：" + getResponse);
            assertNotNull(getUsersData.getUpdatedAt(), "4.8 修改 User 失败：" + getResponse);
            assertNotNull(getUsersData.getLastLoginTime(), "4.9 修改 User 失败：" + getResponse);
            assertEquals(getUsersData.getEnabled(), modifyUserData.getEnabled(), "4.10 修改 User 失败：" + getResponse);

            // 5. 删除
            ApiDeleteUser.Response deleteResponse = deleteApi.request(deleteRequest);
            assertTrue(deleteResponse.isOK(), "5.1 删除 User 失败：" + getResponse);

        } catch (QiniuException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                deleteApi.request(deleteRequest);
            } catch (QiniuException e) {
                // 删除失败时预期的
                e.printStackTrace();
            }
        }

    }

    @Test
    @Tag("IntegrationTest")
    public void testUsersKeyPairs() {


    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testUsersGroups() {


    }

    @Test
    @Disabled
    @Tag("IntegrationTest")
    public void testUsersPolicies() {


    }
}
