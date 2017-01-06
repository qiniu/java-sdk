package com.qiniu.streaming;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.streaming.model.ActivityRecords;
import com.qiniu.streaming.model.StreamAttribute;
import com.qiniu.streaming.model.StreamListing;
import com.qiniu.streaming.model.StreamStatus;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.Iterator;

/**
 * Created by bailong on 16/9/22.
 */
public final class StreamingManager {
    private final String apiServer;
    private final String hub;
    private final Client client;
    private final Auth auth;

    public StreamingManager(Auth auth, String hub) {
        this(auth, hub, "http://pili.qiniuapi.com");
    }

    StreamingManager(Auth auth, String hub, String server) {
        apiServer = server;
        this.hub = hub;
        this.auth = auth;
        client = new Client(null, false, null,
                Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.WRITE_TIMEOUT);
    }

    public void create(String key) throws QiniuException {
        String path = "";
        String body = String.format("{\"key\":\"%s\"}", key);
        post(path, body, null);
    }

    public StreamAttribute attribute(String key) throws QiniuException {
        String path = encodeKey(key);
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

    public StreamListing listStreams(boolean live, String prefix, String marker) throws QiniuException {
        StringMap map = new StringMap();
        if (live) {
            map.put("liveonly", live);
        }
        if (!StringUtils.isNullOrEmpty(prefix)) {
            map.put("prefix", prefix);
        }
        if (!StringUtils.isNullOrEmpty(marker)) {
            map.put("marker", marker);
        }
        String path = "";

        if (map.size() != 0) {
            path += "?" + map.formString();
        }
        return get(path, StreamListing.class);
    }

    public void disableTill(String key, long epoch) throws QiniuException {
        String path = encodeKey(key) + "/disabled";
        String body = String.format("{\"disabledTill\":%d}", epoch);
        post(path, body, null);
    }

    public void enable(String key) throws QiniuException {
        disableTill(key, 0);
    }

    public StreamStatus status(String key) throws QiniuException {
        String path = encodeKey(key) + "/live";
        return get(path, StreamStatus.class);
    }

    public String saveAs(String key, String fileName) throws QiniuException {
        return saveAs(key, fileName, 0, 0);
    }

    public String saveAs(String key, String fileName, long start, long end) throws QiniuException {
        String path = encodeKey(key) + "/saveas";
        String body;
        if (fileName == null) {
            body = String.format("{\"start\": %d,\"end\": %d}", start, end);
        } else {
            body = String.format("{\"fname\": %s,\"start\": %d,\"end\": %d}", fileName, start, end);
        }
        SaveRet r = post(path, body, SaveRet.class);
        return r.fname;
    }

    public ActivityRecords history(String key, long start, long end) throws QiniuException {
        if (start <= 0 || end < 0 || (start >= end && end != 0)) {
            throw new QiniuException(new IllegalArgumentException("bad argument" + start + "," + end));
        }
        String path = encodeKey(key) + "/historyactivity?" + start;
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
     * 获取文件列表迭代器
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
