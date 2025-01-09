package com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupPolicyApiTest {

    String groupAlias = ApiTestConfig.groupAlias;
    String policyAliasA = ApiTestConfig.policyAlias + "A";
    String policyAliasB = ApiTestConfig.policyAlias + "B";
    String baseUrl = ApiTestConfig.baseUrl;
    Api.Config config = ApiTestConfig.config;

    @Test
    @Tag("IntegrationTest")
    public void testGroupPolicyApi() {
        // 清理
        try {
            ApiDeletePolicy.Request deleteRequest = new ApiDeletePolicy.Request(baseUrl, policyAliasA);
            ApiDeletePolicy deleteApi = new ApiDeletePolicy(null, config);
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
        }

        try {
            ApiDeletePolicy.Request deleteRequest = new ApiDeletePolicy.Request(baseUrl, policyAliasB);
            ApiDeletePolicy deleteApi = new ApiDeletePolicy(null, config);
            deleteApi.request(deleteRequest);
        } catch (QiniuException e) {
        }

        try {
            ApiDeleteGroup.Request deleteGroupRequest = new ApiDeleteGroup.Request(baseUrl, groupAlias);
            ApiDeleteGroup deleteGroupApi = new ApiDeleteGroup(null, config);
            deleteGroupApi.request(deleteGroupRequest);
        } catch (QiniuException e) {
        }

        try {
            // 1. 创建分组
            String groupDescription = "JavaTestGroupDescription";
            ApiCreateGroup.Request.CreateGroupParam createGroupParam = new ApiCreateGroup.Request.CreateGroupParam();
            createGroupParam.setAlias(groupAlias);
            createGroupParam.setDescription(groupDescription);
            ApiCreateGroup.Request createGroupRequest = new ApiCreateGroup.Request(baseUrl, createGroupParam);
            ApiCreateGroup createGroupApi = new ApiCreateGroup(null, config);
            ApiCreateGroup.Response createGroupResponse = createGroupApi.request(createGroupRequest);
            Assertions.assertNotNull(createGroupResponse, "1. 创建用户分组失败：" + createGroupResponse);
            Assertions.assertTrue(createGroupResponse.isOK(), "1.1 创建用户分组失败：" + createGroupResponse);

            String policyDesc = policyAliasA + "Desc";
            String policyAction = "cdn/DownloadCDNLog";
            String policyEffect = "Allow";
            String policyResource = "qrn:product:::/a/b/c.txt";
            ApiCreatePolicy.Request.CreateStatement createStatement = new ApiCreatePolicy.Request.CreateStatement();
            createStatement.setActions(new String[]{policyAction});
            createStatement.setEffect(policyEffect);
            createStatement.setResources(new String[]{policyResource});
            ApiCreatePolicy.Request.CreatePolicyParam createPolicyRequestParam = new ApiCreatePolicy.Request.CreatePolicyParam();
            createPolicyRequestParam.setEditType(1);
            createPolicyRequestParam.setAlias(policyAliasA);
            createPolicyRequestParam.setDescription(policyDesc);
            createPolicyRequestParam.setStatement(new ApiCreatePolicy.Request.CreateStatement[]{createStatement});
            ApiCreatePolicy.Request createPolicyRequest = new ApiCreatePolicy.Request(baseUrl, createPolicyRequestParam);
            ApiCreatePolicy createPolicyApi = new ApiCreatePolicy(null, config);
            ApiCreatePolicy.Response createPolicyResponse = createPolicyApi.request(createPolicyRequest);
            assertNotNull(createPolicyResponse, "2. 创建 Policy 失败：" + createPolicyResponse);
            assertTrue(createPolicyResponse.isOK(), "2.1 创建 Policy 失败：" + createPolicyResponse);

            policyDesc = policyAliasB + "Desc";
            createStatement = new ApiCreatePolicy.Request.CreateStatement();
            createStatement.setActions(new String[]{policyAction});
            createStatement.setEffect(policyEffect);
            createStatement.setResources(new String[]{policyResource});
            createPolicyRequestParam = new ApiCreatePolicy.Request.CreatePolicyParam();
            createPolicyRequestParam.setEditType(1);
            createPolicyRequestParam.setAlias(policyAliasB);
            createPolicyRequestParam.setDescription(policyDesc);
            createPolicyRequestParam.setStatement(new ApiCreatePolicy.Request.CreateStatement[]{createStatement});
            createPolicyRequest = new ApiCreatePolicy.Request(baseUrl, createPolicyRequestParam);
            createPolicyApi = new ApiCreatePolicy(null, config);
            createPolicyResponse = createPolicyApi.request(createPolicyRequest);
            assertNotNull(createPolicyResponse, "3. 创建 Policy 失败：" + createPolicyResponse);
            assertTrue(createPolicyResponse.isOK(), "3.1 创建 Policy 失败：" + createPolicyResponse);

            ApiModifyGroupPolicies.Request.ModifiedGroupIamPoliciesParam modifyParam = new ApiModifyGroupPolicies.Request.ModifiedGroupIamPoliciesParam();
            modifyParam.setPolicyAliases(new String[]{policyAliasA});
            ApiModifyGroupPolicies.Request modifyRequest = new ApiModifyGroupPolicies.Request(baseUrl, groupAlias, modifyParam);
            ApiModifyGroupPolicies modifyApi = new ApiModifyGroupPolicies(null, config);
            ApiModifyGroupPolicies.Response modifyResponse = modifyApi.request(modifyRequest);
            Assertions.assertNotNull(modifyResponse, "4. 用户分组添加 Policies 失败：" + modifyResponse);
            Assertions.assertTrue(modifyResponse.isOK(), "4.1 创建用户分组添加 Policies 失败：" + modifyResponse);

            ApiGetGroupPolicies.Request getRequest = new ApiGetGroupPolicies.Request(baseUrl, groupAlias);
            ApiGetGroupPolicies getApi = new ApiGetGroupPolicies(null, config);
            ApiGetGroupPolicies.Response getResponse = getApi.request(getRequest);
            Assertions.assertNotNull(getResponse, "5. 获取用户分组 Policies 失败：" + getResponse);
            Assertions.assertTrue(getResponse.isOK(), "5.1 获取用户分组失败 Policies ：" + getResponse);
            ApiGetGroupPolicies.Response.GetGroupPoliciesData getResponsePoliciesData = getResponse.getData().getData();
            Assertions.assertNotNull(getResponsePoliciesData, "5.2 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getCount() == 1, "5.3 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getList().length == 1, "5.4 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            ApiGetGroupPolicies.Response.GroupPolicy getResponsePolicy = getResponsePoliciesData.getList()[0];
            Assertions.assertEquals(getResponsePolicy.getAlias(), policyAliasA, "5.5 获取用户分组失败 Policies ：" + getResponsePoliciesData);

            ApiUpdateGroupPolicies.Request.UpdatedGroupIamPoliciesParam updateParam = new ApiUpdateGroupPolicies.Request.UpdatedGroupIamPoliciesParam();
            updateParam.setPolicyAliases(new String[]{policyAliasA, policyAliasB});
            ApiUpdateGroupPolicies.Request updateRequest = new ApiUpdateGroupPolicies.Request(baseUrl, groupAlias, updateParam);
            ApiUpdateGroupPolicies updateApi = new ApiUpdateGroupPolicies(null, config);
            ApiUpdateGroupPolicies.Response updateResponse = updateApi.request(updateRequest);
            Assertions.assertNotNull(updateResponse, "6. 用户分组更新 Policies 失败：" + updateResponse);
            Assertions.assertTrue(updateResponse.isOK(), "6.1 用户分组更新 Policies 失败：" + updateResponse);

            getRequest = new ApiGetGroupPolicies.Request(baseUrl, groupAlias);
            getApi = new ApiGetGroupPolicies(null, config);
            getResponse = getApi.request(getRequest);
            Assertions.assertNotNull(getResponse, "7. 获取用户分组 Policies 失败：" + getResponse);
            Assertions.assertTrue(getResponse.isOK(), "7.1 获取用户分组失败 Policies ：" + getResponse);
            getResponsePoliciesData = getResponse.getData().getData();
            Assertions.assertNotNull(getResponsePoliciesData, "7.2 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getCount() == 2, "7.3 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getList().length == 2, "7.4 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            ApiGetGroupPolicies.Response.GroupPolicy[] groupPolicies = getResponsePoliciesData.getList();
            for (ApiGetGroupPolicies.Response.GroupPolicy policy : groupPolicies) {
                if (policy.getAlias().equals(policyAliasA)) {
                    continue;
                }
                if (policy.getAlias().equals(policyAliasB)) {
                    continue;
                }
                Assertions.fail("7.5 获取用户分组失败 Policies");
            }

            ApiDeleteGroupPolicies.Request.DeletedGroupIamPoliciesParam deleteParam = new ApiDeleteGroupPolicies.Request.DeletedGroupIamPoliciesParam();
            deleteParam.setPolicyAliases(new String[]{policyAliasA});
            ApiDeleteGroupPolicies.Request deleteRequest = new ApiDeleteGroupPolicies.Request(baseUrl, groupAlias, deleteParam);
            ApiDeleteGroupPolicies deleteApi = new ApiDeleteGroupPolicies(null, config);
            ApiDeleteGroupPolicies.Response deleteResponse = deleteApi.request(deleteRequest);
            Assertions.assertNotNull(deleteResponse, "8. 用户分组删除 Policies 失败：" + deleteResponse);
            Assertions.assertTrue(deleteResponse.isOK(), "8.1 用户分组删除 Policies 失败：" + deleteResponse);

            getRequest = new ApiGetGroupPolicies.Request(baseUrl, groupAlias);
            getApi = new ApiGetGroupPolicies(null, config);
            getResponse = getApi.request(getRequest);
            Assertions.assertNotNull(getResponse, "9. 获取用户分组 Policies 失败：" + getResponse);
            Assertions.assertTrue(getResponse.isOK(), "9.1 获取用户分组失败 Policies ：" + getResponse);
            getResponsePoliciesData = getResponse.getData().getData();
            Assertions.assertNotNull(getResponsePoliciesData, "9.2 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getCount() == 1, "9.3 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            Assertions.assertTrue(getResponsePoliciesData.getList().length == 1, "9.4 获取用户分组失败 Policies ：" + getResponsePoliciesData);
            groupPolicies = getResponsePoliciesData.getList();
            Assertions.assertEquals(groupPolicies[0].getAlias(), policyAliasB, "9.5 获取用户分组失败 Policies");
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

    }
}
