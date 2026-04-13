package com.qiniu.pili;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;

public class PiliDomainManager extends PiliManager {
  public PiliDomainManager(Auth auth) {
    super(auth);
  }

  public PiliDomainManager(Auth auth, String server) {
    super(auth, server);
  }

  public PiliDomainManager(Auth auth, String server, Client client) {
    super(auth, server, client);
  }

  public PiliDomainManager(Auth auth, String server, Client client, String apihost, String apihttpscheme,
      String appname) {
    super(auth, server, client, apihost, apihttpscheme, appname);
  }

  /**
   * 返回目标直播空间下的所有直播域名
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9895/live-domain-list">直播空间下的所有域名</a>
   * GET /v2/hubs/<hub>/domains
   *
   * @param hub 直播空间
   * @return 获取直播空间域名列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliDomainModel.DomainsListResult getDomainsList(String hub) throws QiniuException {
    String url = super.server + "/v2/hubs/" + hub + "/domains";
    Response response = get(url);
    return response.jsonToObject(PiliDomainModel.DomainsListResult.class);
  }

  /**
   * 
   * GetDomainInfo 查询域名信息
   * 
   * @param hub 直播空间 
   * 
   * @param domain 域名 
   * 
   * @return 获取域名信息请求的回复
   * 
   * @throws QiniuException
   */
  public PiliDomainModel.DomainInfoResult getDomainInfo(String hub, String domain) throws QiniuException {
    if (Objects.isNull(hub) || Objects.isNull(domain) || hub.isEmpty() || domain.isEmpty()) {
      throw new IllegalArgumentException("hub: " + hub + ", domain: " + domain + ", cannot be null or empty!");
    }

    String url = super.server + "/v2/hubs/" + hub + "/domains/" + domain;
    Response response = get(url);
    return response.jsonToObject(PiliDomainModel.DomainInfoResult.class);
  }

  /**
   * BindDomain 绑定直播域名
   * 参考文档：参考文档：<a href=
   * "https://developer.qiniu.com/pili/9897/live-new-domain">绑定直播域名</a>
   * POST /v2/hubs/<hub>/newdomains
   *
   * @param hub    直播空间
   * @param domain 域名
   * @param type   域名的类型
   * @return 是否绑定成功
   * @throws QiniuException 异常
   */
  public QiniuException bindDomain(String hub, String domain, String type) throws QiniuException {
    if (type.equals(PiliDomainModel.PUBLISH_RTMP) || type.equals(PiliDomainModel.LIVE_HDL)
        || type.equals(PiliDomainModel.LIVE_HLS) || type.equals(PiliDomainModel.PUBLISH_RTMP)) {
      // 是其中一个
      HashMap<String, String> req = new HashMap<>();
      req.put("hub", hub);
      req.put("domain", domain);
      req.put("type", type);
      byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
      String url = server + "/v2/hubs/" + hub + "/newdomains";
      Response response = post(url, body);
      return response.isOK() ? null : new QiniuException(response);
    } else {
      // 不是
      return new QiniuException(null, "Type Error!");
    }
  }

  /**
   * UnbindDomain 解绑直播域名
   * 参考文档：参考文档：<a href=
   * "https://developer.qiniu.com/pili/9898/live-delete-domains">解绑直播域名</a>
   * DELETE /v2/hubs/<hub>/domains/<domain>
   *
   * @param hub    直播空间
   * @param domain 域名
   * @return 是否解绑成功
   * @throws QiniuException 异常
   */
  public QiniuException unbindDomain(String hub, String domain) throws QiniuException {
    HashMap<String, String> req = new HashMap<>();
    req.put("hub", hub);
    req.put("domain", domain);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    String url = server + "/v2/hubs/" + hub + "/domains/" + domain;
    Response response = delete(url);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * BindVodDomain 绑定点播域名
   * 参考文档：参考文档：<a href=
   * "https://developer.qiniu.com/pili/9899/live-new-vod-domain">绑定点播域名</a>
   * POST /v2/hubs/<hub>/voddomain
   *
   * @param hub       直播空间
   * @param vodDomain 点播域名
   * @return 是否绑定成功
   * @throws QiniuException 异常
   */
  public QiniuException bindVodDomain(String hub, String vodDomain) throws QiniuException {
    HashMap<String, String> req = new HashMap<>();
    req.put("hub", hub);
    req.put("vodDomain", vodDomain);
    String url = server + "/v2/hubs/" + hub + "/voddomain";
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * SetDomainCert 修改域名证书配置
   * 参考文档：参考文档：<a href=
   * "https://developer.qiniu.com/pili/9900/live-domain-certificate">绑定点播域名</a>
   * POST /v2/hubs/<hub>/domains/<domain>/cert
   *
   * @param hub      直播空间
   * @param domain   域名
   * @param certName 证书名称
   * @return 是否绑定成功
   * @throws QiniuException 异常
   */
  public QiniuException setDomainCert(String hub, String domain, String certName) throws Exception {
    HashMap<String, String> req = new HashMap<>();
    req.put("hub", hub);
    req.put("domain", domain);
    req.put("certName", certName);
    String url = server + "/v2/hubs/" + hub + "/domains/" + domain + "/cert";
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * SetDomainURLRewrite 修改域名改写规则配置
   * POST /v2/hubs/<hub>/domains/<domain>/urlrewrite
   *
   * 可根据业务需求自定义推拉流URL
   * 改写后的URL应符合七牛的直播URL规范: <scheme>://<domain>/<hub>/<stream>[.<ext>]?<query>
   * 举例
   * 匹配规则: (.+)/live/(.+)/playlist.m3u8
   * 改写规则: ${1}/hub/${2}.m3u8
   * 请求URL: https://live.qiniu.com/live/stream01/playlist.m3u8 ; 改写URL:
   * https://live.qiniu.com/hub/stream01.m3u8
   * 请求URL: https://live.qiniu.com/live/stream01.m3u8 ; 与规则不匹配,不做改写
   *
   * @param hub    直播空间
   * @param domain 域名
   * @param rules  规则
   * @return 是否修改成功
   * @throws QiniuException
   */
  public QiniuException setDomainURLRewrite(String hub, String domain, List<PiliDomainModel.Rules> rules)
      throws Exception {
    HashMap<String, Object> req = new HashMap<>();
    req.put("rules", rules);
    req.put("hub", hub);
    req.put("domain", domain);
    String url = server + "/v2/hubs/" + hub + "/domains/" + domain + "/urlrewrite";
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }
}
