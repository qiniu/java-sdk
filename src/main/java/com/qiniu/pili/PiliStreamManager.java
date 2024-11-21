package com.qiniu.pili;

import java.util.HashMap;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import com.qiniu.util.UrlUtils;

public class PiliStreamManager extends PiliManager {
  public PiliStreamManager(Auth auth) {
    super(auth);
  }

  public PiliStreamManager(Auth auth, String server) {
    super(auth, server);
  }

  public PiliStreamManager(Auth auth, String server, Client client) {
    super(auth, server, client);
  }

  public PiliStreamManager(Auth auth, String server, Client client, String apihost, String apihttpscheme,
      String appname) {
    super(auth, server, client, apihost, apihttpscheme, appname);
  }

  /**
   * getStreamsList
   * 参考文档：<a href="https://developer.qiniu.com/pili/2774/current-list">查询直播流列表</a>
   * GET
   * /v2/hubs/<Hub>/streams?liveonly=<true>&prefix=<prefix>&limit=<limit>&marker=<marker>
   *
   * @param hub      请求直播空间
   * @param liveonly 只返回当前在线的流，true 表示查询的是正在直播的流，不指定表示返回所有的流
   * @param prefix   流名称前缀匹配
   * @param limit    返回值数量限制，取值范围0～5000
   * @param marker   游标，表示从该位置开始返回
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.GetStreamsListResponse getStreamsList(String hub, Boolean liveOnly, String prefix,
      int limit, String marker)
      throws QiniuException {
    StringMap queryMap = new StringMap();
    queryMap.put("hub", hub);
    queryMap.put("liveonly", liveOnly);
    queryMap.put("prefix", prefix);
    queryMap.put("limit", limit);
    queryMap.put("marker", marker);
    String url = server + "/v2/hubs/" + hub;

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStreamModel.GetStreamsListResponse.class);
  }

  /**
   * getStreamBaseInfo 查询直播流信息
   * 参考文档：<a href="https://developer.qiniu.com/pili/2773/query-stream">查询直播流列表</a>
   * GET v2/hubs/<hub>/streams/<EncodedStreamTitle>
   *
   * @param hub    请求直播空间
   * @param stream 流
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.GetStreamBaseInfoResponse getStreamBaseInfo(String hub, String stream) throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/streams/" + encodeStream(stream);
    Response response = get(url);
    return response.jsonToObject(PiliStreamModel.GetStreamBaseInfoResponse.class);
  }

  /**
   * streamDisable 禁用直播流
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2775/off-the-air-flow">禁用直播流</a>
   * POST /v2/hubs/<hub>/streams/<EncodedStreamTitle>/disabled
   *
   * @param hub                 请求直播空间
   * @param stream              直播流名
   * @param disabledTill        禁播结束时间，Unix 时间戳，特殊值含义： -1 永久禁播；0 解除禁播
   * @param disablePeriodSecond 禁播时长，单位：秒，当 disabledTill 为0时，disablePeriodSecond
   *                            参数生效，值大于0
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public QiniuException streamDisable(String hub, String stream, int disabledTill, int disablePeriodSecond)
      throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/streams/" + encodeStream(stream) + "/disabled";
    HashMap<String, Object> req = new HashMap<>();
    req.put("hub", hub);
    req.put("encodedStreamTitle", encodeStream(stream));
    req.put("disabledTill", disabledTill);
    req.put("disablePeriodSecond", disablePeriodSecond);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * getStreamLiveStatus 查询直播流实时信息
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2776/live-broadcast-of-real-time-information">查询直播流实时信息</a>
   * GET v2/hubs/<hub>/streams/<EncodedStreamTitle>/live
   *
   * @param hub 请求直播空间
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.GetStreamLiveStatusResponse getStreamLiveStatus(String hub, String stream) throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/streams/" + encodeStream(stream) + "/live";
    Response response = get(url);
    return response.jsonToObject(PiliStreamModel.GetStreamLiveStatusResponse.class);
  }

  /**
   * batchGetStreamLiveStatus 批量查询直播实时状态
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/3764/batch-query-broadcast-real-time-information">批量查询直播实时状态</a>
   * POST /v2/hubs/<hub>/livestreams
   *
   * @param hub   请求直播空间
   * @param items 直播流列表，查询直播流列表数量不超过100
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.BatchGetStreamLiveStatusResponse batchGetStreamLiveStatus(String hub, String[] items)
      throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/livestreams";
    HashMap<String, String[]> req = new HashMap<>();
    req.put("item", items);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.jsonToObject(PiliStreamModel.BatchGetStreamLiveStatusResponse.class);
  }

  /**
   * getStreamHistory 查询直播流推流记录
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2778/live-history">查询直播流推流记录</a>
   * GET
   * /v2/hubs/<hub>/streams/<encodedStreamTitle>/historyactivity?start=<start>&end=<end>
   *
   * @param hub 请求直播空间
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.GetStreamHistoryResponse getStreamHistory(long start, long end, String hub, String stream)
      throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/streams/" + encodeStream(stream) + "/historyactivity";
    StringMap queryMap = new StringMap();
    queryMap.put("start", start);
    queryMap.put("end", end);
    Response response = get(url);
    if (response == null) {
      throw new RuntimeException("Null response!");
    }
    return response.jsonToObject(PiliStreamModel.GetStreamHistoryResponse.class);
  }

  /**
   * streamSaveas 录制直播回放
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2777/save-the-live-playback">录制直播回放</a>
   * POST /v2/hubs/<hub>/streams/<EncodedStreamTitle>/saveas
   *
   * @param hub                       请求直播空间
   * @param stream                    请求流名
   * @param start                     开始时间，Unix 时间戳，默认为第一次直播开始时间
   * @param end                       结束时间，Unix 时间戳，默认为当前时间
   * @param fname                     文件名，为空时会随机生成一个文件名
   * @param format                    文件格式，可选文件格式为：
   *                                  m3u8: HLS格式，默认值
   *                                  flv: FLV格式，将回放切片转封装为单个flv文件，异步模式
   *                                  mp4: MP4格式，将回放切片转封装为单个mp4文件，异步模式
   *                                  异步模式下，生成回放文件需要一定时间
   * @param pipeline                  异步模式时，指定数据处理的队列，可以将优先级较高的任务配置到独立的队列中进行执行
   * @param notify                    回调地址，异步模式完成任务后的回调通知地址，不指定表示不做回调，参考文档：状态查询
   *                                  同步模式下录制结果直接由接口返回，回调不生效
   * @param expireDays                切片文件的生命周期：
   *                                  0: 默认值，表示修改ts文件生命周期为永久保存
   *                                  >0: 表示修改ts文件的的生命周期为 ExpireDays 参数值
   *                                  -1: 表示不修改ts文件的expire属性，可显著提升接口响应速度
   * @param persistentDeleteAfterDays 生成文件的生命周期：
   *                                  0: 默认值，表示生成文件（m3u8/flv/mp4）永久保存
   *                                  >0: 表示生成文件（m3u8/flv/mp4）的生命周期为
   *                                  PersistentDeleteAfterDays 参数值
   *                                  注意，对于m3u8文件，只有当ExpireDays为-1时，persistentDeleteAfterDays才会生效，如果设置了切片文件的生命周期，那生成m3u8文件的生命周期会和切片文件ts的生命周期一致
   * @param firstTsType               过滤ts切片文件类型，部分非标准的直播流，在推流初期缺少视频帧或音频帧，过滤功能可以剔除这部分切片，
   *                                  0: 默认值，不做过滤
   *                                  1: 第一个ts切片需要是纯视频类型，不符合预期的ts切片将被跳过
   *                                  2: 第一个ts切片需要是纯音频类型，不符合预期的ts切片将被跳过
   *                                  3: 第一个ts切片需要是音视频类型，不符合预期的ts切片将被跳过
   * @return 获取直播流列表请求的回复
   * @throws QiniuException 异常
   */
  public PiliStreamModel.StreamSaveasResponse streamSaveas(PiliStreamModel.StreamSaveasRequest param) throws QiniuException {
    String url = server + "/v2/hubs/" + param.hub + "/streams/" + encodeStream(param.stream)
        + "/saveas";
    HashMap<String, Object> req = new HashMap<>();
    req.put("fname", param.fname);
    req.put("start", param.start);
    req.put("end", param.end);
    req.put("format", param.format);
    req.put("pipeline", param.pipeline);
    req.put("notify", param.notify);
    req.put("expireDays", param.expireDays);
    req.put("persistentDeleteAfterDays", param.persistentDeleteAfterDays);
    req.put("firstTsType", param.firstTsType);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.jsonToObject(PiliStreamModel.StreamSaveasResponse.class);
  }

