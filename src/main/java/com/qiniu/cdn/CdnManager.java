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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类主要提供CDN相关功能的接口实现，包括文件和目录的刷新，文件的预取，获取CDN访问日志链接，生成七牛时间戳防盗链等功能。
 */
public final class CdnManager {
    private final Auth auth;
    private final String server;
    private final Client client;

    /*
    * CdnManager 使用七牛标准的管理鉴权方式
    *
    * @param auth - Auth 对象
    * */
    public CdnManager(Auth auth) {
        this(auth, "http://fusion.qiniuapi.com");
    }

    private CdnManager(Auth auth, String server) {
        this.auth = auth;
        this.server = server;
        this.client = new Client();
    }

    /**
     * 刷新链接列表，每次最多不可以超过100条链接
     *
     * @return 刷新请求的回复
     * @link http://developer.qiniu.com/fusion/api/cache-refresh
     */
    public CdnResult.RefreshResult refreshUrls(String[] urls) throws QiniuException {
        return refreshUrlsAndDirs(urls, null);
    }

    /**
     * 刷新目录列表，每次最多不可以超过10个目录
     * 刷新目录需要额外开通权限，可以联系七牛技术支持处理
     *
     * @return 刷新请求的回复
     * @link http://developer.qiniu.com/fusion/api/cache-refresh
     */
    public CdnResult.RefreshResult refreshDirs(String[] dirs) throws QiniuException {
        return refreshUrlsAndDirs(null, dirs);
    }

    public CdnResult.RefreshResult refreshUrlsAndDirs(String[] urls, String[] dirs) throws QiniuException {
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
        Response response = client.post(url, body, headers, Client.JsonMime);
        return response.jsonToObject(CdnResult.RefreshResult.class);
    }

    /**
     * 预取文件链接，每次最多不可以超过100条
     *
     * @return 预取请求的回复
     * @link http://developer.qiniu.com/fusion/api/file-prefetching
     */
    public CdnResult.PrefetchResult prefetchUrls(String[] urls) throws QiniuException {
        HashMap<String, String[]> req = new HashMap<>();
        req.put("urls", urls);
        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/prefetch";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = client.post(url, body, headers, Client.JsonMime);
        return response.jsonToObject(CdnResult.PrefetchResult.class);
    }

    /**
     * 获取域名访问带宽数据
     *
     * @return 获取带宽请求的回复
     * @link http://developer.qiniu.com/fusion/api/traffic-bandwidth
     */
    public CdnResult.BandwidthResult getBandwidthData(String[] domains, String startDate, String endDate,
                                                      String granularity) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("startDate", startDate);
        req.put("endDate", endDate);
        req.put("granularity", granularity);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/bandwidth";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = client.post(url, body, headers, Client.JsonMime);
        return response.jsonToObject(CdnResult.BandwidthResult.class);
    }

    /**
     * 获取域名访问流量数据
     *
     * @return 获取流量请求的回复
     * @link http://developer.qiniu.com/fusion/api/traffic-bandwidth
     */
    public CdnResult.FluxResult getFluxData(String[] domains, String startDate, String endDate,
                                            String granularity) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("startDate", startDate);
        req.put("endDate", endDate);
        req.put("granularity", granularity);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/flux";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = client.post(url, body, headers, Client.JsonMime);
        return response.jsonToObject(CdnResult.FluxResult.class);
    }

    /**
     * 获取CDN域名访问日志的下载链接
     *
     * @return 获取日志下载链接的回复
     * @link http://developer.qiniu.com/fusion/api/download-the-log
     */
    public CdnResult.LogListResult getCdnLogList(String[] domains, String logDate) throws QiniuException {
        HashMap<String, String> req = new HashMap<>();
        req.put("domains", StringUtils.join(domains, ";"));
        req.put("day", logDate);

        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/log/list";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        Response response = client.post(url, body, headers, Client.JsonMime);
        return response.jsonToObject(CdnResult.LogListResult.class);
    }

    /**
     * 构建标准的基于时间戳的防盗链
     *
     * @param host           自定义域名，例如 http://img.abc.com
     * @param fileName       待访问的原始文件名，必须是utf8编码，不需要进行urlencode
     * @param queryStringMap 业务自身的查询参数，必须是utf8编码，不需要进行urlencode
     * @param encryptKey     时间戳防盗链的签名密钥，从七牛后台获取
     * @param deadline       链接的有效期时间戳，是以秒为单位的Unix时间戳
     * @return signedUrl     最终的带时间戳防盗链的url
     * @link https://support.qiniu.com/question/195128
     */
    public static String createTimestampAntiLeechUrl(
            String host, String fileName, final StringMap queryStringMap, String encryptKey, long deadline)
            throws UnsupportedEncodingException, MalformedURLException, NoSuchAlgorithmException {
        String urlToSign;
        String encodedFileName = URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", "%20");
        if (queryStringMap != null && queryStringMap.size() > 0) {
            List<String> queryStrings = new ArrayList<String>();
            for (Map.Entry<String, Object> entry : queryStringMap.map().entrySet()) {
                StringBuilder queryStringBuilder = new StringBuilder();
                queryStringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                queryStringBuilder.append("=");
                queryStringBuilder.append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
                queryStrings.add(queryStringBuilder.toString());
            }
            urlToSign = String.format("%s/%s?%s", host, encodedFileName, StringUtils.join(queryStrings, "&"));
        } else {
            urlToSign = String.format("%s/%s", host, encodedFileName);
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
