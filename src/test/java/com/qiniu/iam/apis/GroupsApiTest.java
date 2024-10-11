package com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GroupsApiTest {

    String groupAlias = ApiTestConfig.groupAlias;
    String userAlias = ApiTestConfig.userAlias;
    String userPWD = ApiTestConfig.userPWD;
    String baseUrl = ApiTestConfig.baseUrl;
    Api.Config config = ApiTestConfig.config;


    @Test
    @Tag("IntegrationTest")
    void testGroups() {
        // 先删除，流程开始先清理历史数据
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
            ApiDeleteGroup.Request deleteGroupRequest = new ApiDeleteGroup.Request(baseUrl, groupAlias + "New");
            ApiDeleteGroup deleteGroupApi = new ApiDeleteGroup(null, config);
            deleteGroupApi.request(deleteGroupRequest);
        } catch (QiniuException e) {
            // 删除失败时预期的
        }

        String groupDescription = "JavaTestGroupDescription";
        try {

            // 1. 创建分组
            ApiCreateGroup.Request.CreateGroupParam createGroupParam = new ApiCreateGroup.Request.CreateGroupParam();
            createGroupParam.setAlias(groupAlias);
            createGroupParam.setDescription(groupDescription);
            ApiCreateGroup.Request createGroupRequest = new ApiCreateGroup.Request(baseUrl, createGroupParam);
            ApiCreateGroup createGroupApi = new ApiCreateGroup(null, config);
            ApiCreateGroup.Response createGroupResponse = createGroupApi.request(createGroupRequest);
            Assertions.assertNotNull(createGroupResponse, "1. 创建分组失败：" + createGroupResponse);
            Assertions.assertTrue(createGroupResponse.isOK(), "1.1 创建分组失败：" + createGroupResponse);
            ApiCreateGroup.Response.CreatedGroupData createGroupResponseData = createGroupResponse.getData().getData();
            Assertions.assertNotNull(createGroupResponseData.getId(), "1.2 创建分组失败：" + createGroupResponse);
            Assertions.assertNotNull(createGroupResponseData.getRootUid(), "1.3 创建分组失败：" + createGroupResponse);
            Assertions.assertNotNull(createGroupResponseData.getAlias(), "1.4 创建分组失败：" + createGroupResponse);
            Assertions.assertEquals(createGroupResponseData.getDescription(), groupDescription, "1.5 创建分组失败：" + createGroupResponse);
            Assertions.assertEquals(createGroupResponseData.getEnabled(), true, "1.6 创建分组失败：" + createGroupResponse);
            Assertions.assertNotNull(createGroupResponseData.getCreatedAt(), "1.7 创建分组失败：" + createGroupResponse);
            Assertions.assertNotNull(createGroupResponseData.getUpdatedAt(), "1.8 创建分组失败：" + createGroupResponse);

            // 2. 获取分组详情
            ApiGetGroup.Request getRequest = new ApiGetGroup.Request(baseUrl, groupAlias);
            ApiGetGroup getApi = new ApiGetGroup(null, config);
            ApiGetGroup.Response getResponse = getApi.request(getRequest);
            Assertions.assertNotNull(getResponse, "2. 获取分组详情失败：" + getResponse);
            Assertions.assertTrue(getResponse.isOK(), "2.1 获取分组详情失败：" + getResponse);
            ApiGetGroup.Response.GetGroupData getResponseData = getResponse.getData().getData();
            Assertions.assertNotNull(getResponseData, "2.2 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getId(), createGroupResponseData.getId(), "2.3 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getRootUid(), createGroupResponseData.getRootUid(), "2.4 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getAlias(), createGroupResponseData.getAlias(), "2.5 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getDescription(), createGroupResponseData.getDescription(), "2.6 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getEnabled(), createGroupResponseData.getEnabled(), "2.7 获取分组详情失败：" + getResponse);
            Assertions.assertNotNull(getResponseData.getCreatedAt(), "2.8 获取分组详情失败：" + getResponse);
            Assertions.assertNotNull(getResponseData.getUpdatedAt(), "2.9 获取分组详情失败：" + getResponse);


            // 3. 列举所有分组
            ApiGetGroups.Request getGroupsRequest = new ApiGetGroups.Request(baseUrl);
            ApiGetGroups getGroupsApi = new ApiGetGroups(null, config);
            ApiGetGroups.Response getGroupsResponse = getGroupsApi.request(getGroupsRequest);
            Assertions.assertNotNull(getGroupsResponse, "3. 列举所有分组失败：" + getResponse);
            Assertions.assertTrue(getGroupsResponse.isOK(), "3.1 列举所有分组详情失败：" + getResponse);
            ApiGetGroups.Response.GetGroupsData getGroupsResponseData = getGroupsResponse.getData().getData();
            Assertions.assertNotNull(getGroupsResponseData, "3.2 列举所有分组失败：" + getResponse);
            Assertions.assertTrue(getGroupsResponseData.getCount() > 0, "3.3 列举所有分组失败：" + getResponse);
            Assertions.assertNotNull(getGroupsResponseData.getList(), "3.4 列举所有分组失败：" + getResponse);
            ApiGetGroups.Response.GetGroup group = null;
            for (ApiGetGroups.Response.GetGroup g : getGroupsResponseData.getList()) {
                if (g.getId().equals(getResponseData.getId())) {
                    group = g;
                }
            }
            Assertions.assertNotNull(group, "3.5 列举所有分组失败：" + getResponse);
            Assertions.assertEquals(group.getId(), getResponseData.getId(), "3.6 列举所有分组失败：" + getResponse);
            Assertions.assertEquals(group.getRootUid(), getResponseData.getRootUid(), "3.7 列举所有分组失败：" + getResponse);
            Assertions.assertEquals(group.getAlias(), getResponseData.getAlias(), "3.8 列举所有分组失败：" + getResponse);
            Assertions.assertEquals(group.getDescription(), getResponseData.getDescription(), "3.9 列举所有分组失败：" + getResponse);
            Assertions.assertEquals(group.getEnabled(), getResponseData.getEnabled(), "3.10 列举所有分组失败：" + getResponse);
            Assertions.assertNotNull(group.getCreatedAt(), "3.11 列举所有分组失败：" + getResponse);
            Assertions.assertNotNull(group.getUpdatedAt(), "3.12 列举所有分组失败：" + getResponse);


            // 4. 更新用户分组详情
            String newGroupAlias = groupAlias + "New";
            groupDescription = groupDescription + "New";
            ApiModifyGroup.Request.ModifyGroupParam modifyGroupParam = new ApiModifyGroup.Request.ModifyGroupParam();
            modifyGroupParam.setNewAlias(newGroupAlias);
            modifyGroupParam.setDescription(groupDescription);
            ApiModifyGroup.Request modifyGroupRequest = new ApiModifyGroup.Request(baseUrl, groupAlias, modifyGroupParam);
            ApiModifyGroup modifyGroupApi = new ApiModifyGroup(null, config);
            ApiModifyGroup.Response modifyGroupResponse = modifyGroupApi.request(modifyGroupRequest);
            Assertions.assertNotNull(modifyGroupResponse, "4. 创建分组失败：" + modifyGroupRequest);
            Assertions.assertTrue(modifyGroupResponse.isOK(), "4.1 创建分组失败：" + modifyGroupRequest);

            groupAlias = newGroupAlias;

            // 5. 验证用户分组详情更新
            getRequest = new ApiGetGroup.Request(baseUrl, groupAlias);
            getApi = new ApiGetGroup(null, config);
            getResponse = getApi.request(getRequest);
            Assertions.assertNotNull(getResponse, "5. 获取分组详情失败：" + getResponse);
            Assertions.assertTrue(getResponse.isOK(), "5.1 获取分组详情失败：" + getResponse);
            getResponseData = getResponse.getData().getData();
            Assertions.assertNotNull(getResponseData, "5.2 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getId(), createGroupResponseData.getId(), "5.3 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getRootUid(), createGroupResponseData.getRootUid(), "5.4 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getAlias(), groupAlias, "5.5 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getDescription(), groupDescription, "5.6 获取分组详情失败：" + getResponse);
            Assertions.assertEquals(getResponseData.getEnabled(), createGroupResponseData.getEnabled(), "5.7 获取分组详情失败：" + getResponse);
            Assertions.assertNotNull(getResponseData.getCreatedAt(), "5.8 获取分组详情失败：" + getResponse);
            Assertions.assertNotNull(getResponseData.getUpdatedAt(), "5.9 获取分组详情失败：" + getResponse);

            // 用户相关
            // 6. 创建群组的用户
            ApiCreateUser.Request.CreateIamUserParam createUserParam = new ApiCreateUser.Request.CreateIamUserParam();
            createUserParam.setAlias(userAlias);
            createUserParam.setPassword(userPWD);
            ApiCreateUser.Request createUserRequest = new ApiCreateUser.Request(baseUrl, createUserParam);
            ApiCreateUser createApi = new ApiCreateUser(null, config);
            ApiCreateUser.Response createUserResponse = createApi.request(createUserRequest);
            assertNotNull(createUserResponse, "6. 创建 User 失败：" + createUserResponse);
            assertTrue(createUserResponse.isOK(), "6.1 创建 User 失败：" + createUserResponse);
            ApiCreateUser.Response.CreatedIamUserData createUser = createUserResponse.getData().getData();

            // 6.1 群组添加用户
            ApiModifyGroupUsers.Request.ModifiedGroupIamUsersParam modifyGroupUserParam = new ApiModifyGroupUsers.Request.ModifiedGroupIamUsersParam();
            modifyGroupUserParam.setUserAliases(new String[]{userAlias});
            ApiModifyGroupUsers.Request modifyGroupUserRequest = new ApiModifyGroupUsers.Request(baseUrl, groupAlias, modifyGroupUserParam);
            ApiModifyGroupUsers modifyGroupUserApi = new ApiModifyGroupUsers(null, config);
            ApiModifyGroupUsers.Response modifyGroupUserResponse = modifyGroupUserApi.request(modifyGroupUserRequest);
            assertNotNull(modifyGroupUserResponse, "6.1 创建 User 失败：" + modifyGroupUserResponse);
            assertTrue(modifyGroupUserResponse.isOK(), "6.1.1 创建 User 失败：" + modifyGroupUserResponse);

            // 7. 列举群组的用户，验证创建是否成功
            ApiGetGroupUsers.Request getGroupUsersRequest = new ApiGetGroupUsers.Request(baseUrl, groupAlias);
            ApiGetGroupUsers getGroupUsersApi = new ApiGetGroupUsers(null, config);
            ApiGetGroupUsers.Response getGroupUsersResponse = getGroupUsersApi.request(getGroupUsersRequest);
            assertNotNull(getGroupUsersResponse, "7. 列举群组的用户失败：" + getGroupUsersResponse);
            assertTrue(getGroupUsersResponse.isOK(), "7.1 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(1, (int) getGroupUsersResponse.getData().getData().getCount(), "7.2 列举群组的用户失败：" + getGroupUsersResponse);
            ApiGetGroupUsers.Response.GroupIamUser groupUser = getGroupUsersResponse.getData().getData().getList()[0];
            assertEquals(groupUser.getId(), createUser.getId(), "7.3 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getRootUid(), createUser.getRootUid(), "7.4 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getIuid(), createUser.getIuid(), "7.5 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getAlias(), createUser.getAlias(), "7.6 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getCreatedAt(), "7.7 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getUpdatedAt(), "7.8 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getLastLoginTime(), "7.9 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getEnabled(), createUser.getEnabled(), "7.10 列举群组的用户失败：" + getGroupUsersResponse);

            // 8. 删除群组的用户
            ApiDeleteGroupUsers.Request.DeletedGroupIamUsersParam deleteGroupUserParam = new ApiDeleteGroupUsers.Request.DeletedGroupIamUsersParam();
            deleteGroupUserParam.setUserAliases(new String[]{userAlias});
            ApiDeleteGroupUsers.Request deleteGroupUserRequest = new ApiDeleteGroupUsers.Request(baseUrl, groupAlias, deleteGroupUserParam);
            ApiDeleteGroupUsers deleteGroupUserApi = new ApiDeleteGroupUsers(null, config);
            ApiDeleteGroupUsers.Response deleteGroupUserResponse = deleteGroupUserApi.request(deleteGroupUserRequest);
            assertNotNull(deleteGroupUserResponse, "8. 删除群组的用户失败：" + deleteGroupUserResponse);
            assertTrue(deleteGroupUserResponse.isOK(), "8.1 删除群组的用户失败：" + deleteGroupUserResponse);

            // 9. 列举群组的用户，验证删除
            getGroupUsersRequest = new ApiGetGroupUsers.Request(baseUrl, groupAlias);
            getGroupUsersApi = new ApiGetGroupUsers(null, config);
            getGroupUsersResponse = getGroupUsersApi.request(getGroupUsersRequest);
            assertNotNull(getGroupUsersResponse, "9. 列举群组的用户失败：" + getGroupUsersResponse);
            assertTrue(getGroupUsersResponse.isOK(), "9.1 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(0, (int) getGroupUsersResponse.getData().getData().getCount(), "9.2 列举群组的用户失败：" + getGroupUsersResponse);

            // 10. 重新分配 User
            ApiUpdateGroupUsers.Request.UpdatedGroupIamUsersParam updateGroupUsersRequestParam = new ApiUpdateGroupUsers.Request.UpdatedGroupIamUsersParam();
            updateGroupUsersRequestParam.setUserAliases(new String[]{userAlias});
            ApiUpdateGroupUsers.Request updateGroupUsersRequest = new ApiUpdateGroupUsers.Request(baseUrl, groupAlias, updateGroupUsersRequestParam);
            ApiUpdateGroupUsers updateGroupUsersApi = new ApiUpdateGroupUsers(null, config);
            ApiUpdateGroupUsers.Response updateGroupUsersResponse = updateGroupUsersApi.request(updateGroupUsersRequest);
            assertNotNull(updateGroupUsersResponse, "10. 分组增加 User 失败：" + updateGroupUsersResponse);
            assertTrue(updateGroupUsersResponse.isOK(), "10.1 分组增加 User 失败：" + updateGroupUsersResponse);

            // 11. 列举群组的用户，验证重置
            getGroupUsersRequest = new ApiGetGroupUsers.Request(baseUrl, groupAlias);
            getGroupUsersApi = new ApiGetGroupUsers(null, config);
            getGroupUsersResponse = getGroupUsersApi.request(getGroupUsersRequest);
            assertNotNull(getGroupUsersResponse, "11. 列举群组的用户失败：" + getGroupUsersResponse);
            assertTrue(getGroupUsersResponse.isOK(), "11.1 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(1, (int) getGroupUsersResponse.getData().getData().getCount(), "11.2 列举群组的用户失败：" + getGroupUsersResponse);
            groupUser = getGroupUsersResponse.getData().getData().getList()[0];
            assertEquals(groupUser.getId(), createUser.getId(), "11.3 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getRootUid(), createUser.getRootUid(), "11.4 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getIuid(), createUser.getIuid(), "11.5 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getAlias(), createUser.getAlias(), "11.6 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getCreatedAt(), "11.7 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getUpdatedAt(), "11.8 列举群组的用户失败：" + getGroupUsersResponse);
            assertNotNull(groupUser.getLastLoginTime(), "11.9 列举群组的用户失败：" + getGroupUsersResponse);
            assertEquals(groupUser.getEnabled(), createUser.getEnabled(), "11.10 列举群组的用户失败：" + getGroupUsersResponse);

            // 12 列举用户组指定服务操作下的可访问资源
            String service = "cdn";
            String actionAlias = "DownloadCDNLog";
            ApiGetGroupServiceActionResources.Request getGroupServiceActionResourcesRequest = new ApiGetGroupServiceActionResources.Request(baseUrl, groupAlias, service, actionAlias);
            ApiGetGroupServiceActionResources getGroupServiceActionResourcesApi = new ApiGetGroupServiceActionResources(null, config);
            ApiGetGroupServiceActionResources.Response getGroupServiceActionResourcesResponse = getGroupServiceActionResourcesApi.request(getGroupServiceActionResourcesRequest);
            assertNotNull(getGroupServiceActionResourcesResponse, "12 列举子用户指定服务操作下的可访问资源失败：" + getGroupServiceActionResourcesResponse);
            assertTrue(getGroupServiceActionResourcesResponse.isOK(), "12.1 列举子用户指定服务操作下的可访问资源失败：" + getGroupServiceActionResourcesResponse);

            ApiGetGroupServiceActionResources.Response.GetGroupServiceActionResources getGroupServiceActionResources = getGroupServiceActionResourcesResponse.getData().getData();
            assertNotNull(getGroupServiceActionResources, "12.2 列举子用户指定服务操作下的可访问资源失败：" + getGroupServiceActionResources);
            assertNotNull(getGroupServiceActionResources.getAllowedResources(), "12.3 列举子用户指定服务操作下的可访问资源失败：" + getGroupServiceActionResources);
            assertNotNull(getGroupServiceActionResources.getDeniedResources(), "12.4 列举子用户指定服务操作下的可访问资源失败：" + getGroupServiceActionResources);

        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

    }
}
