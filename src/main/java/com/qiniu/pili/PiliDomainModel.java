package com.qiniu.pili;

import java.util.List;
import java.util.Map;

/**
 * 这里定义了Pili Domain响应相关的类
 */
public class PiliDomainModel {
  private PiliDomainModel() {

  }

  /**
   * 查询域名列表返回值
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9895/live-domain-list">域名列表</a>
   */
  public class DomainsListResult {
    /**
     * Domains 域名列表
     */
    public DomainsListItem[] domains;
  }

  /**
   * 查询域名列表项
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9895/live-domain-list">域名列表项</a>
   */
  public class DomainsListItem {
    /**
     * Type 域名类型
     */
    String type;
    /**
     * Domain 域名
     */
    String domain;
    /**
     * CNAME CNAME 
     */
    String cname;
    /**
     * CertEnable 是否配置 SSL 证书
     */
    Boolean certEnable;
    /**
     * CertName 证书名称
     */
    String certName;
  }

  /**
   * 查询域名信息返回值
   * 参考文档：<a href=
   * "https://developer.qiniu.com/pili/9896/live-domain-information">域名信息</a>
   */
  public class DomainInfoResult {
    /**
     * Domain 域名
     */
    public String domain;

    /**
     * Type 域名类型
     */
    public String type;

    /**
     * Cname CNAME
     */
    public String cname;

    /**
     * ConnectCallback 开播回调配置
     */
    public DomainCallbackConfig connectCallback;

    /**
     * DisconnectCallback 断播回调配置
     */
    public DomainCallbackConfig disconnectCallback;

    /**
     * IPLimit IP 访问限制
     */
    public DomainIPLimit iPLimit;

    /**
     * PlaySecurity 时间戳防盗链配置
     */
    public DomainPlaySecurity playSecurity;

    /**
     * 断流延迟配置
     * DisconnectDelay 单位:秒,针对直播流短时间内闪断重连的情况,不触发断流回调,避免因为短时间内频繁断流造成大量回调
     */
    public long disconnectDelay;

    /**
     * UrlRewrite URL 改写规则
     */
    public DomainUrlRewrite urlRewrite;

    /**
     * CertEnable 是否配置 SSL 证书
     */
    public boolean certEnable;

    /**
     * CertName 证书名称
     */
    public String certName;

    /**
     * Disable 域名是否为禁用状态
     */
    public boolean disable;
  }

  public class DomainCallbackConfig {
    /**
     * Type 回调类型
     * 可选回调类型为
     * 留空: 不开启回调功能
     * GET: 发送GET请求回调,请求参数携带在query中
     * FORM: 发送POST请求回调,请求参数携带在body中,Content-Type 为
     * application/x-www-form-urlencoded
     * JSON: 发送POST请求回调,请求参数携带在body中,Content-Type 为 application/json
     */
    public String type;

    /**
     * URL 回调地址
     * 支持魔法变量
     */
    public String url;

    /**
     * Timeout 超时时间
     * 与回调地址的 HTTP 连接超时时间,单位:秒
     * 默认值为 2 秒,配置范围为 0~10 秒
     */
    public long timeout;

    /**
     * Vars 请求参数
     * 支持魔法变量,至少需要一组请求参数,规则解析出错误的会设置成空字段
     */
    public Map<String, String> vars;

    /**
     * RetryTimes 重试次数
     * 可选范围 0~5 次
     */
    public int retryTimes;

    /**
     * RetryInterval 重试间隔
     * 可选范围 0~5 秒,单位:秒
     */
    public int retryInterval;

    /**
     * SuccessCode 回调成功的 http code
     * 为 0 表示通配
     */
    public int successCode;

    /**
     * FailCode 回调失败的 http code
     * 为 0 表示通配,当 SuccessCode 不为 0 的情况下生效
     */
    public int failCode;
  }

  public class DomainIPLimit {
    /**
     * WhiteList 白名单
     * 允许推拉流的 IP 列表，CIDR 类型
     * 配置白名单后,黑名单列表将失效
     */
    public List<String> whitelist;

