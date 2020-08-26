package com.qiniu.cdn;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类主要提供CDN相关功能的接口实现，包括文件和目录的刷新，文件的预取，获取CDN访问日志链接，生成七牛时间戳防盗链等功能。
 * 参考文档：<a href="https://developer.qiniu.com/fusion">融合CDN</a>
 */
public final class CdnManager {
    /**
     * 单个请求最大刷新的链接数量
     */
    private static final int MAX_API_REFRESH_URL_COUNT = 60;
    /**
     * 单个请求最大刷新的目录数量
     */
    private static final int MAX_API_REFRESH_DIR_COUNT = 10;
    /**
     * 单个请求最大预取的链接数量
     */
    private static final int MAX_API_PREFETCH_URL_COUNT = 60;

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

    public CdnManager(Auth auth, String server, Client client) {
        this.auth = auth;
        this.server = server;
        this.client = client;
    }

    public static String createTimestampAntiLeechUrl(URL oUrl, String encryptKey, long deadline) throws QiniuException {
        try {
            String urlencodedPath = URLEncoder.encode(oUrl.getPath(), "UTF-8").replaceAll("%2F", "/");
            String query = oUrl.getQuery();
            String file = (query == null) ? urlencodedPath : (urlencodedPath + "?" + query);
            URL url = new URL(oUrl.getProtocol(), oUrl.getHost(), oUrl.getPort(), file);
            String expireHex = Long.toHexString(deadline);

            String toSignStr = String.format("%s%s%s", encryptKey, urlencodedPath, expireHex);
            String signedStr = StringUtils.md5Lower(toSignStr);

            String signedUrl;
            if (url.getQuery() != null) {
                signedUrl = String.format("%s&sign=%s&t=%s", url, signedStr, expireHex);
            } else {
                signedUrl = String.format("%s?sign=%s&t=%s", url, signedStr, expireHex);
            }

            return signedUrl;
        } catch (Exception e) {
            throw new QiniuException(e, "timestamp anti leech failed");
        }
    }

    /**
     * 构建七牛标准的基于时间戳的防盗链
     * 参考文档：<a href="https://support.qiniu.com/question/195128">时间戳防盗链</a>
     *
     * @param host           自定义域名，例如 http://img.abc.com
     * @param fileName       待访问的原始文件名，必须是utf8编码，不需要进行urlencode
     * @param queryStringMap 业务自身的查询参数，必须是utf8编码，不需要进行urlencode
     * @param encryptKey     时间戳防盗链的签名密钥，从七牛后台获取
     * @param deadline       链接的有效期时间戳，是以秒为单位的Unix时间戳
     * @return signedUrl     最终的带时间戳防盗链的url
     */
    public static String createTimestampAntiLeechUrl(
            String host, String fileName, final StringMap queryStringMap, String encryptKey, long deadline)
            throws QiniuException {
        URL urlObj = null;
        try {
            String urlToSign = null;
            if (queryStringMap != null && queryStringMap.size() > 0) {
                List<String> queryStrings = new ArrayList<String>();
                for (Map.Entry<String, Object> entry : queryStringMap.map().entrySet()) {
                    StringBuilder queryStringBuilder = new StringBuilder();
                    queryStringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                    queryStringBuilder.append("=");
                    queryStringBuilder.append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
                    queryStrings.add(queryStringBuilder.toString());
                }
                urlToSign = String.format("%s/%s?%s", host, fileName, StringUtils.join(queryStrings, "&"));
            } else {
                urlToSign = String.format("%s/%s", host, fileName);
            }

            urlObj = new URL(urlToSign);
        } catch (Exception e) {
            throw new QiniuException(e, "timestamp anti leech failed");
        }
        return createTimestampAntiLeechUrl(urlObj, encryptKey, deadline);
    }

    /**
     * 刷新链接列表，每次最多不可以超过 60 条链接
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/cache-refresh">缓存刷新</a>
     *
     * @param urls 待刷新文件外链列表
     * @return 刷新请求的回复
     */
    public CdnResult.RefreshResult refreshUrls(String[] urls) throws QiniuException {
        return refreshUrlsAndDirs(urls, null);
    }

    /**
     * 刷新目录列表，每次最多不可以超过 10 个目录
     * 刷新目录需要额外开通权限，可以联系七牛技术支持处理
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/cache-refresh">缓存刷新</a>
     *
     * @param dirs 待刷新目录列表
     * @return 刷新请求的回复
     */
    public CdnResult.RefreshResult refreshDirs(String[] dirs) throws QiniuException {
        return refreshUrlsAndDirs(null, dirs);
    }

    /**
     * 刷新文件外链和目录，外链每次不超过 60 个，目录每次不超过 10 个
     * 刷新目录需要额外开通权限，可以联系七牛技术支持处理
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/cache-refresh">缓存刷新</a>
     *
     * @param urls 待刷新文件外链列表
     * @param dirs 待刷新目录列表
     * @return 刷新请求的回复
     */
    public CdnResult.RefreshResult refreshUrlsAndDirs(String[] urls, String[] dirs) throws QiniuException {
        //check params
        if (urls != null && urls.length > MAX_API_REFRESH_URL_COUNT) {
            throw new QiniuException(new Exception("url count exceeds the max refresh limit per request"));
        }
        if (dirs != null && dirs.length > MAX_API_REFRESH_DIR_COUNT) {
            throw new QiniuException(new Exception("dir count exceeds the max refresh limit per request"));
        }

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
     * 预取文件链接，每次最多不可以超过 60 条
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/file-prefetching">文件预取</a>
     *
     * @param urls 待预取的文件外链列表
     * @return 预取请求的回复
     */
    public CdnResult.PrefetchResult prefetchUrls(String[] urls) throws QiniuException {
        if (urls != null && urls.length > MAX_API_PREFETCH_URL_COUNT) {
            throw new QiniuException(new Exception("url count exceeds the max prefetch limit per request"));
        }
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
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/traffic-bandwidth">流量带宽</a>
     *
     * @param domains     待获取数据的域名列表
     * @param startDate   开始日期，格式为：2017-02-18
     * @param endDate     截至日期，格式为：2017-02-20
     * @param granularity 数据粒度，支持的取值为 5min ／ hour ／day
     * @return 获取带宽数据请求的回复
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
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/traffic-bandwidth">流量带宽</a>
     *
     * @param domains     待获取数据的域名列表
     * @param startDate   开始日期，格式为：2017-02-18
     * @param endDate     截至日期，格式为：2017-02-20
     * @param granularity 数据粒度，支持的取值为 5min ／ hour ／day
     * @return 获取流量数据请求的回复
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
     * 获取CDN域名访问日志的下载链接，具体下载操作请自行根据链接下载
     * 参考文档：<a href="http://developer.qiniu.com/fusion/api/download-the-log">日志下载</a>
     *
     * @param domains 待获取日志下载信息的域名列表
     * @param logDate 待获取日志的具体日期，格式为：2017-02-18
     * @return 获取日志下载链接的回复
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
}
