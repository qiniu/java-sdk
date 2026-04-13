package com.qiniu.pili;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PiliUrl {
  // RTMPPublishURL 生成 RTMP 推流地址
  public static String rtmpPublishUrl(String hub, String domain, String streamTitle) {
    return String.format("rtmp://%s/%s/%s", domain, hub, streamTitle);
  }

  /**
   * 生成 SRT 推流地址
   *
   * @param hub         云直播的 hub 名称
   * @param domain      云直播的推流域名
   * @param streamTitle 流名称
   * @return 生成的 SRT 推流地址
   */
  public static String srtPublishURL(String hub, String domain, String streamTitle) {
    // 使用 String.format() 方法格式化字符串，将 domain、SRTPort、hub 和 streamTitle 替换到对应的位置
    return String.format("srt://%s:%s?streamid=#!::h=%s/%s,m=publish,domain=%s", domain, PiliUrlModel.SRTPort, hub,
        streamTitle, domain);
  }

  /**
   * 生成 RTMP 播放地址
   *
   * @param hub         云直播的 hub 名称
   * @param domain      云直播的播放域名
   * @param streamTitle 流名称
   * @return 生成的 RTMP 播放地址
   */
  public static String rtmpPlayURL(String hub, String domain, String streamTitle) {
    // 使用 String.format() 方法格式化字符串，将 domain、hub 和 streamTitle 替换到对应的位置
    return String.format("rtmp://%s/%s/%s", domain, hub, streamTitle);
  }

  /**
   * 生成 HLS 播放地址
   *
   * @param hub         云直播的 hub 名称
   * @param domain      云直播的播放域名
   * @param streamTitle 流名称
   * @return 生成的 HLS 播放地址
   */
  public static String hlsPlayURL(String hub, String domain, String streamTitle) {
    // 使用 String.format() 方法格式化字符串，将 domain、hub 和 streamTitle 替换到对应的位置
    return String.format("https://%s/%s/%s.m3u8", domain, hub, streamTitle);
  }

  /**
   * 生成 HDL 播放地址
   *
   * @param hub         云直播的 hub 名称
   * @param domain      云直播的播放域名
   * @param streamTitle 流名称
   * @return 生成的 HDL 播放地址
   */
  public static String hdlPlayURL(String hub, String domain, String streamTitle) {
    // 使用 String.format() 方法格式化字符串，将 domain、hub 和 streamTitle 替换到对应的位置
    return String.format("https://%s/%s/%s.flv", domain, hub, streamTitle);
  }

  // 转换 SRT URL
  public static String convertSRTURL(URL url, String token) {
    Map<String, String> queryMap = new HashMap<>();
    String path = url.getPath().substring(1); // 去掉开头的斜杠
    for (String queryParam : url.getQuery().split("&")) {
      String[] queryPair = queryParam.split("=", 2);
      if (queryPair.length == 2) {
        queryMap.put(queryPair[0], queryPair[1]);
      }
    }
    queryMap.put("m", "publish");
    if (token != null && !token.isEmpty()) {
      queryMap.put("token", token);
    }

    List<String> queryPartList = new ArrayList<>();
    for (Map.Entry<String, String> entry : queryMap.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      try {
        String queryPart = key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        queryPartList.add(queryPart);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    String rawQuery = String.join(",", queryPartList);

    String host = url.getHost();
    if (!host.contains(":")) {
      host += ":" + PiliUrlModel.SRTPort;
    }

    return String.format("srt://%s?streamid=#!::h=%s,%s", host, path, rawQuery);
  }

  /**
   * 判断对象是否为其类型的零值
   *
   * @param obj 待判断对象
   * @return 是否为零值
   */
  public static boolean isZero(Object obj) {
    if (obj == null) {
      return true;
    } else if (obj instanceof Boolean) {
      return !((Boolean) obj);
    } else if (obj instanceof Number) {
      return ((Number) obj).doubleValue() == 0;
    } else if (obj instanceof Character) {
      return (Character) obj == '\0';
    } else if (obj instanceof CharSequence) {
      return ((CharSequence) obj).length() == 0;
    } else if (obj.getClass().isArray()) {
      for (int i = 0; i < Array.getLength(obj); i++) {
        if (!isZero(Array.get(obj, i))) {
          return false;
        }
      }
      return true;
    } else if (obj instanceof Map) {
      return ((Map<?, ?>) obj).isEmpty();
    } else if (obj instanceof Collection) {
      return ((Collection<?>) obj).isEmpty();
    } else if (obj instanceof Iterable) {
      return !((Iterable<?>) obj).iterator().hasNext();
    } else if (obj instanceof Iterator) {
      return !((Iterator<?>) obj).hasNext();
    } else {
      return false;
    }
  }

  /**
   * magicCtx 生成魔法变量
   * $(key): 密钥
   * $(path): URL 中的 path 部分
   * $(streamKey): URL 中的 hub/stream 部分
   * $(streamTitle): URL 中的 stream 部分
   * $(path_<number>): URL 中的 path 部分,<number> 表示 path 层级
   * $(_<query>): URL 中的 query 字段,举例: key1=val,魔法变量中使用 $(_key1) 表示 val
   */
  public Map<String, Object> magicCtx(URL u, List<String> query, String key) {
    String path = u.getPath();
    Map<String, Object> ctx = new HashMap<String, Object>();
    ctx.put("key", key);
    ctx.put("path", path);
    ctx.put("streamKey", parseStreamKey(path));
    ctx.put("streamTitle", parseStreamTitile(path));

    for (int k = 1; k <= path.split("/").length; k++) {
      ctx.put("path_" + k, path.split("/")[k - 1]);
    }

    for (String queryPart : query) {
      if (!queryPart.isEmpty()) {
        String q = "_" + queryPart.split("=")[0];
        ctx.put(q, queryPart.split("=")[1]);
      }
    }
    return ctx;
  }

  public String parseStreamKey(String path) {
    String noExt = path.trim().substring(0, path.lastIndexOf("."));
    return noExt.substring(1).trim();
  }

  public String parseStreamTitile(String path) {
    String noExt = path.trim().substring(0, path.lastIndexOf("."));
    String noFirstSlash = noExt.substring(1).trim();
    int slash = noFirstSlash.indexOf('/');
    return noFirstSlash.substring(slash + 1);
  }

}
