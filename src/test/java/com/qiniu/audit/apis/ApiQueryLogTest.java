package com.qiniu.audit.apis;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

public class ApiQueryLogTest {

    static final String baseUrl = "api.qiniu.com";
    static final Api.Config config = new Api.Config.Builder()
            .setAuth(TestConfig.testAuth)
            .setRequestDebugLevel(Api.Config.DebugLevelDetail)
            .setResponseDebugLevel(Api.Config.DebugLevelDetail)
            .build();

    @Test
    @Tag("IntegrationTest")
    public void testQuery() {

        try {

            String sTime = "2024-07-03T22:44:26Z";
            String eTime = "2024-09-10T22:44:26Z";
            String eventNames = "SSOLogin";
            String serviceName = "Account";
            String principalId = "1002013202061";
            String accessKeyId = "";
            ApiQueryLog.Request request = new ApiQueryLog.Request(baseUrl, sTime, eTime);
            request.setLimit(1);
            request.setEventNames(eventNames);
            request.setServiceName(serviceName);
            request.setPrincipalId(principalId);
            request.setAccessKeyId(accessKeyId);
            ApiQueryLog api = new ApiQueryLog(null, config);

            ApiQueryLog.Response response = api.request(request);
            Assertions.assertNotNull(response, "1. 获取 log 失败：" + response);
            Assertions.assertTrue(response.isOK(), "1.1 获取 log 失败：" + response);
            Assertions.assertNotNull(response.getData(), "1.2 获取 log 失败：" + response);
            Assertions.assertEquals(1, response.getData().getAuditLogInfos().length, "1.3 获取 log 失败：" + response);
            Assertions.assertNotNull(response.getData().getNextMark(), "1.4 获取 log 失败：" + response);

            ApiQueryLog.Response.LogInfo logInfo = response.getData().getAuditLogInfos()[0];
            Assertions.assertNotNull(logInfo.getEventId(), "1.5 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getEventType(), "1.6 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getEventTime(), "1.7 获取 log 失败：" + response);
            Assertions.assertEquals(logInfo.getEventName(), eventNames, "1.8 获取 log 失败：" + response);
            Assertions.assertEquals(logInfo.getUserIdentity().getAccessKeyId(), accessKeyId, "1.9 获取 log 失败：" + response);
            Assertions.assertEquals(logInfo.getUserIdentity().getPrincipalId(), principalId, "1.10 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getUserIdentity().getPrincipalType(), "1.11 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getUserIdentity().getAccountId(), "1.12 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getEventRw(), "1.13 获取 log 失败：" + response);
            Assertions.assertEquals(logInfo.getServiceName(), serviceName, "1.14 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getSourceIp(), "1.15 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getUserAgent(), "1.16 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getResourceNames(), "1.17 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getRequestId(), "1.18 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getRequestUrl(), "1.19 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getRequestParams(), "1.20 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getResponseData(), "1.21 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getResponseCode(), "1.22 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getResponseMessage(), "1.23 获取 log 失败：" + response);
            Assertions.assertNotNull(logInfo.getAdditionalEventData(), "1.24 获取 log 失败：" + response);


            request.setNextMark(response.getData().getNextMark());

            response = api.request(request);
            Assertions.assertNotNull(response, "1.25 获取 log 失败：" + response);
            Assertions.assertTrue(response.isOK(), "1.26 获取 log 失败：" + response);
            Assertions.assertNotNull(response.getData(), "1.27 获取 log 失败：" + response);
            Assertions.assertEquals(1, response.getData().getAuditLogInfos().length, "1.28 获取 log 失败：" + response);
            Assertions.assertNotNull(response.getData().getNextMark(), "1.29 获取 log 失败：" + response);


        } catch (QiniuException e) {
            if (e.response == null || e.response.statusCode != 400) {
                throw new RuntimeException(e);
            }
            e.printStackTrace();
        }
    }
}