    /**
     * BlackList 黑名单
     * 限制推拉流的 IP 列表,CIDR 类型
     */
    public List<String> blacklist;
  }

  public class DomainPlaySecurity {
    /**
     * Type 防盗链类型
     * 可选防盗链类型为
     * - 留空: 默认类型,表示继承直播空间级别配置
     * - none: 表示关闭鉴权
     * - tsStartMD5: 有效时间从 TsPart 表示的时间戳开始,到 Range 秒后截止
     * - tsExpireMD5: 有效时间从现在当前到 TsPart 表示的时间戳为止
     */
    public String type;

    /**
     * Key1 主密钥
     */
    public String key1;

    /**
     * Key2 副密钥
     * 两个密钥将同时生效,便于线上业务替换密钥
     */
    public String key2;

    /**
     * Range 有效时间
     * 当 Type 为 "tsStartMD5" 时生效,单位:秒
     */
    public int range;

    /**
     * Rule 签名规则
     * 支持魔法变量的规则,最终签算结果为所有变量的md5
     * - $(key): 密钥
     * - $(path): URL 中的 path 部分
     * - $(streamKey): URL 中的 hub/stream 部分
     * - $(streamTitle): URL 中的 stream 部分
     * - $(path_<number>): URL 中的 path 部分,<number> 表示 path 层级
     * - $(_<query>): URL 中的 query 字段,举例: key1=val,魔法变量中使用 $(_key1) 表示 val
     * 举例:
     * 配置Rule为: $(key)$(path)$(_t)
     * 魔法变量替换完成后: key/hub/streamTitle1634745600
     * 对结果进行md5计算,最终签算值为:3bc26fe6b35f5c7efab87778c5b27993
     */
    public String rule;

    /**
     * Rule2 签名规则 2
     * 两个签名规则将同时生效,便于线上业务更换签名规则
     */
    public String rule2;

    /**
     * TsPart 时间戳字段
     * URL中表示时间戳的字段名
     */
    public String tsPart;

    /**
     * TsBase 时间戳进制
     * 可选进制格式为 2-36,即 2 进制到 36 进制,默认使用16进制
     */
    public int tsBase;

    /**
     * SignPart 签名字段
     * URL中表示token的字段名
     */
    public String signPart;

    /**
     * GapDuration 时间误差值
     * 针对 tsExpireMD5 生效,避免因签算方与服务器本地时间误差造成的鉴权失败
     */
    public int gapDuration;
  }

  /**
   * DomainUrlRewrite URL 改写规则配置
   */
  public class DomainUrlRewrite {
    /**
     * rewriteRules 需要转换处理的 URL 规则配置
     */
    public List<DomainUrlRewriteItem> rewriteRules;
  }

  /**
   * DomainUrlRewriteItem URL 改写规则项
   */
  public static class DomainUrlRewriteItem {
    /**
     * Pattern 匹配规则
     */
    public String pattern;

    /**
     * Replace 改写规则
     */
    public String replace;
  }

  public static final String PUBLISH_RTMP = "publishRtmp";
  public static final String LIVE_RTMP = "liveRtmp";
  public static final String LIVE_HLS = "liveHls";
  public static final String LIVE_HDL = "liveHdl";

  /**
   * DomainURLRewriteRule URL 改写规则
   */
  public static class Rules {
    /**
     * Pattern 匹配规则
     * 针对完整URL的正则表达式，形式如：(.+)/live/(.+)/playlist.m3u8
     * 括号中的内容允许在 Replace 中使用${n}引用（n表示括号顺序）
     */
    public String pattern;

    /**
     * Replace 改写规则
     * 希望得到的改写结果，形式如：${1}/hub/${2}.m3u8
     * 改写后的URL应符合七牛的直播URL规范: <scheme>://<domain>/<hub>/<stream>[<ext>]?<query>
     */
    public String replace;

    public Rules(String pattern, String replace) {
      this.pattern = pattern;
      this.replace = replace;
    }
  }
}
