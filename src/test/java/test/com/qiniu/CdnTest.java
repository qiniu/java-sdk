package test.com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by bailong on 16/09/21. Updated by panyuan on 19/03/07
 */
public class CdnTest {

    /**
     * 获取Response
     *
     * @param url
     * @return
     */
    public static Response getResponse(String url) {
        try {
            Client client = new Client();
            Response response = client.get(url);
            return response;
        } catch (QiniuException ex) {
            return ex.response;
        }
    }

    /**
     * 获取日期
     *
     * @param daysBefore
     * @return
     */
    private String getDate(int daysBefore) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -daysBefore);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }

    /**
     * 测试刷新，只检查是否返回200
     */
    @Test
    @Tag("IntegrationTest")
    public void testRefresh() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.RefreshResult r;
        try {
            r = c.refreshUrls(new String[] { TestConfig.testUrl_z0 });
            assertEquals(200, r.code, msg);
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()), msg);
        }
    }

    /**
     * 测试预取，只检测是否返回200
     */
    @Test
    @Tag("IntegrationTest")
    public void testPrefetch() {
        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.PrefetchResult r;
        try {
            r = c.prefetchUrls(new String[] { TestConfig.testUrl_na0 });
            assertEquals(200, r.code);
        } catch (QiniuException e) {
            e.printStackTrace();
            assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode()));
        }
    }

    /**
     * 测试获取域名带宽，只检测是否返回200
     */
    @Test
    @Tag("IntegrationTest")
    public void testGetBandwidth() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.BandwidthResult r = null;
        String[] domains = { TestConfig.testDomain_z0 };
        String startDate = getDate(3);
        String endDate = getDate(1);
        String granularity = "day";
        try {
            r = c.getBandwidthData(domains, startDate, endDate, granularity);
            assertEquals(200, r.code, msg);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail(msg);
        }
    }

    /**
     * 测试获取域名流量，只检测是否返回200
     */
    @Test
    @Tag("IntegrationTest")
    public void testGetFlux() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.FluxResult r = null;
        String[] domains = { TestConfig.testDomain_z0 };
        String startDate = getDate(3);
        String endDate = getDate(1);
        String granularity = "day";
        try {
            r = c.getFluxData(domains, startDate, endDate, granularity);
            assertEquals(200, r.code, msg);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail(msg);
        }
    }

    /**
     * 测试获取CDN域名访问日志的下载链接 检测日志信息列表长度是否>=0
     */
    @Test
    @Tag("IntegrationTest")
    public void testGetCdnLogList() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        CdnManager c = new CdnManager(TestConfig.testAuth);
        CdnResult.LogListResult r = null;
        String[] domains = { TestConfig.testDomain_z0 };
        String logDate = getDate(2);

        try {
            r = c.getCdnLogList(domains, logDate);
            assertTrue(r.data.size() >= 0, msg);
        } catch (QiniuException e) {
            e.printStackTrace();
            fail(msg);
        }
    }

    /**
     * 测试时间戳防盗链 检测url是否请求403 检测signedUrl1是否返回200 检测signedUrl2是否返回200
     * 检测signedUrl3是否返回403 检测signedUrl3与预期结果是否一致
     */
    @Test
    @Tag("IntegrationTest")
    public void testCreateTimestampAntiLeechUrlSimple() {
        String msg = " 空间删除了访问域名，若测试，请先在空间绑定域名,  ";

        String host = "http://" + TestConfig.testDomain_z0_timeStamp;
        String fileName = TestConfig.testKey_z0;
        StringMap queryStringMap = new StringMap();
        queryStringMap.put("qiniu", "七牛");
        queryStringMap.put("test", "Test");
        String encryptKey1 = "908b9cbbbc88028b50b8e8a88baa879bf1b8a788";
        String encryptKey2 = "d799eba9ff99ea88cfb8acbbf8b82898208afbb8";
        long deadline1 = System.currentTimeMillis() / 1000 + 3600;
        long deadline2 = deadline1;
        long deadline3 = 1485893946; // 2017-02-01 04:19:06 +0800 CST
        String testUrl_z0_timeStamp_outdate = "http://javasdk-timestamp.peterpy.cn/do_not_delete/1.png?sign=14f48f829b78d5c9a34eb77e9a13f1b6&t=5890f13a";
        try {
            URL url = new URL(TestConfig.testUrl_z0_timeStamp);
            assertEquals(403, getResponse(url.toString()).statusCode, msg);
            String signedUrl1 = CdnManager.createTimestampAntiLeechUrl(host, fileName, queryStringMap, encryptKey1,
                    deadline1);
            String signedUrl2 = CdnManager.createTimestampAntiLeechUrl(url, encryptKey2, deadline2);
            String signedUrl3 = CdnManager.createTimestampAntiLeechUrl(host, fileName, null, encryptKey1, deadline3);
            System.out.println(signedUrl1);
            System.out.println(signedUrl2);
            System.out.println(signedUrl3);
            assertEquals(200, getResponse(signedUrl1).statusCode, msg);
            assertEquals(200, getResponse(signedUrl2).statusCode, msg);
            assertEquals(403, getResponse(signedUrl3).statusCode, msg);
            assertEquals(testUrl_z0_timeStamp_outdate, signedUrl3);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(msg);
        }
    }

}
