package com.qiniu.cdn;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by bailong on 16/9/21.
 */
public final class CdnManager {
    private final Auth auth;
    private final String server;
    private final Client client;

    public CdnManager(Auth auth) {
        this(auth, "http://fusion.qiniuapi.com");
    }

    private CdnManager(Auth auth, String server) {
        this.auth = auth;
        this.server = server;
        this.client = new Client(null, false, null,
                Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.WRITE_TIMEOUT);
    }

    /**
     * 刷新链接列表，每次最多不可以超过100条链接
     *
     * @link http://developer.qiniu.com/article/fusion/api/refresh.html
     */
    public Response refreshUrls(String[] urls) throws QiniuException {
        return refreshUrlsAndDirs(urls, null);
    }

    /**
     * 刷新目录列表，每次最多不可以超过10个目录
     * 刷新目录需要额外开通权限，可以联系七牛技术支持处理
     *
     * @link http://developer.qiniu.com/article/fusion/api/refresh.html
     */
    public Response refreshDirs(String[] dirs) throws QiniuException {
        return refreshUrlsAndDirs(null, dirs);
    }

    public Response refreshUrlsAndDirs(String[] urls, String[] dirs) throws QiniuException {
        HashMap<String, String[]> req = new HashMap<>();
        if (urls != null) {
            req.put("urls", urls);
        }
        if (dirs != null) {
            req.put("dirs", dirs);
        }
        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/refresh";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * 预取文件链接，每次最多不可以超过100条
     *
     * @link http://developer.qiniu.com/article/fusion/api/prefetch.html
     */
    public Response prefetchUrls(String[] urls) throws QiniuException {
        HashMap<String, String[]> req = new HashMap<>();
        req.put("urls", urls);
        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/prefetch";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * 获取域名访问带宽数据
     *
     * @link http://developer.qiniu.com/article/fusion/api/traffic-bandwidth.html
     */
    public Response getBandwidthData(String[] domains, String startDate, String endDate,
                                     String granularity) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("startDate", startDate);
        req.put("endDate", endDate);
        req.put("granularity", granularity);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/bandwidth";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * 获取域名访问流量数据
     *
     * @link http://developer.qiniu.com/article/fusion/api/traffic-bandwidth.html
     */
    public Response getFluxData(String[] domains, String startDate, String endDate,
                                String granularity) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("startDate", startDate);
        req.put("endDate", endDate);
        req.put("granularity", granularity);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/flux";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * 获取CDN域名访问日志的下载链接
     *
     * @link http://developer.qiniu.com/article/fusion/api/log.html
     */
    public Response getCdnLogList(String[] domains, String logDate) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("day", logDate);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/log/list";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    /**
     * 构建标准的基于时间戳的防盗链
     *
     * @param host        自定义域名，例如 http://img.abc.com
     * @param fileName    待访问的原始文件名，必须是utf8编码，不需要进行urlencode
     * @param queryString 业务自身的查询参数，必须是utf8编码，不需要进行urlencode
     * @param encryptKey  时间戳防盗链的签名密钥，从七牛后台获取
     * @param deadline    链接的有效期时间戳，是以秒为单位的Unix时间戳
     * @return signedUrl   最终的带时间戳防盗链的url
     */
    public static String createTimestampAntiLeechUrl(
            String host, String fileName, String queryString, String encryptKey, long deadline)
            throws UnsupportedEncodingException, MalformedURLException, NoSuchAlgorithmException {
        String urlToSign;
        if (queryString.trim().length() != 0) {
            urlToSign = String.format("%s/%s?%s", host, URLEncoder.encode(fileName, "utf-8"),
                    URLEncoder.encode(queryString, "utf-8"));
        } else {
            urlToSign = String.format("%s/%s", host, URLEncoder.encode(fileName, "utf-8"));
        }

        URL urlObj = new URL(urlToSign);
        String path = urlObj.getPath();

        String expireHex = Long.toHexString(deadline);

        String toSignStr = String.format("%s%s%s", encryptKey, path, expireHex);
        String signedStr = StringUtils.md5Lower(toSignStr);

        String signedUrl;
        if (urlObj.getQuery() != null) {
            signedUrl = String.format("%s&sign=%s&t=%s", urlToSign, signedStr, expireHex);
        } else {
            signedUrl = String.format("%s?sign=%s&t=%s", urlToSign, signedStr, expireHex);
        }

        return signedUrl;
    }
}
