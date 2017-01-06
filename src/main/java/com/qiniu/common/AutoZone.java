package com.qiniu.common;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Json;
import com.qiniu.util.UrlSafeBase64;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bailong on 16/9/15.
 */
final class AutoZone extends Zone {
    static AutoZone instance = new AutoZone();
    private final String ucServer;
    private Map<ZoneIndex, ZoneInfo> zones = new ConcurrentHashMap<>();
    private Client client;

    AutoZone() {
        this("https://uc.qbox.me");
    }

    AutoZone(String ucServer) {
        this.ucServer = ucServer;
        client = new Client();
    }

    private UCRet getZoneJson(ZoneIndex index) throws QiniuException {
        String address = ucServer + "/v1/query?ak=" + index.accessKey + "&bucket=" + index.bucket;

        Response r = client.get(address);
        return r.jsonToObject(UCRet.class);
    }

    // only for test public
    ZoneInfo zoneInfo(String ak, String bucket) throws QiniuException {
        ZoneIndex index = new ZoneIndex(ak, bucket);
        ZoneInfo info = zones.get(index);
        if (info == null) {
            UCRet ret = getZoneJson(index);
            try {
                info = ZoneInfo.buildFromUcRet(ret);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (info != null) {
                zones.put(index, info);
            }
        }
        return info;
    }

    // only for test public
    ZoneInfo queryByToken(String token) {
        try {
            // http://developer.qiniu.com/article/developer/security/upload-token.html
            // http://developer.qiniu.com/article/developer/security/put-policy.html
            String[] strings = token.split(":");
            String ak = strings[0];
            String policy = new String(UrlSafeBase64.decode(strings[2]), Constants.UTF_8);
            String bkt = Json.decode(policy).get("scope").toString().split(":")[0];
            return zoneInfo(ak, bkt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String upHost(String token) {
        ZoneInfo info = queryByToken(token);
        if (info == null) {
            return "";
        }
        return info.upHost;
    }

    public String upHostBackup(String token) {
        ZoneInfo info = queryByToken(token);
        if (info == null) {
            return "";
        }
        return info.upBackup;
    }

    public String upIpBackup(String token) {
        ZoneInfo info = queryByToken(token);
        if (info == null) {
            return "";
        }
        return info.upIp;
    }

    public String upHostHttps(String token) {
        ZoneInfo info = queryByToken(token);
        if (info == null) {
            return "";
        }
        return info.upHttps;
    }

    static class ZoneInfo {
        final String ioHost;
        final String upHost;
        final String upIp;
        final String upBackup;
        final String upHttps;

        private ZoneInfo(String ioHost, String upHost, String upIp, String upBackup, String upHttps) {
            this.ioHost = ioHost;
            this.upHost = upHost;
            this.upIp = upIp;
            this.upBackup = upBackup;
            this.upHttps = upHttps;
        }

        static ZoneInfo buildFromUcRet(UCRet ret) {
            String ioHost = ret.http.get("io").get(0);
            List<String> up = ret.http.get("up");
            String upHost = up.get(0);
            String upBackup = up.get(1);
            String upIp = up.get(2).split(" ")[2].split("//")[1];
            String upHttps = ret.https.get("up").get(0);

            return new ZoneInfo(ioHost, upHost, upIp, upBackup, upHttps);
        }
    }

    private static class ZoneIndex {
        private final String accessKey;
        private final String bucket;

        ZoneIndex(String accessKey, String bucket) {
            this.accessKey = accessKey;
            this.bucket = bucket;
        }

        public int hashCode() {
            return accessKey.hashCode() * 37 + bucket.hashCode();
        }

        public boolean equals(Object obj) {
            return obj == this || !(obj == null || !(obj instanceof ZoneIndex))
                    && ((ZoneIndex) obj).accessKey.equals(accessKey) && ((ZoneIndex) obj).bucket.equals(bucket);
        }
    }

    private class UCRet {
        Map<String, List<String>> http;
        Map<String, List<String>> https;
    }
}
