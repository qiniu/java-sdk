package com.qiniu.pili;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class PiliStatManager extends PiliManager {
  public PiliStatManager(Auth auth) {
    super(auth);
  }

  public PiliStatManager(Auth auth, String server) {
    super(auth, server);
  }

  public PiliStatManager(Auth auth, String server, Client client) {
    super(auth, server, client);
  }

  public PiliStatManager(Auth auth, String server, Client client, String apihost, String apihttpscheme,
      String appname) {
    super(auth, server, client, apihost, apihttpscheme, appname);
  }

  /**
   * FlowDefaultSelect 上下行流量默认查询字段
   */
  public static final String FLOW_DEFAULT_SELECT = "flow";

  /**
   * CodecDefaultSelect 转码使用量默认查询字段
   */
  public static final String CODEC_DEFAULT_SELECT = "duration";

  /**
   * NropDefaultSelect 鉴黄使用量默认查询字段
   */
  public static final String NROP_DEFAULT_SELECT = "count";

  /**
   * getStatUpflow 获取上行流量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9862/live-statd-upflow">获取上行流量</a>
   * GET
   * /statd/upflow?$hub=<hub>&$domain=<domain>&$area=area&begin=<begin>&end=<end>&g=<g>&select=flow
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取上行流量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatUpflow(PiliStatModel.GetStatUpflowRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = FLOW_DEFAULT_SELECT;
    }

    String url = server + "/statd/upflow";
    StringMap queryMap = new StringMap();
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.put("end", param.commonRequest.end);
    queryMap.put("select", param.select);
    queryMap.putNotNull("hub", param.where.get("hub").toString());
    queryMap.putNotNull("domain", param.where.get("domain").toString());
    queryMap.putNotNull("area", param.where.get("area").toString());
    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);

    Response response = get(requestUrl);
    if (response == null) {
      throw new QiniuException(response);
    }
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * groupStatUpflow 分组获取上行流量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9862/live-statd-upflow">分组获取上行流量</a>
   * GET
   * /statd/upflow?$hub=<hub>&$domain=<domain>&$area=area&begin=<begin>&end=<end>&g=<g>&group=<group>&select=flow
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取上行流量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatUpflow(PiliStatModel.GroupStatUpflowRequest param)
      throws QiniuException {

    if (param.select.isEmpty()) {
      param.select = FLOW_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/upflow";
    StringMap queryMap = new StringMap();
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.put("end", param.commonRequest.end);
    queryMap.put("select", param.select);
    queryMap.putNotNull("group", param.group);
    queryMap.putNotNull("hub", param.where.get("hub").toString());
    queryMap.putNotNull("domain", param.where.get("domain").toString());
    queryMap.putNotNull("area", param.where.get("area").toString());
    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);

    Response response = get(requestUrl);
    if (response == null) {
      throw new QiniuException(response);
    }
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }

  /**
   * getStatUpflow 查询直播下行流量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9863/live-statd-downflow">获取直播下行流量</a>
   * GET
   * /statd/downflow?$hub=<hub>&$domain=<domain>&$area=area&begin=<begin>&end=<end>&g=<g>&group=<group>&select=flow
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取下行流量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatDownflow(PiliStatModel.GetStatDownflowRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = FLOW_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/downflow";
    StringMap queryMap = new StringMap();
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.put("end", param.commonRequest.end);
    queryMap.put("select", param.select);
    queryMap.putNotNull("hub", param.where.get("hub").toString());
    queryMap.putNotNull("domain", param.where.get("domain").toString());
    queryMap.putNotNull("area", param.where.get("area").toString());
    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);

    Response response = get(requestUrl);
    if (response == null) {
      throw new QiniuException(response);
    }
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * groupStatDownflow 分组获取下行流量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9862/live-statd-upflow">获取上行流量</a>
   * GET
   * /statd/upflow?$hub=<hub>&$domain=<domain>&$area=area&begin=<begin>&end=<end>&g=<g>&select=flow
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取下行流量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatDownflow(PiliStatModel.GroupStatDownflowRequest param)
      throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = FLOW_DEFAULT_SELECT;
    }

    String url = server + "/statd/downflow";
    StringMap queryMap = new StringMap();
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.put("end", param.commonRequest.end);
    queryMap.put("select", param.select);
    queryMap.put("group", param.group);
    queryMap.putNotNull("hub", param.where.get("hub").toString());
    queryMap.putNotNull("domain", param.where.get("domain").toString());
    queryMap.putNotNull("area", param.where.get("area").toString());
    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);

    Response response = get(requestUrl);
    if (response == null) {
      throw new QiniuException(response);
    }
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }

  /**
   * getStatCodec 获取直播转码使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9864/live-statd-transcoding-usage">获取直播转码使用量</a>
   * GET
   * /statd/codec?$hub=<hub>&$profile=<profile>&begin=<begin>&end=<end>&g=<g>&group=<group>&select=duration
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取直播转码使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatCodec(PiliStatModel.GetStatCodecRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = CODEC_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/codec";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * GroupStatCodec 分组获取直播转码使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9864/live-statd-transcoding-usage">获取直播转码使用量</a>
   * GET
   * /statd/codec?$hub=<hub>&$profile=<profile>&begin=<begin>&end=<end>&g=<g>&select=duration
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              值字段，用于返回需要查询的数据。 duration 时长，单位：毫秒
   * @param param.where               查询where条件 hub 直播空间 profile 转码规格
   * @return 获取直播转码使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatCodec(PiliStatModel.GroupStatCodecRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = CODEC_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/codec";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("group", param.group);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }

  /**
   * getStatNrop 获取直播鉴黄使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9865/live-statd-nrop-usage">获取鉴黄使用量</a>
   * GET
   * /statd/nrop?$assured=<false>&$hub=<hub>&begin=<begin>&end=<end>&g=<g>&select=count
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.select              值字段，用于返回需要查询的数据。可选值为flow，流量，单位：byte。带宽可以从流量转换，公式为
   *                                  带宽=流量*8/时间粒度，单位：bps
   * @param param.where               查询where条件 hub 直播空间 domain 域名 area 区域
   *                                  中国大陆(cn)、香港(hk)、台湾(tw)、亚太(apac)、美洲(am)、欧洲/中东/非洲(emea)
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatNrop(PiliStatModel.GetStatNropRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = NROP_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/nrop";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * GroupStatNrop 分组获取直播鉴黄使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9865/live-statd-nrop-usage">获取直播转码使用量</a>
   * GET
   * /statd/nrop?$assured=<false>&$hub=<hub>&begin=<begin>&end=<end>&g=<g>&group=<group>&select=count
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              值字段，用于返回需要查询的数据 count 鉴黄次数
   * @param param.where               查询where条件 hub 直播空间 assured
   *                                  鉴黄结果是否确定，true或false
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatNrop(PiliStatModel.GroupStatCodecRequest param) throws QiniuException {
    if (param.select.isEmpty()) {
      param.select = CODEC_DEFAULT_SELECT;
    }

    String url = this.server + "/statd/nrop";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("group", param.group);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }

  /**
   * getStatCaster 获取导播台使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9866/statd-caster-usage">获取导播台使用量</a>
   * GET
   * /statd/caster?$resolution=<resolution>&$container=<container>&begin=<begin>&end=<end>&g=<g>&select=<select>
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.select              值字段，用于返回需要查询的数据。upflow 上行流量，单位：byte /
   *                                  下行流量，单位：byte / 使用时长，单位：秒
   * @param param.where               查询where条件 container 容器 / resolution 分辨率
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatCaster(PiliStatModel.GetStatCasterRequest param) throws QiniuException {
    String url = this.server + "/statd/caster";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * groupStatCaster 分组获取导播台使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9866/statd-caster-usage">获取导播台使用量</a>
   * GET
   * /statd/caster?$resolution=<resolution>&$container=<container>&begin=<begin>&end=<end>&g=<g>&group=<group>&select=<select>
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              值字段，用于返回需要查询的数据。upflow 上行流量，单位：byte /
   *                                  下行流量，单位：byte / 使用时长，单位：秒
   * @param param.where               查询where条件 container 容器 / resolution 分辨率
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatCaster(PiliStatModel.GroupStatCasterRequest param)
      throws QiniuException {
    String url = this.server + "/statd/caster";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("group", param.group);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }

  /**
   * getStatPub 获取Pub服务使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9867/statd-pub-usage">查询Pub转推服务使用量</a>
   * GET
   * /statd/pub?$tp=<tp>&begin=<begin>&end=<end>&g=<g>&group=<group>&select=<select>
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              count 转推次数 / duration 转推时长，单位：毫秒
   * @param param.where               查询where条件 tp 状态
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatResponse[] getStatPub(PiliStatModel.GetStatPubRequest param) throws QiniuException {
    String url = this.server + "/statd/caster";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatResponse[].class);
  }

  /**
   * groupStatPub 分组获取Pub服务使用量
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9867/statd-pub-usage">分组获取Pub服务使用量</a>
   * GET GET
   * /statd/pub?$tp=<tp>&begin=<begin>&end=<end>&g=<g>&group=<group>&select=<select>
   *
   * @param param.commonRequest.begin 开始时间，支持格式：20060102、20060102150405
   * @param param.commonRequest.end   结束时间，支持格式：20060102、20060102150405，超过当前时间，则以当前时间为准，时间范围为左闭右开区间
   * @param param.commonRequest.g     时间粒度，可取值为 5min、hour、day、month
   * @param param.group               按特定条件将返回数据分组，可取值为条件字段
   * @param param.select              count 转推次数 / duration 转推时长，单位：毫秒
   * @param param.where               查询where条件 tp 状态
   * @return 获取直播鉴黄使用量请求的回复
   * @throws QiniuException 异常
   */
  public PiliStatModel.StatGroupResponse[] groupStatPub(PiliStatModel.GroupStatPubRequest param) throws QiniuException {
    String url = this.server + "/statd/caster";
    StringMap queryMap = new StringMap();
    queryMap.put("select", param.select);
    queryMap.put("begin", param.commonRequest.begin);
    queryMap.put("group", param.group);
    queryMap.put("g", param.commonRequest.g);
    queryMap.putWhen("end", param.commonRequest.end, !param.commonRequest.end.isEmpty());
    queryMap.putWhen("where", param.where, !param.where.isEmpty());

    String requestUrl = UrlUtils.composeUrlWithQueries(url, queryMap);
    Response response = get(requestUrl);
    return response.jsonToObject(PiliStatModel.StatGroupResponse[].class);
  }
}
