package test.com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.iam.apis.*;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

public class UserApiTest {

    String userAlias = ApiTestConfig.userAlias;
    String groupAlias = ApiTestConfig.groupAlias;
    String policyAlias = ApiTestConfig.policyAlias;
    String userPWD = ApiTestConfig.userPWD;
    String baseUrl = ApiTestConfig.baseUrl;
    Api.Config config = ApiTestConfig.config;

    @Test
    @Tag("IntegrationTest")
    public void testUsers() {
        // 先删除，流程开始先清理历史数据
        try {
            ApiDeleteUser.Request deleteUserRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
            ApiDeleteUser deleteUserApi = new ApiDeleteUser(null, config);
            deleteUserApi.request(deleteUserRequest);
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
            assertTrue(createResponse.isOK(), "1.1 创建 User 失败：" + createResponse);
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
            ApiModifyUser.Request.ModifiedIamUserParam modifyParam = new ApiModifyUser.Request.ModifiedIamUserParam();
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
            ApiDeleteUser.Request deleteUserRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
            ApiDeleteUser deleteUserApi = new ApiDeleteUser(null, config);
            ApiDeleteUser.Response deleteUserResponse = deleteUserApi.request(deleteUserRequest);
            assertTrue(deleteUserResponse.isOK(), "5.1 删除 User 失败：" + getResponse);

        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUsersKeyPairs() {

        // 先删除，流程开始先清理历史数据
        try {
            ApiDeleteUser.Request deleteUserRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
            ApiDeleteUser deleteUserApi = new ApiDeleteUser(null, config);
            deleteUserApi.request(deleteUserRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
            e.printStackTrace();
        }

        try {
            // 1. 创建用户
            // 创建后会自带一组
            ApiCreateUser.Request.CreateIamUserParam createUserParam = new ApiCreateUser.Request.CreateIamUserParam();
            createUserParam.setAlias(userAlias);
            createUserParam.setPassword(userPWD);
            ApiCreateUser.Request createUserRequest = new ApiCreateUser.Request(baseUrl, createUserParam);
            ApiCreateUser createUserApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createUserResponse = createUserApi.request(createUserRequest);
            assertNotNull(createUserResponse, "1. 创建 User 失败：" + createUserResponse);
            assertEquals(createUserResponse.isOK(), true, "1.1 创建 User 失败：" + createUserResponse);
            ApiCreateUser.Response.CreatedIamUserData createUserResponseData = createUserResponse.getData().getData();

            // 2. 创建秘钥
            ApiCreateUserKeypairs.Request createRequest = new ApiCreateUserKeypairs.Request(baseUrl, userAlias);
            ApiCreateUserKeypairs createApi = new ApiCreateUserKeypairs(null, config);
            ApiCreateUserKeypairs.Response createResponse = createApi.request(createRequest);
            assertNotNull(createUserResponse, "2. 创建 User 秘钥失败：" + createUserResponse);
            assertEquals(createUserResponse.isOK(), true, "2.1 创建 User 秘钥失败：" + createUserResponse);
            ApiCreateUserKeypairs.Response.CreatedIamUserKeyPairData createResponseData = createResponse.getData().getData();
            assertNotNull(createResponseData.getId(), "2.3 创建 User 秘钥失败：" + createUserResponse);
            assertNotNull(createResponseData.getAccessKey(), "2.4 创建 User 秘钥失败：" + createUserResponse);
            assertNotNull(createResponseData.getSecretKey(), "2.5 创建 User 秘钥失败：" + createUserResponse);
            assertEquals(createResponseData.getUserId(), createUserResponseData.getId(), "2.6 创建 User 秘钥失败：" + createUserResponse);
            assertNotNull(createResponseData.getCreatedAt(), "2.7 创建 User 秘钥失败：" + createUserResponse);
            assertEquals(createResponseData.getEnabled(), true, "2.8 创建 User 秘钥失败：" + createUserResponse);

            // 3. 获取秘钥
            ApiGetUserKeypairs.Request getRequest = new ApiGetUserKeypairs.Request(baseUrl, userAlias);
            ApiGetUserKeypairs getApi = new ApiGetUserKeypairs(null, config);
            ApiGetUserKeypairs.Response getResponse = getApi.request(getRequest);
            assertNotNull(getResponse, "3. 获取 User 秘钥失败：" + createUserResponse);
            assertEquals(getResponse.isOK(), true, "3.1 创建 User 秘钥失败：" + getResponse);
            ApiGetUserKeypairs.Response.GetIamUserKeyPairsData getResponseData = getResponse.getData().getData();
            assertEquals(getResponseData.getCount().intValue(), 2, "3.2 创建 User 秘钥失败：" + getResponse);
            ApiGetUserKeypairs.Response.GetIamUserKeyPair keyPair = null;
            for (ApiGetUserKeypairs.Response.GetIamUserKeyPair kp : getResponseData.getList()) {
                if (kp.getAccessKey().equals(createResponseData.getAccessKey())) {
                    keyPair = kp;
                }
            }
            assertNotNull(keyPair, "3.3 创建 User 秘钥失败：" + getResponse);
            assertNotNull(keyPair.getId(), "3.4 创建 User 秘钥失败：" + getResponse);
            assertEquals(keyPair.getSecretKey(), createResponseData.getSecretKey(), "3.5 创建 User 秘钥失败：" + getResponse);
            assertEquals(keyPair.getUserId(), createUserResponseData.getId(), "3.6 创建 User 秘钥失败：" + getResponse);
            assertNotNull(keyPair.getCreatedAt(), "3.7 创建 User 秘钥失败：" + getResponse);
            assertNotNull(keyPair.getEnabled(), "3.8 创建 User 秘钥失败：" + getResponse);

            // 4. 修改秘钥: Disable
            ApiDisableUserKeypair.Request diableRequest = new ApiDisableUserKeypair.Request(baseUrl, userAlias, createResponseData.getAccessKey());
            ApiDisableUserKeypair disableApi = new ApiDisableUserKeypair(null, config);
            ApiDisableUserKeypair.Response disableResponse = disableApi.request(diableRequest);
            assertNotNull(disableResponse, "4. Disable User 秘钥失败：" + disableResponse);
            assertEquals(disableResponse.isOK(), true, "4.1 Disable User 秘钥失败：" + disableResponse);

            // 5. 验证秘钥修改: Disable
            getResponse = getApi.request(getRequest);
            assertNotNull(getResponse, "5. 验证 User 秘钥失败：" + createUserResponse);
            assertEquals(getResponse.isOK(), true, "5.1 验证 User 秘钥失败：" + getResponse);
            getResponseData = getResponse.getData().getData();
            assertEquals(getResponseData.getCount().intValue(), 2, "5.2 验证 User 秘钥失败：" + getResponse);
            for (ApiGetUserKeypairs.Response.GetIamUserKeyPair kp : getResponseData.getList()) {
                if (kp.getAccessKey().equals(createResponseData.getAccessKey())) {
                    keyPair = kp;
                }
            }
            assertEquals(keyPair.getEnabled(), false, "5.3 验证 User 秘钥失败：" + getResponse);

            // 6. 修改秘钥: Enable
            ApiEnableUserKeypair.Request enableRequest = new ApiEnableUserKeypair.Request(baseUrl, userAlias, createResponseData.getAccessKey());
            ApiEnableUserKeypair enableApi = new ApiEnableUserKeypair(null, config);
            ApiEnableUserKeypair.Response enableResponse = enableApi.request(enableRequest);
            assertNotNull(enableResponse, "6. Enable User 秘钥失败：" + enableResponse);
            assertEquals(enableResponse.isOK(), true, "6.1 Enable User 秘钥失败：" + enableResponse);

            // 7. 验证秘钥修改: Disable
            getResponse = getApi.request(getRequest);
            assertNotNull(getResponse, "7. 验证 User 秘钥失败：" + createUserResponse);
            assertEquals(getResponse.isOK(), true, "7.1 验证 User 秘钥失败：" + getResponse);
            getResponseData = getResponse.getData().getData();
            assertEquals(getResponseData.getCount().intValue(), 2, "7.2 验证 User 秘钥失败：" + getResponse);
            for (ApiGetUserKeypairs.Response.GetIamUserKeyPair kp : getResponseData.getList()) {
                if (kp.getAccessKey().equals(createResponseData.getAccessKey())) {
                    keyPair = kp;
                }
            }
            assertEquals(keyPair.getEnabled(), true, "7.3 验证 User 秘钥失败：" + getResponse);

            // 8. 删除秘钥
            ApiDeleteUserKeypair.Request deleteRequest = new ApiDeleteUserKeypair.Request(baseUrl, userAlias, createResponseData.getAccessKey());
            ApiDeleteUserKeypair deleteApi = new ApiDeleteUserKeypair(null, config);
            ApiDeleteUserKeypair.Response deleteResponse = deleteApi.request(deleteRequest);
            assertNotNull(deleteResponse, "8. Delete User 秘钥失败：" + deleteResponse);
            assertEquals(deleteResponse.isOK(), true, "8.1 Delete User 秘钥失败：" + deleteResponse);

        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUsersGroups() {
        try {
            ApiDeleteUser.Request deleteUserRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
            ApiDeleteUser deleteUserApi = new ApiDeleteUser(null, config);
            deleteUserApi.request(deleteUserRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
        }

        try {
            ApiDeleteGroup.Request deleteGroupRequest = new ApiDeleteGroup.Request(baseUrl, groupAlias);
            ApiDeleteGroup deleteGroupApi = new ApiDeleteGroup(null, config);
            deleteGroupApi.request(deleteGroupRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
        }

        try {
            // 0 创建用户的
            ApiCreateUser.Request.CreateIamUserParam createUserParam = new ApiCreateUser.Request.CreateIamUserParam();
            createUserParam.setAlias(userAlias);
            createUserParam.setPassword(userPWD);
            ApiCreateUser.Request createUserRequest = new ApiCreateUser.Request(baseUrl, createUserParam);
            ApiCreateUser createUserApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createUserResponse = createUserApi.request(createUserRequest);
            assertNotNull(createUserResponse, "0. 创建 User 失败：" + createUserResponse);
            assertEquals(createUserResponse.isOK(), true, "0.1 创建 User 失败：" + createUserResponse);

            // 1 创建用户的 Group
            ApiCreateGroup.Request.CreateGroupParam createGroupParam = new ApiCreateGroup.Request.CreateGroupParam();
            createGroupParam.setAlias(groupAlias);
            ApiCreateGroup.Request createGroupRequest = new ApiCreateGroup.Request(baseUrl, createGroupParam);
            ApiCreateGroup createGroupApi = new ApiCreateGroup(null, config);
            ApiCreateGroup.Response createGroupResponse = createGroupApi.request(createGroupRequest);
            Assertions.assertNotNull(createGroupResponse, "1. 创建分组失败：" + createGroupResponse);
            Assertions.assertTrue(createGroupResponse.isOK(), "1.1 创建分组失败：" + createGroupResponse);

            // 2 用户添加至 Group
            ApiUpdateUserGroups.Request.UpdatedIamUserGroupsParam updateUserGroupsRequestParam = new ApiUpdateUserGroups.Request.UpdatedIamUserGroupsParam();
            updateUserGroupsRequestParam.setGroupAliases(new String[]{groupAlias});
            ApiUpdateUserGroups.Request updateUserGroupsRequest = new ApiUpdateUserGroups.Request(baseUrl, userAlias, updateUserGroupsRequestParam);
            ApiUpdateUserGroups updateUserGroupsApi = new ApiUpdateUserGroups(null, config);
            ApiUpdateUserGroups.Response updateUserGroupsResponse = updateUserGroupsApi.request(updateUserGroupsRequest);
            Assertions.assertNotNull(updateUserGroupsResponse, "2 用户添加至 Group 失败：" + updateUserGroupsResponse);
            Assertions.assertTrue(updateUserGroupsResponse.isOK(), "2.1 用户添加至 Group 失败：" + updateUserGroupsResponse);

            // 3 获取用户的 Group（验证）
            ApiGetUserGroups.Request getUserGroupsRequest = new ApiGetUserGroups.Request(baseUrl, userAlias);
            getUserGroupsRequest.setPage(0);
            ApiGetUserGroups getUserGroupsApi = new ApiGetUserGroups(null, config);
            ApiGetUserGroups.Response getUserGroupsResponse = getUserGroupsApi.request(getUserGroupsRequest);
            Assertions.assertNotNull(getUserGroupsResponse, "3 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertTrue(getUserGroupsResponse.isOK(), "3.1 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertTrue(getUserGroupsResponse.getData().getData().getCount() > 0, "3.2 获取用户的 Group 失败：" + getUserGroupsResponse);

            ApiGetUserGroups.Response.IamUserGroup userGroup = getUserGroupsResponse.getData().getData().getList()[0];
            Assertions.assertNotNull(userGroup.getId(), "3.3 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertNotNull(userGroup.getRootUid(), "3.4 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertEquals(userGroup.getAlias(), groupAlias, "3.5 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertNotNull(userGroup.getDescription(), "3.6 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertEquals(userGroup.getEnabled(), true, "3.7 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertNotNull(userGroup.getCreatedAt(), "3.8 获取用户的 Group 失败：" + getUserGroupsResponse);
            Assertions.assertNotNull(userGroup.getUpdatedAt(), "3.9 获取用户的 Group 失败：" + getUserGroupsResponse);

        } catch (QiniuException e) {
            fail(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUsersPolicies() {
        try {
            ApiDeleteUser.Request deleteUserRequest = new ApiDeleteUser.Request(baseUrl, userAlias);
            ApiDeleteUser deleteUserApi = new ApiDeleteUser(null, config);
            deleteUserApi.request(deleteUserRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
        }

        try {
            ApiDeletePolicy.Request deleteRequest = new ApiDeletePolicy.Request(baseUrl, policyAlias);
            ApiDeletePolicy deleteApi = new ApiDeletePolicy(null, config);
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
        }

        try {
            // 0 创建用户的
            ApiCreateUser.Request.CreateIamUserParam createUserParam = new ApiCreateUser.Request.CreateIamUserParam();
            createUserParam.setAlias(userAlias);
            createUserParam.setPassword(userPWD);
            ApiCreateUser.Request createUserRequest = new ApiCreateUser.Request(baseUrl, createUserParam);
            ApiCreateUser createUserApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createUserResponse = createUserApi.request(createUserRequest);
            assertNotNull(createUserResponse, "0. 创建 User 失败：" + createUserResponse);
            assertEquals(createUserResponse.isOK(), true, "0.1 创建 User 失败：" + createUserResponse);

            // 1 创建用户的 Policy
            String policyDesc = policyAlias + "Desc";
            String policyAction = "cdn/DownloadCDNLog";
            String policyEffect = "Allow";
            String policyResource = "qrn:product:::/a/b/c.txt";
            ApiCreatePolicy.Request.CreateStatement createStatement = new ApiCreatePolicy.Request.CreateStatement();
            createStatement.setActions(new String[]{policyAction});
            createStatement.setEffect(policyEffect);
            createStatement.setResources(new String[]{policyResource});
            ApiCreatePolicy.Request.CreatePolicyParam createPolicyRequestParam = new ApiCreatePolicy.Request.CreatePolicyParam();
            createPolicyRequestParam.setEditType(1);
            createPolicyRequestParam.setAlias(policyAlias);
            createPolicyRequestParam.setDescription(policyDesc);
            createPolicyRequestParam.setStatement(new ApiCreatePolicy.Request.CreateStatement[]{createStatement});
            ApiCreatePolicy.Request createPolicyRequest = new ApiCreatePolicy.Request(baseUrl, createPolicyRequestParam);
            ApiCreatePolicy createPolicyApi = new ApiCreatePolicy(null, config);
            ApiCreatePolicy.Response createPolicyResponse = createPolicyApi.request(createPolicyRequest);
            assertNotNull(createPolicyResponse, "1. 创建 Policy 失败：" + createPolicyResponse);
            assertTrue(createPolicyResponse.isOK(), "1.1 创建 Policy 失败：" + createPolicyResponse);

            // 2 用户添加至 Policy

            // 3 获取用户 Policy

            // 4 更新用户添加至 Policy

            // 5 获取用户的 Policy（验证）

            // 6 删除用户的 Policy

            // 7 获取用户的 Policy（验证）

            // 8 获取用户的 Service

            // 9 列举子用户指定服务操作下的可访问资源
        } catch (QiniuException e) {
            fail(e);
        }
    }
}
