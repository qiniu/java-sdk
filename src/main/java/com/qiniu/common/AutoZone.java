package com.qiniu.common;

import com.qiniu.http.Client;
import com.qiniu.http.Response;

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
    ZoneInfo queryByToken(ZoneReqInfo ab) {
        try {
            return zoneInfo(ab.ak, ab.bucket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUpHttp(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upHttp;
    }

    @Override
    public String getUpBackupHttp(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upBackupHttp;
    }

    @Override
    public String getUpIpHttp(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upIpHttp;
    }

    @Override
    public String getIovipHttp(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.iovipHttp;
    }


    @Override
    public String getUpHttps(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upHttps;
    }

    @Override
    public String getUpBackupHttps(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upBackupHttps;
    }

    @Override
    public String getUpIpHttps(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.upIpHttps;
    }

    @Override
    public String getIovipHttps(ZoneReqInfo ab) {
        ZoneInfo info = queryByToken(ab);
        if (info == null) {
            return "";
        }
        return info.iovipHttps;
    }



    static class ZoneInfo {
        final String upHttp;
        final String upBackupHttp;
        final String upIpHttp;
        final String iovipHttp;

        final String upHttps;
        final String upBackupHttps;
        final String upIpHttps;
        final String iovipHttps;

        private ZoneInfo(String upHttp, String upBackupHttp, String upIpHttp, String iovipHttp,
                         String upHttps, String upBackupHttps, String upIpHttps, String iovipHttps) {
            this.upHttp = upHttp;
            this.upBackupHttp = upBackupHttp;
            this.upIpHttp = upIpHttp;
            this.iovipHttp = iovipHttp;
            this.upHttps = upHttps;
            this.upBackupHttps = upBackupHttps;
            this.upIpHttps = upIpHttps;
            this.iovipHttps = iovipHttps;
        }

        /*
         * {"ttl":86400,
         *  "http":
         *    {
         *      "io":["http://iovip.qbox.me"],
         *      "up":["http://up.qiniu.com","http://upload.qiniu.com",
         *        "-H up.qiniu.com http://183.136.139.16"]
         *    },
         *  "https":{"io":["https://iovip.qbox.me"],"up":["https://up.qbox.me"]}}
         * */
        static ZoneInfo buildFromUcRet(UCRet ret) {
            List<String> upsHttp = ret.http.get("up");
            String upHttp = upsHttp.get(0);
            String upBackupHttp = upsHttp.get(1);
            String upIpHttp = upsHttp.get(2).split(" ")[2].split("//")[1];
            String ioHttp = ret.http.get("io").get(0);

            List<String> upsHttps = ret.https.get("up");
            String upHttps = upsHttps.get(0);
            String upBackupHttps = upHttps;
            String upIpHttps = "";
            if (upsHttps.size() > 1) {
                upBackupHttps = upsHttps.get(1);
            }
            if (upsHttps.size() > 2) {
                upIpHttps = upsHttps.get(2).split(" ")[2].split("//")[1];
            }
            String ioHttps = ret.https.get("io").get(0);

            return new ZoneInfo(upHttp, upBackupHttp, upIpHttp, ioHttp, upHttps, upBackupHttps, upIpHttps, ioHttps);
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
