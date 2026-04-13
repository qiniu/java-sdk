package com.qiniu.pili;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

public class PiliManager {

  // APIHost 标准 API 服务器地址
  protected static final String API_HOST = "pili.qiniuapi.com";

  // IAMAPIHost IAM(权限策略) API 服务器地址
  protected static final String IAMAPIHost = "pili-iam.qiniuapi.com";

  // APIHTTPScheme HTTP 模式
  protected static final String APIHTTPScheme = "http://";

  // APIHTTPSScheme HTTPS 模式
  protected static final String APIHTTPSScheme = "https://";

  // DefaultAppName 默认 AppName 名称
  protected static final String DefaultAppName = "pili";

  protected final Auth auth;
  protected final String server;
  protected final Client client;
  protected final String apihost;
  protected final String apihttpscheme;

  /*
   * PiliManager 使用七牛标准的管理鉴权方式
   *
   * @param auth - Auth 对象
   */
  public PiliManager(Auth auth) {
    this(auth, API_HOST);
  }

  public PiliManager(Auth auth, String server) {
    this(auth, server, new Client());
  }

  public PiliManager(Auth auth, String server, Client client) {
    this(auth, server, client, API_HOST, APIHTTPScheme, DefaultAppName);
  }

  public PiliManager(Auth auth, String server, Client client, String apihost, String apihttpscheme, String appname) {
    this.auth = auth;
    this.server = server;
    this.client = client;

    if (!apihost.isEmpty()) {
      this.apihost = apihost;
    } else {
      this.apihost = API_HOST;
    }

    if (!apihttpscheme.isEmpty()) {
      this.apihttpscheme = apihttpscheme;
    } else {
      this.apihttpscheme = APIHTTPScheme;
    }

    if (!appname.isEmpty()) {
      Client.setAppName(appname);
    } else {
      Client.setAppName(DefaultAppName);
    }
  }

  /*
   * 相关请求的方法列表
   */
  protected Response get(String url) throws QiniuException {
    StringMap headers = composeHeader(url, MethodType.GET.toString(), null, Client.FormMime);
    return client.get(url, headers);
  }

  protected Response get(String url, byte[] body) throws QiniuException {
    StringMap headers = composeHeader(url, MethodType.GET.toString(), body, Client.FormMime);
    return client.get(url, headers);
  }

  protected Response post(String url, byte[] body) throws QiniuException {
    StringMap headers = composeHeader(url, MethodType.POST.toString(), body, Client.JsonMime);
    return client.post(url, body, headers, Client.JsonMime);
  }

  protected Response put(String url, byte[] body) throws QiniuException {
    StringMap headers = composeHeader(url, MethodType.PUT.toString(), body, Client.JsonMime);
    return client.put(url, body, headers, Client.JsonMime);
  }

  protected Response delete(String url) throws QiniuException {
    StringMap headers = composeHeader(url, MethodType.DELETE.toString(), null, Client.DefaultMime);
    return client.delete(url, headers);
  }

  protected StringMap composeHeader(String url, String method, byte[] body, String contentType) {
    StringMap headers = auth.authorizationV2(url, method, body, contentType);
    headers.put("Content-Type", contentType);
    return headers;
  }
}
