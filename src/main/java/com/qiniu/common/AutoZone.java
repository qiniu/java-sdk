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
    public static AutoZone instance = new AutoZone();
    private final String ucServer;

    /**
     * 空间机房，域名信息缓存
     */
    private Map<ZoneIndex, ZoneInfo> zones;
    private Map<String, Zone> inferDomainsMap;
    private Client client;


    /**
     * 构建默认的域名接口获取对象
     */
    public AutoZone() {
        this("https://uc.qbox.me");
    }

    public AutoZone(String ucServer) {
        this.ucServer = ucServer;
        this.client = new Client();
        this.zones = new ConcurrentHashMap<>();
        this.inferDomainsMap = new ConcurrentHashMap<>();
        this.inferDomainsMap.put("http://up.qiniu.com", zone0());
        this.inferDomainsMap.put("http://up-z1.qiniu.com", zone1());
        this.inferDomainsMap.put("http://up-z2.qiniu.com", zone2());
        this.inferDomainsMap.put("http://up-na0.qiniu.com", zoneNa0());
    }

    /**
     * 通过接口查询域名
     */
    private UCRet getZoneJson(ZoneIndex index) throws QiniuException {
        String address = ucServer + "/v1/query?ak=" + index.accessKey + "&bucket=" + index.bucket;

        Response r = client.get(address);
        return r.jsonToObject(UCRet.class);
    }

    /**
     * 首先从缓存读取Zone信息，如果没有则发送请求从接口查询
     */
    public ZoneInfo queryZoneInfo(String ak, String bucket) throws QiniuException {
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

    /**
     * Zone信息查询接口
     */
    public ZoneInfo queryZoneInfo(ZoneReqInfo zoneReqInfo) {
        try {
            return queryZoneInfo(zoneReqInfo.getAccessKey(), zoneReqInfo.getBucket());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUpHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upHttp;
    }

    @Override
    public String getUpBackupHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upBackupHttp;
    }

    @Override
    public String getUpIpHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upIpHttp;
    }

    @Override
    public String getIovipHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.iovipHttp;
    }


    @Override
    public String getUpHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upHttps;
    }

    @Override
    public String getUpBackupHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upBackupHttps;
    }

    @Override
    public String getUpIpHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.upIpHttps;
    }

    @Override
    public String getIovipHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return "";
        }
        return info.iovipHttps;
    }

    @Override
    public String getRsHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getRsHttp();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getRsHttp();
        } else {
            return super.getRsHttp();
        }
    }

    @Override
    public String getRsHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getRsHttps();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getRsHttps();
        } else {
            return super.getRsHttps();
        }
    }

    @Override
    public String getRsfHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getRsfHttp();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getRsfHttp();
        } else {
            return super.getRsfHttp();
        }
    }

    @Override
    public String getRsfHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getRsfHttps();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getRsfHttps();
        } else {
            return super.getRsfHttps();
        }
    }

    @Override
    public String getApiHttp(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getApiHttp();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getApiHttp();
        } else {
            return super.getApiHttp();
        }
    }

    @Override
    public String getApiHttps(ZoneReqInfo zoneReqInfo) {
        ZoneInfo info = queryZoneInfo(zoneReqInfo);
        if (info == null) {
            return super.getApiHttps();
        }
        Zone zone = this.inferDomainsMap.get(info.upHttp);
        if (zone != null) {
            return zone.getApiHttps();
        } else {
            return super.getApiHttps();
        }
    }

    /**
     * 从接口获取的域名信息
     */
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
