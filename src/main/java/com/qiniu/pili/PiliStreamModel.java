package com.qiniu.pili;

import java.util.List;

public class PiliStreamModel {

  public PiliStreamModel() {
  }

  /**
   * 请求查询直播流列表参数
   */
  public class GetStreamListRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * LiveOnly 只返回当前在线的流
     */
    public boolean liveOnly;

    /**
     * Prefix 流名称前缀匹配
     */
    public String prefix;

    /**
     * Limit 返回值数量限制
     */
    public String limit;

    /**
     * Marker 从该位置开始返回
     */
    public String marker;
  }

  /**
   * 查询直播流列表返回值
   */
  public class GetStreamsListResponse {
    /**
     * Items 流列表
     */
    public List<GetStreamsListResponseItem> items;

    /**
     * Marker 表示当前位置,若marker为空,表示遍历完成
     */
    public String marker;
  }

  public class GetStreamsListResponseItem {
    /**
     * Key 流名
     */
    public String key;
  }

  /**
   * 查询直播流信息请求参数
   */
  public class GetStreamBaseInfoRequest {
    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;
  }

  /**
   * 查询直播流信息返回值
   */
  public class GetStreamBaseInfoResponse {

    /**
     * CreatedAt 直播流创建时间
     * Unix 时间戳,单位:秒
     */
    public long createdAt;

    /**
     * UpdatedAt 直播流更新时间
     * Unix 时间戳,单位:秒
     */
    public long updatedAt;

    /**
     * ExpireAt 直播流过期时间
     * 默认流过期时间15天,Unix 时间戳,单位:秒
     */
    public long expireAt;

    /**
     * DisabledTill 禁用结束时间
     */
    public int disabledTill;

    /**
     * Converts 转码配置
     */
    public List<String> converts;

    /**
     * Watermark 是否开启水印
     */
    public boolean watermark;

    /**
     * PublishSecurity 推流鉴权类型
     */
    public String publishSecurity;

    /**
     * PublishKey 推流密钥
     */
    public String publishKey;

    /**
     * NropEnable 是否开启鉴黄
     */
    public boolean nropEnable;
  }

  /**
   * 禁用直播流请求参数
   */
  public class StreamDisabledRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;

    /**
     * DisabledTill 禁用结束时间
     * Unix 时间戳,单位:秒
     * 特殊值 -1 表示永久禁用;0 解除禁用
     */
    public int disabledTill;

    /**
     * DisablePeriodSecond 禁用时长
     * 单位:秒
     * 当 DisabledTill 为0时,DisablePeriodSecond 参数生效
     */
    public int disablePeriodSecond;
  }

  /**
   * 查询直播流实时信息请求参数
   */
  public class GetStreamLiveStatusRequest {
    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;
  }

  /**
   * 直播流实时状态
   */
  public class StreamLiveStatus {
    /**
     * StartAt 推流开始时间
     * Unix 时间戳,单位:秒
     */
    public long startAt;

    /**
     * ClientIP 推流端IP
     */
    public String clientIP;

    /**
     * ServerIP 服务端IP
     */
    public String serverIP;

    /**
     * Bps 推流码率
     */
    public int bps;

    /**
     * Fps 帧率
     */
    public StreamLiveStatusFPS fps;

    /**
     * Key 流名
     */
    public String key;
  }

  /**
   * 流实时帧率
   */
  public class StreamLiveStatusFPS {

    /**
     * Audio 音频帧率
     */
    public int audio;

    /**
     * Video 视频帧率
     */
    public int video;

    /**
     * Data metadata帧率
     */
    public int data;
  }

  /**
   * 查询直播流实时信息返回值
   */
  public class GetStreamLiveStatusResponse {

    /**
     * StreamLiveStatus 直播流实时状态
     */
    public StreamLiveStatus streamLiveStatus;

    /**
     * VideoBitRate 视频码率,单位:bps
     */
    public int videoBitRate;

    /**
     * AudioBitRate 音频码率,单位:bps
     */
    public int audioBitRate;
  }

  /**
   * 批量查询直播实时状态请求参数
   */
  public class BatchGetStreamLiveStatusRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Items 流列表 查询流列表数量不超过100
     */
    public List<String> items;
  }

  /**
   * 批量查询直播实时状态返回值
   */
  public class BatchGetStreamLiveStatusResponse {
    /**
     * Items 流列表
     */
    public List<StreamLiveStatus> items;
  }

  /**
   * 查询直播流推流记录请求参数
   */
  public class GetStreamHistoryRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;

    /**
     * Start 开始时间
     * 单位:秒,默认为0
     */
    public long start;

    /**
     * End 结束时间
     * 单位:秒,默认为当前时间
     */
    public long end;
  }

  /**
   * 查询直播流推流记录返回值
   */
  public class GetStreamHistoryResponse {

    /**
     * Items 直播流推流记录列表
     */
    public List<GetStreamHistoryItem> items;
  }

  /**
   * 查询直播流推流记录项
   */
  public class GetStreamHistoryItem {

    /**
     * Start 推流开始时间
     */
    public long start;

    /**
     * End 推流结束时间
     */
    public long end;

    /**
     * ClientIP 推流端IP
     */
    public String clientIP;

    /**
     * ServerIP 服务端IP
     */
    public String serverIP;
  }

  /**
   * 录制直播回放请求参数
   */
  public class StreamSaveasRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;

    /**
     * Start 开始时间
     * Unix 时间戳,单位:秒
     */
    public long start;

    /**
     * End 结束时间
     * Unix 时间戳,单位:秒,默认为当前时间
     */
    public long end;

    /**
     * Fname 文件名
     * 为空时会随机生成一个文件名
     */
    public String fname;

    /**
     * Format
     * 文件格式
     * 可选文件格式为
     * - m3u8: HLS格式,默认值
     * - flv: FLV格式,将回放切片转封装为单个flv文件,异步模式
     * - mp4: MP4格式,将回放切片转封装为单个mp4文件,异步模式
     * 异步模式下,生成回放文件需要一定时间
     */
    public String format;

    /**
     * Pipeline
     * 异步模式时,指定数据处理的队列
     * 可以将优先级较高的任务配置到独立的队列中进行执行
     * 参考文档:https://developer.qiniu.com/dora/kb/2500/streaming-media-queue-about-seven-cows
     */
    public String pipeline;

    /**
     * Notify 回调地址
     * 异步模式完成任务后的回调通知地址,不指定表示不做回调
     */
    public String notify;

    /**
     * ExpireDays 切片文件的生命周期
     * - 0: 默认值,表示修改ts文件生命周期为永久保存
     * - >0: 表示修改ts文件的的生命周期为 ExpireDays 参数值
     * - -1: 表示不修改ts文件的expire属性,可显著提升接口响应速度
     */
    public int expireDays;

    /**
     * PersistentDeleteAfterDays 生成文件的生命周期
     * - 0: 默认值,表示生成文件(m3u8/flv/mp4)永久保存
     * - >0: 表示生成文件(m3u8/flv/mp4)的生命周期为 PersistentDeleteAfterDays 参数值
     * m3u8文件只有当ExpireDays为-1时生效
     */
    public int persistentDeleteAfterDays;

    /**
     * FirstTsType 过滤ts切片文件类型
     * 部分非标准的直播流,在推流初期缺少视频帧或音频帧,过滤功能可以剔除这部分切片
     * - 0: 默认值,不做过滤
     * - 1: 第一个ts切片需要是纯视频类型,不符合预期的ts切片将被跳过
     * - 2: 第一个ts切片需要是纯音频类型,不符合预期的ts切片将被跳过
     * - 3: 第一个ts切片需要是音视频类型,不符合预期的ts切片将被跳过
     */
    public byte firstTsType;
  }

  /**
   * 录制直播回放返回值
   */
  public class StreamSaveasResponse {

    /**
     * Fname 文件名
     */
    public String fname;

    /**
     * Start 开始时间
     * Unix 时间戳,单位:秒
     */
    public long start;

    /**
     * End 结束时间
     * Unix 时间戳,单位:秒
     */
    public long end;

    /**
     * PersistentID 异步任务ID
     */
    public String persistentID;
  }

  /**
   * 保存直播截图请求参数
   */
  public class StreamSnapshotRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;

    /**
     * Time 截图时间
     * Unix 时间戳,单位:秒,不指定则为当前时间
     */
    public int time;

    /**
     * Fname 文件名
     * 不指定系统会随机生成
     */
    public String fname;

    /**
     * Format 文件格式
     * 默认为jpg,支持选择jpg/png
     */
    public String format;

    /**
     * Pipeline 异步模式时,指定数据处理的队列
     * 可以将优先级较高的任务配置到独立的队列中进行执行
     * 参考文档:https://developer.qiniu.com/dora/kb/2500/streaming-media-queue-about-seven-cows
     */
    public String pipeline;

    /**
     * Notify 回调地址
     * 若指定回调地址,则截图动作为异步模式
     */
    public String notify;

    /**
     * DeleteAfterDays 生命周期
     * - 0: 默认值,表示截图文件永久保存,单位:天
     */
    public int deleteAfterDays;
  }

  /**
   * 保存直播截图返回值
   */
  public class StreamSnapshotResponse {

    /**
     * Fname 文件名
     */
    public String fname;

    /**
     * PersistentID 异步任务ID
     */
    public String persistentID;
  }

  /**
   * 修改直播流转码配置请求参数
   */
  public class StreamConvertsRequest {

    /**
     * Hub 直播空间
     */
    public String hub;

    /**
     * Stream 流名
     */
    public String stream;

    /**
     * Converts 转码配置
     */
    public String[] converts;
  }
}