  /**
   * streamSnapshot 保存直播截图
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2520/save-the-live-capture">禁用直播流</a>
   * POST /v2/hubs/<hub>/streams/<EncodedStreamTitle>/snapshot
   *
   * @param hub             请求直播空间
   * @param stream          直播流名
   * @param time            截图时间，Unix 时间戳，不指定则为当前时间
   * @param fname           文件名，不指定系统会随机生成
   * @param format          文件格式，默认为jpg，可取值为jpg和png
   * @param pipeline        异步模式时，指定数据处理的队列，可以将优先级较高的任务配置到独立的队列中进行执行
   * @param notify          回调地址，若指定回调地址，则截图动作为异步模式
   * @param deleteAfterDays 生命周期，- 0: 默认值，表示截图文件永久保存，单位：天
   * @return 获取直播流列表请求的回复
   * @throws StreamSnapshotResponse
   */
  public PiliStreamModel.StreamSnapshotResponse streamSnapshot(PiliStreamModel.StreamSnapshotRequest param)
      throws QiniuException {
    String url = server + "/v2/hubs/" + param.hub + "/streams/" + encodeStream(param.stream)
        + "/snapshot";
    HashMap<String, Object> req = new HashMap<>();
    req.put("time", param.time);
    req.put("fname", param.fname);
    req.put("format", param.format);
    req.put("pipeline", param.pipeline);
    req.put("notify", param.notify);
    req.put("deleteAfterDays", param.deleteAfterDays);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.jsonToObject(PiliStreamModel.StreamSnapshotResponse.class);
  }

