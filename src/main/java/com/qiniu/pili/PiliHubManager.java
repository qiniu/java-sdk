package com.qiniu.pili;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

public class PiliHubManager extends PiliManager {
  public PiliHubManager(Auth auth) {
    super(auth);
  }

  public PiliHubManager(Auth auth, String server) {
    super(auth, server);
  }

  public PiliHubManager(Auth auth, String server, Client client) {
    super(auth, server, client);
  }

  public PiliHubManager(Auth auth, String server, Client client, String apihost, String apihttpscheme, String appname) {
    super(auth, server, client, apihost, apihttpscheme, appname);
  }

  /**
   * getHubList 查询直播空间列表
   * 参考文档：<a href="https://developer.qiniu.com/pili/9888/live-hub-list">直播空间列表</a>
   * GET /v2/hubs
   *
   * @return 获取直播空间列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliHubModel.HubListResult getHubList() throws QiniuException {
    String url = server + "/v2/hubs";
    Response response = get(url);
    return response.jsonToObject(PiliHubModel.HubListResult.class);
  }

  /**
   * getHubInfo 查询直播空间信息
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9889/live-hub-information">查询直播空间信息</a>
   * GET /v2/hubs/<hub>
   *
   * @param hub 请求直播空间
   * @return 获取直播空间列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliHubModel.HubInfoResult getHubInfo(String hub) throws Exception {
    String url = super.server + "/v2/hubs/" + hub;
    Response response = get(url);
    return response.jsonToObject(PiliHubModel.HubInfoResult.class);
  }

  /**
   * hubSecurity 修改直播空间推流鉴权配置
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9890/live-hub-publish-authentication">修改直播空间推流鉴权配置</a>
   * POST /v2/hubs/<hub>/security
   *
   * @param hub             请求直播空间名
   * @param publishSecurity 鉴权方式，可选推流鉴权类型为: expiry: 限时鉴权、expiry_sk: 限时鉴权SK
   * @param publishKey      密钥
   * @return 修改直播空间推流鉴权配置 是否错误
   * @throws QiniuException 异常
   */
  public QiniuException hubSecurity(String hub, String publishSecurity, String publishKey) throws QiniuException {
    StringMap req = new StringMap();
    req.put("hub", hub);
    req.put("publishSecurity", publishSecurity);
    req.put("publishKey", publishKey);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);

    String url = this.server + "/v2/hubs/" + hub + "/security";
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * hubHlsplus 修改直播空间 hls 低延迟配置
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9891/live-hub-hls-low-latency-configuration">修改直播空间
   * hls 低延迟配置</a>
   * POST /v2/hubs/<hub>/hlsplus
   *
   * @param hub     请求直播空间名
   * @param HlsPlus 是否开启 hls 低延迟
   * @return 修改直播空间 hls 低延迟配置 是否错误
   * @throws QiniuException 异常
   */
  public QiniuException hubHlsplus(PiliHubModel.HubHlsplusRequest param) throws QiniuException {
    byte[] body = Json.encode(param).getBytes(Constants.UTF_8);
    String url = this.server + "/v2/hubs/" + param.hub + "/security";
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * hubPersistence 修改直播空间存储配置
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9892/live-hub-bucket-configuration">修改直播空间存储配置</a>
   * POST /v2/hubs/<hub>/persistence
   *
   * @param param.hub                请求直播空间名
   * @param param.storageBucket      存储空间
   * @param param.liveDataExpireDays 存储过期时间
   * @return 修改直播空间存储配置是否错误
   * @throws QiniuException 异常
   */
  public QiniuException hubPersistence(PiliHubModel.HubPersistenceRequest param) throws QiniuException {
    byte[] body = Json.encode(param).getBytes(Constants.UTF_8);
    String url = this.server + "/v2/hubs/" + param.hub + "/persistence";
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * hubSnapshot 修改直播空间封面配置
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9892/live-hub-bucket-configuration">修改直播空间存储配置</a>
   * POST /v2/hubs/<hub>/snapshot
   *
   * @param param.hub              请求直播空间名
   * @param param.snapshotInterval SnapshotInterval 间隔时间
   * @return 获取直播空间列表请求的回复
   * @throws QiniuException 异常
   */
  public QiniuException hubSnapshot(PiliHubModel.HubSnapshotRequest param) throws QiniuException {
    byte[] body = Json.encode(param).getBytes(Constants.UTF_8);
    String url = this.server + "/v2/hubs/" + param.hub + "/snapshot";
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }
}
