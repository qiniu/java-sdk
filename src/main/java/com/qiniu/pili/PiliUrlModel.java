package com.qiniu.pili;

public class PiliUrlModel {
  private PiliUrlModel() {

  }

  /**
   * SRTScheme SRT 协议头
   */
  public static final String SRTScheme = "srt";

  /**
   * SRTPort SRT 协议端口
   * 截止目前,SRT 协议没有 IANA 官方约定的端口号
   * 七牛采用 1935/UDP 作为 SRT 协议端口号
   */
  public static final String SRTPort = "1935";

  /**
   * SecurityTypeStaticKeyPart 静态鉴权密钥字段名
   */
  public static final String SecurityTypeStaticKeyPart = "key";

  /**
   * SecurityTypeExpiryTsPart 推流限时鉴权时间戳字段名
   */
  public static final String SecurityTypeExpiryTsPart = "expire";

  /**
   * SecurityTypeExpirySkTsPart 推流限时鉴权sk时间戳字段名
   */
  public static final String SecurityTypeExpirySkTsPart = "e";

  /**
   * SecurityTypeDynamicNoncePart 动态鉴权随机值字段名
   */
  public static final String SecurityTypeDynamicNoncePart = "nonce";

  /**
   * SecurityTypeTokenPart 鉴权签算字段名
   * 用于 限时鉴权(expiry)、限时鉴权SK(expiry_sk)、动态鉴权(dynamic)
   */
  public static final String SecurityTypeTokenPart = "token";

  /**
   * 鉴权类型
   */
  public static final String SecurityTypeNull = "";

  /**
   * SecurityTypeNone 关闭鉴权
   * 用于推流 URL 签算(直播鉴权)和播放 URL 签算(时间戳防盗链)
   * 强制关闭鉴权,域名级别鉴权不继承直播空间级别鉴权配置
   */
  public static final String SecurityTypeNone = "none";

  /**
   * SecurityTypeStatic 静态鉴权
   * 用于推流 URL 签算(直播鉴权)
   */
  public static final String SecurityTypeStatic = "static";

  /**
   * SecurityTypeExpiry 限时鉴权
   * 用于推流 URL 签算(直播鉴权)
   */
  public static final String SecurityTypeExpiry = "expiry";

  /**
   * SecurityTypeExpirySK 限时鉴权SK
   * 用于推流 URL 签算(直播鉴权)
   */
  public static final String SecurityTypeExpirySK = "expiry_sk";

  /**
   * Deprecated: SecurityTypeDynamic 动态鉴权
   * 用于推流 URL 签算(直播鉴权),该鉴权类型即将移除,不建议使用
   */
  @Deprecated
  public static final String SecurityTypeDynamic = "dynamic";

  /**
   * SecurityTypeTsStartMD5 时间戳防盗链开始时间限制
   * 用于播放 URL 签算(时间戳防盗链)
   * 签算有效时间从 URL 的时间戳开始,直到超过有效时间(range)范围
   */
  public static final String SecurityTypeTsStartMD5 = "tsStartMD5";

  /**
   * SecurityTypeTsExpireMD5 时间戳防盗链结束时间限制
   * 用于播放 URL 签算(时间戳防盗链)
   * 签算有效时间从当前时间开始,直到 URL 的时间戳为止
   */
  public static final String SecurityTypeTsExpireMD5 = "tsExpireMD5";

  public class SignPublishURLArgs {
    /**
     * SecurityType
     * 鉴权类型
     */
    public String securityType;

    /**
     * PublishKey
     * 推流鉴权秘钥
     * 静态鉴权(static)、限时鉴权(expiry)、动态鉴权(dynamic) 类型必要参数
     */
    public String publishKey;

    /**
     * ExpireAt
     * 过期时间
     * 签算URL过期时间,单位:秒
     * 限时鉴权(expiry)、限时鉴权SK(expiry_sk) 类型必要参数
     */
    public long expireAt;

    /**
     * Nonce
     * 随机数
     * 要求每次推流请求的nonce值需要大于上一次推流请求的nonce值
     * 动态鉴权(dynamic) 鉴权类型必要参数
     */
    public int nonce;

    /**
     * AccessKey
     * 访问密钥
     * 限时鉴权SK(expiry_sk) 类型必要参数
     */
    public String accessKey;

    /**
     * SecretKey
     * 安全密钥
     * 限时鉴权SK(expiry_sk) 类型必要参数
     */
    public String secretKey;
  }

  public class SignPlayURLArgs {
    /**
     * SecurityType
     * 鉴权类型
     * 支持鉴权类型为
     * - tsStartMD5 时间戳防盗链开始时间限制
     * - tsExpireMD5 时间戳防盗链结束时间限制
     */
    public String securityType;

    /**
     * Key
     * 密钥
     */
    public String key;

    /**
     * Timestamp
     * 时间戳
     * 单位:秒
     * 鉴权类型为 tsStartMD5 时,表示鉴权开始时间
     * 鉴权类型为 tsExpireMD5 时,表示鉴权结束时间
     */
    public long timestamp;

    /**
     * Rule
     * 签名规则
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
     * TsPart 时间戳字段
     * URL 中表示时间戳的字段名
     */
    public String tsPart;

    /**
     * TsBase 时间戳进制
     * 可选进制格式为 2-36,即 2 进制到 36 进制,默认使用 16 进制
     */
    public int tsBase;

    /**
     * SignPart 签名字段
     * URL 中表示 token 的字段名
     */
    public String signPart;
  }
}