  /**
   * streamConverts 修改直播流转码配置
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/2521/modify-the-flow-configuration">修改直播流转码配置</a>
   * POST /v2/hubs/<hub>/streams/<EncodedStreamTitle>/converts
   *
   * @param hub             请求直播空间
   * @param stream          直播流名
   * @param time            截图时间，Unix 时间戳，不指定则为当前时间
   * @param fname           文件名，不指定系统会随机生成
   * @param format          文件格式，默认为jpg，可取值为jpg和png
   * @param pipeline        异步模式时，指定数据处理的队列，可以将优先级较高的任务配置到独立的队列中进行执行
   * @param notify          回调地址，若指定回调地址，则截图动作为异步模式
   * @param deleteAfterDays 生命周期，- 0: 默认值，表示截图文件永久保存，单位：天
   * @return 获取直播流列表请求的回复
   * @throws QiniuException
   */
  public QiniuException streamConverts(String hub, String stream, String[] converts) throws QiniuException {
    String url = server + "/v2/hubs/" + hub + "/streams/" + encodeStream(stream) + "/converts";
    HashMap<String, String[]> req = new HashMap<>();
    req.put("converts", converts);
    byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
    Response response = post(url, body);
    return response.isOK() ? null : new QiniuException(response);
  }

  /**
   * 请求流名base64编码
   */
  public static String encodeStream(String str) {
    return UrlSafeBase64.encodeToString(str.getBytes());
  }
}
