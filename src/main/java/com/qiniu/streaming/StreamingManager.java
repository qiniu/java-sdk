package com.qiniu.streaming;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.streaming.model.ActivityRecords;
import com.qiniu.streaming.model.StreamAttribute;
import com.qiniu.streaming.model.StreamListing;
import com.qiniu.streaming.model.StreamStatus;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;

import java.util.Iterator;

/**
 * 该类封装了直播服务端API的功能
 * 参考文档：<a href="https://developer.qiniu.com/pili/api/the-server-api-reference">直播服务端API参考</a>
 */
public final class StreamingManager {
    private final String apiServer;
    private final String hub;
    private final Client client;
    private final Auth auth;

    /**
     * 构建一个直播流管理对象
     *
     * @param auth Auth对象
     * @param hub  直播应用名称
     */
    public StreamingManager(Auth auth, String hub) {
        this(auth, hub, "http://pili.qiniuapi.com");
    }

    public StreamingManager(Auth auth, String hub, String server) {
        this.apiServer = server;
        this.hub = hub;
        this.auth = auth;
        client = new Client();
    }

    public StreamingManager(Auth auth, String hub, String sever, Client client) {
        this.apiServer = sever;
        this.hub = hub;
        this.auth = auth;
        this.client = client;
    }

    /**
     * 创建一个新的直播流对象，其鉴权方式默认和直播应用设置的鉴权方式一致
     *
     * @param streamKey 直播流名称，可包含 字母、数字、中划线、下划线；1 ~ 200 个字符长
     */
    public void create(String streamKey) throws QiniuException {
        String path = "";
        String body = new StringMap().put("key", streamKey).jsonString();
        post(path, body, null);
    }

    /**
     * 获取流对象的相关信息
     *
     * @param streamKey 直播流名称
     */
    public StreamAttribute attribute(String streamKey) throws QiniuException {
        String path = encodeKey(streamKey);
        return get(path, StreamAttribute.class);
    }

    /**
     * 根据前缀获取流列表的迭代器
     *
     * @param live   是否在推流
     * @param prefix 文件名前缀
     * @return Stream迭代器
     */
    public ListIterator createStreamListIterator(boolean live, String prefix) {
        return new ListIterator(live, prefix);
    }

    /**
     * 获取直播流列表
     *
     * @param live   是否直播中
     * @param prefix 流名称前缀
     * @param marker 下一次列举的位置
     */
    public StreamListing listStreams(boolean live, String prefix, String marker) throws QiniuException {
        StringMap map = new StringMap();

        map.putWhen("liveonly", live, live);
        map.putNotEmpty("prefix", prefix);
        map.putNotEmpty("marker", marker);

        String path = "";

        if (map.size() != 0) {
            path += "?" + map.formString();
        }
        return get(path, StreamListing.class);
    }

    /**
     * 禁用流
     *
     * @param streamKey         流名称
     * @param expireAtTimestamp 禁用截至时间戳，单位秒
     */
    public void disableTill(String streamKey, long expireAtTimestamp) throws QiniuException {
        String path = encodeKey(streamKey) + "/disabled";
        String body = String.format("{\"disabledTill\":%d}", expireAtTimestamp);
        post(path, body, null);
    }

    /**
     * 启用流
     *
     * @param streamKey 流名称
     */
    public void enable(String streamKey) throws QiniuException {
        disableTill(streamKey, 0);
    }

    /**
     * 获取流状态
     *
     * @param streamKey 流名称
     */
    public StreamStatus status(String streamKey) throws QiniuException {
        String path = encodeKey(streamKey) + "/live";
        return get(path, StreamStatus.class);
    }

    /**
     * 从直播流数据中录制点播，该方法录制的时间段为整个流开始和结束时间
     *
     * @param streamKey 流名称
     * @param fileName  录制后保存的文件名
     */
    public String saveAs(String streamKey, String fileName) throws QiniuException {
        return saveAs(streamKey, fileName, 0, 0);
    }

    /**
     * * 从直播流数据中录制点播，该方法可以指定录制的时间段
     *
     * @param streamKey 流名称
     * @param fileName  录制后保存的文件名
     * @param start     录制开始的时间戳，单位秒
     * @param end       录制结束的时间戳，单位秒
     */
    public String saveAs(String streamKey, String fileName, long start, long end) throws QiniuException {
        return saveAs(streamKey, fileName, start, end, null);
    }


    /**
     * * 从直播流数据中录制点播，该方法可以指定录制的时间段
     *
     * @param streamKey 流名称
     * @param fileName  录制后保存的文件名
     * @param start     录制开始的时间戳，单位秒
     * @param end       录制结束的时间戳，单位秒
     * @param other     文档中指定的其它参数
     */
    public String saveAs(String streamKey, String fileName, long start, long end, StringMap other)
            throws QiniuException {
        String path = encodeKey(streamKey) + "/saveas";
        StringMap param = other != null ? other : new StringMap();
        param.putNotEmpty("fname", fileName).put("start", start).put("end", end);
        String body = param.jsonString();
        SaveRet r = post(path, body, SaveRet.class);
        return r.fname;
    }


    /**
     * 获取流推流的片段列表，一个流开始和断流算一个片段
     *
     * @param streamKey 流名称
     * @param start     开始时间戳，单位秒
     * @param end       结束时间戳，单位秒
     */
    public ActivityRecords history(String streamKey, long start, long end) throws QiniuException {
        if (start <= 0 || end < 0 || (start >= end && end != 0)) {
            throw new QiniuException(new IllegalArgumentException("bad argument" + start + "," + end));
        }
        String path = encodeKey(streamKey) + "/historyactivity?start=" + start;
        if (end != 0) {
            path += "&end=" + end;
        }
        return get(path, ActivityRecords.class);
    }

    private String encodeKey(String key) {
        return "/" + UrlSafeBase64.encodeToString(key);
    }

    private <T> T get(String path, Class<T> classOfT) throws QiniuException {
        String url = apiServer + "/v2/hubs/" + hub + "/streams" + path;
        StringMap headers = auth.authorizationV2(url);
        Response r = client.get(url, headers);
        if (classOfT != null) {
            return r.jsonToObject(classOfT);
        }
        return null;
    }

    private <T> T post(String path, String body, Class<T> classOfT) throws QiniuException {
        String url = apiServer + "/v2/hubs/" + hub + "/streams" + path;
        byte[] b = body.getBytes();
        StringMap headers = auth.authorizationV2(url, "POST", b, Client.JsonMime);
        Response r = client.post(url, b, headers, Client.JsonMime);
        if (classOfT != null) {
            return r.jsonToObject(classOfT);
        }
        return null;
    }

    private static class SaveRet {
        public String fname;
    }

    /**
     * 获取流列表迭代器
     */
    public class ListIterator implements Iterator<String[]> {
        private final boolean live;
        private String marker = null;
        private String prefix;
        private QiniuException exception = null;

        public ListIterator(boolean live, String prefix) {
            this.live = live;
            this.prefix = prefix;
        }

        public QiniuException exception() {
            return exception;
        }

        @Override
        public boolean hasNext() {
            return exception == null && !"".equals(marker);
        }

        @Override
        public String[] next() {
            try {
                StreamListing l = listStreams(live, prefix, marker);
                this.marker = l.marker == null ? "" : l.marker;
                return l.keys();
            } catch (QiniuException e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
