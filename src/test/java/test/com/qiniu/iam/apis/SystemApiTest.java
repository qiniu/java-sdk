package test.com.qiniu.iam.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.iam.apis.ApiGetActions;
import com.qiniu.iam.apis.ApiGetAudits;
import com.qiniu.iam.apis.ApiGetServices;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemApiTest {

    String baseUrl = ApiTestConfig.baseUrl;
    Api.Config config = ApiTestConfig.config;

    @Test
    @Tag("IntegrationTest")
    public void testAction() {
        try {
            ApiGetActions.Request request = new ApiGetActions.Request(baseUrl);
            ApiGetActions api = new ApiGetActions(null, config);
            ApiGetActions.Response response = api.request(request);
            assertNotNull(response, "1. 获取 Action 失败：" + response);
            assertTrue(response.isOK(), "1.1 获取 Action 失败：" + response);
            ApiGetActions.Response.GetActionsData responseData = response.getData().getData();
            assertTrue(responseData.getCount() > 0, "1.2 获取 Action 失败：" + response);
            ApiGetActions.Response.GetAction action = responseData.getList()[0];
            assertNotNull(action.getId(), "1.3 获取 Action 失败：" + response);
            assertNotNull(action.getName(), "1.4 获取 Action 失败：" + response);
            assertNotNull(action.getAlias(), "1.5 获取 Action 失败：" + response);
            assertNotNull(action.getService(), "1.6 获取 Action 失败：" + response);
            assertNotNull(action.getScope(), "1.7 获取 Action 失败：" + response);
            assertNotNull(action.getEnabled(), "1.8 获取 Action 失败：" + response);
            assertNotNull(action.getCreatedAt(), "1.9 获取 Action 失败：" + response);
            assertNotNull(action.getUpdatedAt(), "1.10 获取 Action 失败：" + response);
        } catch (QiniuException e) {
            fail();
            throw new RuntimeException(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testService() {
        try {
            ApiGetServices.Request request = new ApiGetServices.Request(baseUrl);
            ApiGetServices api = new ApiGetServices(null, config);
            ApiGetServices.Response response = api.request(request);
            assertNotNull(response, "1. 获取 Services 失败：" + response);
            assertTrue(response.isOK(), "1.1 获取 Services 失败：" + response);
            ApiGetServices.Response.GetServicesResp responseData = response.getData();
            assertTrue(responseData.getData().length > 0, "1.2 获取 Services 失败：" + response);
        } catch (QiniuException e) {
            fail();
            throw new RuntimeException(e);
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testAudits() {
        try {
            ApiGetAudits.Request request = new ApiGetAudits.Request(baseUrl);
            ApiGetAudits api = new ApiGetAudits(null, config);
            ApiGetAudits.Response response = api.request(request);
            assertNotNull(response, "1. 获取 Audits 失败：" + response);
            assertTrue(response.isOK(), "1.1 获取 Audits 失败：" + response);
            ApiGetAudits.Response.GetAuditLogsData responseData = response.getData().getData();
            assertNotNull(responseData.getMarker(), "1.2 获取 Audits 失败：" + response);
            if (responseData.getList().length > 0) {
                ApiGetAudits.Response.GetAuditLog log = responseData.getList()[0];
                assertNotNull(log.getId(), "1.3 获取 Audits 失败：" + response);
                assertNotNull(log.getRootUid(), "1.4 获取 Audits 失败：" + response);
                assertNotNull(log.getIuid(), "1.5 获取 Audits 失败：" + response);
                assertNotNull(log.getService(), "1.6 获取 Audits 失败：" + response);
                assertNotNull(log.getAction(), "1.7 获取 Audits 失败：" + response);
                assertNotNull(log.getCreatedAt(), "1.8 获取 Audits 失败：" + response);
                assertNotNull(log.getEventTime(), "1.9 获取 Audits 失败：" + response);
                assertNotNull(log.getDurationMs(), "1.10 获取 Audits 失败：" + response);
                assertNotNull(log.getSourceIp(), "1.11 获取 Audits 失败：" + response);
                assertNotNull(log.getUserEvent(), "1.12 获取 Audits 失败：" + response);
                assertNotNull(log.getErrorCode(), "1.13 获取 Audits 失败：" + response);
                assertNotNull(log.getErrorMessage(), "1.14 获取 Audits 失败：" + response);
            }
        } catch (QiniuException e) {
            fail();
            throw new RuntimeException(e);
        }

    }
}
