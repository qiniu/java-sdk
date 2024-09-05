package test.com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.iam.apis.*;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyApiTest {

    String userAlias = ApiTestConfig.userAlias;
    String userPWD = ApiTestConfig.userPWD;
    String groupAlias = ApiTestConfig.groupAlias;
    String policyAlias = ApiTestConfig.policyAlias;
    String baseUrl = ApiTestConfig.baseUrl;
    Api.Config config = ApiTestConfig.config;

    @Test
    @Tag("IntegrationTest")
    public void testPolicyApi() {
        // 清理
        try {
            ApiDeletePolicy.Request deleteRequest = new ApiDeletePolicy.Request(baseUrl, policyAlias);
            ApiDeletePolicy deleteApi = new ApiDeletePolicy(null, config);
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
        }

        try {
            ApiDeletePolicy.Request deleteRequest = new ApiDeletePolicy.Request(baseUrl, policyAlias + "New");
            ApiDeletePolicy deleteApi = new ApiDeletePolicy(null, config);
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
        }

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
            String policyDesc = policyAlias + "Desc";
            String policyAction = "cdn/DownloadCDNLog";
            String policyEffect = "Allow";
            String policyResource = "qrn:product:::/a/b/c.txt";
            // 1. 创建 Policy
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

            ApiCreatePolicy.Response.CreatedPolicyData createPolicyResponseData = createPolicyResponse.getData().getData();
            assertNotNull(createPolicyResponseData.getId(), "1.2 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(createPolicyResponseData.getRootUid(), "1.3 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createPolicyResponseData.getAlias(), policyAlias, "1.4 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createPolicyResponseData.getDescription(), policyDesc, "1.5 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createPolicyResponseData.getEnabled(), true, "1.6 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(createPolicyResponseData.getUpdatedAt(), "1.7 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(createPolicyResponseData.getCreatedAt(), "1.8 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createPolicyResponseData.getStatement().length, 1, "1.9 创建 Policy 失败：" + createPolicyResponse);
            ApiCreatePolicy.Response.CreatedStatement createResponseStatement = createPolicyResponseData.getStatement()[0];
            assertEquals(createResponseStatement.getEffect(), policyEffect, "1.10 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createResponseStatement.getActions()[0], policyAction, "1.11 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(createResponseStatement.getResources()[0], policyResource, "1.12 创建 Policy 失败：" + createPolicyResponse);

            // 2. 获取 Policy（验证创建）
            ApiGetPolicy.Request getPolicyRequest = new ApiGetPolicy.Request(baseUrl, policyAlias);
            ApiGetPolicy getPolicyApi = new ApiGetPolicy(null, config);
            ApiGetPolicy.Response getPolicyResponse = getPolicyApi.request(getPolicyRequest);
            assertNotNull(getPolicyResponse, "2. 获取  Policy 失败：" + getPolicyResponse);
            assertTrue(getPolicyResponse.isOK(), "2.1 获取  Policy 失败：" + getPolicyResponse);

            ApiGetPolicy.Response.GetPolicyData getPolicyResponseData = getPolicyResponse.getData().getData();
            assertNotNull(getPolicyResponseData.getId(), "2.2 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getRootUid(), "2.3 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getAlias(), policyAlias, "2.4 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getDescription(), policyDesc, "2.5 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getEnabled(), true, "2.6 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getUpdatedAt(), "2.7 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getCreatedAt(), "2.8 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getStatement().length, 1, "2.9 创建 Policy 失败：" + createPolicyResponse);
            ApiGetPolicy.Response.Statement getPolicyResponseStatement = getPolicyResponseData.getStatement()[0];
            assertEquals(getPolicyResponseStatement.getEffect(), policyEffect, "2.10 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseStatement.getActions()[0], policyAction, "2.11 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseStatement.getResources()[0], policyResource, "2.12 创建 Policy 失败：" + createPolicyResponse);

            // 3. 修改 Policy
            String policyAliasNew = policyAlias + "New";
            String policyDescNew = policyDesc + "New";
            String policyActionNew = "certificate/GetCertificate";
            String policyEffectNew = "Deny";
            String policyResourceNew = policyResource + "New";
            ApiModifyPolicy.Request.ModifyStatement modifyPolicyStatement = new ApiModifyPolicy.Request.ModifyStatement();
            modifyPolicyStatement.setEffect(policyEffectNew);
            modifyPolicyStatement.setActions(new String[]{policyActionNew});
            modifyPolicyStatement.setResources(new String[]{policyResourceNew});
            ApiModifyPolicy.Request.ModifyPolicyParam modifyPolicyRequestParam = new ApiModifyPolicy.Request.ModifyPolicyParam();
            modifyPolicyRequestParam.setNewAlias(policyAliasNew);
            modifyPolicyRequestParam.setDescription(policyDescNew);
            modifyPolicyRequestParam.setStatement(new ApiModifyPolicy.Request.ModifyStatement[]{modifyPolicyStatement});
            ApiModifyPolicy.Request modifyPolicyRequest = new ApiModifyPolicy.Request(baseUrl, policyAlias, modifyPolicyRequestParam);
            ApiModifyPolicy modifyPolicyApi = new ApiModifyPolicy(null, config);
            ApiModifyPolicy.Response modifyPolicyResponse = modifyPolicyApi.request(modifyPolicyRequest);
            assertNotNull(modifyPolicyResponse, "3. 获取  Policy 失败：" + modifyPolicyResponse);
            assertTrue(modifyPolicyResponse.isOK(), "3.1 获取  Policy 失败：" + modifyPolicyResponse);

            policyAlias = policyAliasNew;
            policyDesc = policyDescNew;
            policyAction = policyActionNew;
            policyEffect = policyEffectNew;
            policyResource = policyResourceNew;
            ApiModifyPolicy.Response.ModifiedPolicyData modifyPolicyResponseData = modifyPolicyResponse.getData().getData();
            assertNotNull(modifyPolicyResponseData.getId(), "3.2 创建 Policy 失败：" + modifyPolicyResponse);
            assertNotNull(modifyPolicyResponseData.getRootUid(), "3.3 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseData.getAlias(), policyAlias, "3.4 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseData.getDescription(), policyDesc, "3.5 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseData.getEnabled(), true, "3.6 创建 Policy 失败：" + modifyPolicyResponse);
            assertNotNull(modifyPolicyResponseData.getUpdatedAt(), "3.7 创建 Policy 失败：" + modifyPolicyResponse);
            assertNotNull(modifyPolicyResponseData.getCreatedAt(), "3.8 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseData.getStatement().length, 1, "3.9 创建 Policy 失败：" + modifyPolicyResponse);
            ApiModifyPolicy.Response.ModifiedStatement modifyPolicyResponseStatement = modifyPolicyResponseData.getStatement()[0];
            assertEquals(modifyPolicyResponseStatement.getEffect(), policyEffect, "3.10 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseStatement.getActions()[0], policyAction, "3.11 创建 Policy 失败：" + modifyPolicyResponse);
            assertEquals(modifyPolicyResponseStatement.getResources()[0], policyResource, "3.12 创建 Policy 失败：" + modifyPolicyResponse);

            // 4. 获取 Policy（验证修改）
            getPolicyRequest = new ApiGetPolicy.Request(baseUrl, policyAlias);
            getPolicyApi = new ApiGetPolicy(null, config);
            getPolicyResponse = getPolicyApi.request(getPolicyRequest);
            assertNotNull(getPolicyResponse, "4. 获取  Policy 失败：" + getPolicyResponse);
            assertTrue(getPolicyResponse.isOK(), "4.1 获取  Policy 失败：" + getPolicyResponse);

            getPolicyResponseData = getPolicyResponse.getData().getData();
            assertNotNull(getPolicyResponseData.getId(), "4.2 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getRootUid(), "4.3 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getAlias(), policyAlias, "4.4 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getDescription(), policyDesc, "4.5 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getEnabled(), true, "4.6 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getUpdatedAt(), "4.7 创建 Policy 失败：" + createPolicyResponse);
            assertNotNull(getPolicyResponseData.getCreatedAt(), "4.8 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseData.getStatement().length, 1, "4.9 创建 Policy 失败：" + createPolicyResponse);
            getPolicyResponseStatement = getPolicyResponseData.getStatement()[0];
            assertEquals(getPolicyResponseStatement.getEffect(), policyEffect, "4.10 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseStatement.getActions()[0], policyAction, "4.11 创建 Policy 失败：" + createPolicyResponse);
            assertEquals(getPolicyResponseStatement.getResources()[0], policyResource, "4.12 创建 Policy 失败：" + createPolicyResponse);

            // 5. 列举 Policy
            ApiGetPolicies.Request getPoliciesRequest = new ApiGetPolicies.Request(baseUrl);
            ApiGetPolicies getPoliciesApi = new ApiGetPolicies(null, config);
            ApiGetPolicies.Response getPoliciesResponse = getPoliciesApi.request(getPoliciesRequest);
            assertNotNull(getPoliciesResponse, "5. 获取所有 Policy 失败：" + getPoliciesResponse);
            assertTrue(getPoliciesResponse.isOK(), "5.1 获取所有  Policy 失败：" + getPoliciesResponse);

            ApiGetPolicies.Response.Policy[] allPolicies = getPoliciesResponse.getData().getData().getList();
            ApiGetPolicies.Response.Policy listPolicy = null;
            for (ApiGetPolicies.Response.Policy policy : allPolicies) {
                if (Objects.equals(policy.getId(), getPolicyResponseData.getId())) {
                    listPolicy = policy;
                    break;
                }
            }
            assertNotNull(listPolicy.getId(), "5.2 创建 Policy 失败：" + getPoliciesResponse);
            assertNotNull(listPolicy.getRootUid(), "5.3 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicy.getAlias(), policyAlias, "5.4 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicy.getDescription(), policyDesc, "5.5 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicy.getEnabled(), true, "5.6 创建 Policy 失败：" + getPoliciesResponse);
            assertNotNull(listPolicy.getUpdatedAt(), "5.7 创建 Policy 失败：" + getPoliciesResponse);
            assertNotNull(listPolicy.getCreatedAt(), "5.8 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicy.getStatement().length, 1, "5.9 创建 Policy 失败：" + getPoliciesResponse);
            ApiGetPolicies.Response.Statement listPolicyResponseStatement = listPolicy.getStatement()[0];
            assertEquals(listPolicyResponseStatement.getEffect(), policyEffect, "5.10 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicyResponseStatement.getActions()[0], policyAction, "5.11 创建 Policy 失败：" + getPoliciesResponse);
            assertEquals(listPolicyResponseStatement.getResources()[0], policyResource, "5.12 创建 Policy 失败：" + createPolicyResponse);

            // 6. 删除 Policy
            ApiDeletePolicy.Request deletePolicyRequest = new ApiDeletePolicy.Request(baseUrl, policyAlias);
            ApiDeletePolicy deletePolicyApi = new ApiDeletePolicy(null, config);
            ApiDeletePolicy.Response deletePolicyResponse = deletePolicyApi.request(deletePolicyRequest);
            assertNotNull(deletePolicyResponse, "6. 获取所有 Policy 失败：" + deletePolicyResponse);
            assertTrue(deletePolicyResponse.isOK(), "6.1 获取所有  Policy 失败：" + deletePolicyResponse);

            // 7. 获取 Policy（验证删除）
            getPoliciesRequest = new ApiGetPolicies.Request(baseUrl);
            getPoliciesApi = new ApiGetPolicies(null, config);
            getPoliciesResponse = getPoliciesApi.request(getPoliciesRequest);
            assertNotNull(getPoliciesResponse, "7. 获取所有 Policy 失败：" + getPoliciesResponse);
            assertTrue(getPoliciesResponse.isOK(), "7.1 获取所有  Policy 失败：" + getPoliciesResponse);

            allPolicies = getPoliciesResponse.getData().getData().getList();
            listPolicy = null;
            for (ApiGetPolicies.Response.Policy policy : allPolicies) {
                if (Objects.equals(policy.getId(), getPolicyResponseData.getId())) {
                    listPolicy = policy;
                    break;
                }
            }
            assertNull(listPolicy, "7.2 获取所有  Policy 失败：" + deletePolicyResponse);

            // 8. 创建 Policy 用户
            ApiCreateUser.Request.CreateIamUserParam createUserParam = new ApiCreateUser.Request.CreateIamUserParam();
            createUserParam.setAlias(userAlias);
            createUserParam.setPassword(userPWD);
            ApiCreateUser.Request createRequest = new ApiCreateUser.Request(baseUrl, createUserParam);
            ApiCreateUser createUserApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createUserResponse = createUserApi.request(createRequest);
            assertNotNull(createUserResponse, "8. 创建 Policy 用户失败：" + createUserResponse);
            assertTrue(createUserResponse.isOK(), "8.1 创建 Policy 用户失败：" + createUserResponse);
            ApiCreateUser.Response.CreatedIamUserData createdUserData = createUserResponse.getData().getData();

            // 9. 创建 Policy
            createStatement = new ApiCreatePolicy.Request.CreateStatement();
            createStatement.setActions(new String[]{policyAction});
            createStatement.setEffect(policyEffect);
            createStatement.setResources(new String[]{policyResource});
            createPolicyRequestParam = new ApiCreatePolicy.Request.CreatePolicyParam();
            createPolicyRequestParam.setEditType(1);
            createPolicyRequestParam.setAlias(policyAlias);
            createPolicyRequestParam.setDescription(policyDesc);
            createPolicyRequestParam.setStatement(new ApiCreatePolicy.Request.CreateStatement[]{createStatement});
            createPolicyRequest = new ApiCreatePolicy.Request(baseUrl, createPolicyRequestParam);
            createPolicyApi = new ApiCreatePolicy(null, config);
            createPolicyResponse = createPolicyApi.request(createPolicyRequest);
            assertNotNull(createPolicyResponse, "9. 创建 Policy 失败：" + createPolicyResponse);
            assertTrue(createPolicyResponse.isOK(), "9.1 创建 Policy 失败：" + createPolicyResponse);

            // 9.1 Policy 添加用户
            ApiUpdatePolicyUsers.Request.UpdatedPolicyIamUsersParam updatePolicyUsersRequestParam = new ApiUpdatePolicyUsers.Request.UpdatedPolicyIamUsersParam();
            updatePolicyUsersRequestParam.setUserAliases(new String[]{userAlias});
            ApiUpdatePolicyUsers.Request updatePolicyUsersRequest = new ApiUpdatePolicyUsers.Request(baseUrl, policyAlias, updatePolicyUsersRequestParam);
            ApiUpdatePolicyUsers updatePolicyUsersApi = new ApiUpdatePolicyUsers(null, config);
            ApiUpdatePolicyUsers.Response updatePolicyUsersResponse = updatePolicyUsersApi.request(updatePolicyUsersRequest);
            assertNotNull(updatePolicyUsersResponse, "9.1 Policy 添加用户失败：" + updatePolicyUsersResponse);
            assertTrue(updatePolicyUsersResponse.isOK(), "9.1.1 Policy 添加用户失败：" + updatePolicyUsersResponse);

            // 10. 获取 Policy 用户
            ApiGetPolicyUsers.Request getPolicyUsersRequest = new ApiGetPolicyUsers.Request(baseUrl, policyAlias);
            ApiGetPolicyUsers getPolicyUsersApi = new ApiGetPolicyUsers(null, config);
            ApiGetPolicyUsers.Response getPolicyUsersResponse = getPolicyUsersApi.request(getPolicyUsersRequest);
            assertNotNull(getPolicyUsersResponse, "10. 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertTrue(getPolicyUsersResponse.isOK(), "10.1 获取 Policy 用户失败：" + getPolicyUsersResponse);

            ApiGetPolicyUsers.Response.PolicyIamUser[] allPolicyUsers = getPolicyUsersResponse.getData().getData().getList();
            assertTrue(allPolicyUsers.length > 0, "10.2 获取 Policy 用户失败：" + getPolicyUsersResponse);

            ApiGetPolicyUsers.Response.PolicyIamUser getPolicyResponseUser = null;
            for (ApiGetPolicyUsers.Response.PolicyIamUser user : allPolicyUsers) {
                if (user.getId().equals(createUserResponse.getData().getData().getId())) {
                    getPolicyResponseUser = user;
                    break;
                }
            }
            assertEquals(getPolicyResponseUser.getAlias(), userAlias, "10.3 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getId(), "10.4 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getRootUid(), "10.5 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getIuid(), "10.6 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getAlias(), "10.7 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getCreatedAt(), "10.8 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getUpdatedAt(), "10.9 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getLastLoginTime(), "10.10 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertNotNull(getPolicyResponseUser.getEnabled(), "10.11 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertEquals(getPolicyResponseUser.getId(), createdUserData.getId(), "10.12 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertEquals(getPolicyResponseUser.getRootUid(), createdUserData.getRootUid(), "10.13 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertEquals(getPolicyResponseUser.getIuid(), createdUserData.getIuid(), "10.14 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertEquals(getPolicyResponseUser.getAlias(), createdUserData.getAlias(), "10.15 获取 Policy 用户失败：" + getPolicyUsersResponse);
            assertEquals(getPolicyResponseUser.getEnabled(), createdUserData.getEnabled(), "10.16 获取 Policy 用户失败：" + getPolicyUsersResponse);

            // 11. 创建 Policy 群组
            ApiCreateGroup.Request.CreateGroupParam createGroupParam = new ApiCreateGroup.Request.CreateGroupParam();
            createGroupParam.setAlias(groupAlias);
            ApiCreateGroup.Request createGroupRequest = new ApiCreateGroup.Request(baseUrl, createGroupParam);
            ApiCreateGroup createGroupApi = new ApiCreateGroup(null, config);
            ApiCreateGroup.Response createGroupResponse = createGroupApi.request(createGroupRequest);
            Assertions.assertNotNull(createGroupResponse, "11. 创建 Policy 群组失败：" + createGroupResponse);
            Assertions.assertTrue(createGroupResponse.isOK(), "11.1 创建 Policy 群组失败：" + createGroupResponse);
            ApiCreateGroup.Response.CreatedGroupData createGroupResponseData = createGroupResponse.getData().getData();

            // 12. Policy 添加群组
            ApiUpdatePolicyGroups.Request.UpdatedPolicyGroupsParam updatePolicyGroupsRequestParam = new ApiUpdatePolicyGroups.Request.UpdatedPolicyGroupsParam();
            updatePolicyGroupsRequestParam.setGroupAliases(new String[]{groupAlias});
            ApiUpdatePolicyGroups.Request updatePolicyGroupsRequest = new ApiUpdatePolicyGroups.Request(baseUrl, policyAlias, updatePolicyGroupsRequestParam);
            ApiUpdatePolicyGroups updatePolicyGroupsApi = new ApiUpdatePolicyGroups(null, config);
            ApiUpdatePolicyGroups.Response updatePolicyGroupsResponse = updatePolicyGroupsApi.request(updatePolicyGroupsRequest);
            Assertions.assertNotNull(updatePolicyGroupsResponse, "12. Policy 添加群组失败：" + updatePolicyGroupsResponse);
            Assertions.assertTrue(updatePolicyGroupsResponse.isOK(), "12.1 Policy 添加群组失败：" + updatePolicyGroupsResponse);

            // 13. 获取 Policy 群组
            ApiGetPolicyGroups.Request getPolicyGroupsRequest = new ApiGetPolicyGroups.Request(baseUrl, policyAlias);
            ApiGetPolicyGroups getPolicyGroupsApi = new ApiGetPolicyGroups(null, config);
            ApiGetPolicyGroups.Response getPolicyGroupsResponse = getPolicyGroupsApi.request(getPolicyGroupsRequest);
            Assertions.assertNotNull(getPolicyGroupsResponse, "13. 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertTrue(getPolicyGroupsResponse.isOK(), "13.1 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertTrue(getPolicyGroupsResponse.getData().getData().getCount() > 0, "13.2 获取 Policy 群组失败：" + getPolicyGroupsResponse);

            ApiGetPolicyGroups.Response.GetPolicyGroup[] allPolicyGroups = getPolicyGroupsResponse.getData().getData().getList();
            Assertions.assertNotNull(allPolicyGroups, "13. 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            assertEquals(allPolicyGroups.length, (int) getPolicyGroupsResponse.getData().getData().getCount(), "13.1 获取 Policy 群组失败：" + getPolicyGroupsResponse);

            ApiGetPolicyGroups.Response.GetPolicyGroup policyGroup = allPolicyGroups[0];
            Assertions.assertEquals(policyGroup.getId(), createGroupResponseData.getId(), "13.2 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertEquals(policyGroup.getRootUid(), createGroupResponseData.getRootUid(), "13.3 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertEquals(policyGroup.getAlias(), createGroupResponseData.getAlias(), "13.4 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertEquals(policyGroup.getDescription(), createGroupResponseData.getDescription(), "13.5 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertEquals(policyGroup.getEnabled(), createGroupResponseData.getEnabled(), "13.6 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertNotNull(policyGroup.getCreatedAt(), "13.7 获取 Policy 群组失败：" + getPolicyGroupsResponse);
            Assertions.assertNotNull(policyGroup.getUpdatedAt(), "13.8 获取 Policy 群组失败：" + getPolicyGroupsResponse);


        } catch (QiniuException e) {
            fail();
            throw new RuntimeException(e);
        }
    }
}
