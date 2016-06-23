package com.qiniu.util;

import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Simon on 6/22/16.
 */
public class UC {
    private Map<AKBKT, ZoneInfo> zones = new MConcurrentHashMap();

    private boolean isSyncLocked = false;
    private Lock lock = new ReentrantLock();

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.SECONDS)
            .readTimeout(10000, TimeUnit.SECONDS)
            .writeTimeout(10000, TimeUnit.SECONDS).build();

    private UC() {

    }

    private static UC uc = new UC();

    public static Zone zone(String ak, String bkt) throws QiniuException {
        return uc.getZone(Config.zone, ak, bkt, Config.UPLOAD_BY_HTTPS);
    }

    public static Zone zone(String uptoken) throws QiniuException {
        try {
            // http://developer.qiniu.com/article/developer/security/upload-token.html
            // http://developer.qiniu.com/article/developer/security/put-policy.html
            String[] strings = uptoken.split(":");
            String ak = strings[0];
            String policy = new String(UrlSafeBase64.decode(strings[2]), Config.UTF_8);
            String bkt = Json.decode(policy).get("scope").toString().split(":")[0];
            return zone(ak, bkt);
        } catch (QiniuException e) {
            throw e;
        } catch (Exception e) {
            throw new QiniuException(e);
        }
    }


    public static String ucVal(String ak, String bkt) throws QiniuException {
        // 不解析返回的值,此时 isHttps (Config.UPLOAD_BY_HTTPS) 无实际意义
        return uc.getUCVal(ak, bkt, Config.UPLOAD_BY_HTTPS);
    }

    public static void clear() {
        uc.zones.clear();
    }

    /**
     * 返回 java-sdk 中需要的 zone 对象
     */
    private Zone getZone(final Zone userSetZone, final String ak, final String bkt, boolean isHttps)
            throws QiniuException {
        if (userSetZone != null) {
            return userSetZone;
        }
        ZoneInfo zoneInfo = getZoneInfo(ak, bkt, isHttps);
        return zoneInfo.zone;
    }

    /**
     * 返回 uc.qbox.me 的原始字符串
     */
    public String getUCVal(final String ak, final String bkt, boolean isHttps) throws QiniuException {
        ZoneInfo zoneInfo = getZoneInfo(ak, bkt, isHttps);
        return zoneInfo.ucjson;
    }


    ZoneInfo getZoneInfo(final String ak, final String bkt, boolean isHttps) throws QiniuException {
        final AKBKT akbkt = new AKBKT(ak, bkt, isHttps);
        ZoneInfo zoneInfo = zones.get(akbkt);
        Zone tmp_zone = zoneInfo == null ? null : zoneInfo.zone;

        if (tmp_zone != null && zoneInfo.deadline > System.currentTimeMillis() / 1000) {
            return zoneInfo;
        }

        if (tmp_zone != null) {
            if (!isSyncLocked) {
                try {
                    lock.lock();
                    if (!isSyncLocked) {
                        isSyncLocked = true;
                        new Thread() {
                            public void run() {
                                try {
                                    build(akbkt);
                                } catch (QiniuException e) {
                                    e.printStackTrace();
                                } finally {
                                    isSyncLocked = false;
                                }
                            }
                        }.start();
                    }
                } finally {
                    lock.unlock();
                }
            }
            return zoneInfo;
        } else {
            try {
                lock.lock();
                zoneInfo = zones.get(akbkt);
                tmp_zone = zoneInfo == null ? null : zoneInfo.zone;
                if (tmp_zone == null) {
                    build(akbkt);
                    zoneInfo = zones.get(akbkt);
                    return zoneInfo;
                } else {
                    return zoneInfo;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void build(AKBKT akbkt) throws QiniuException {
        try {
            String address = Config.UC_HOST + "/v1/query?ak=" + akbkt.ak + "&bucket=" + akbkt.bkt;
            long start = System.currentTimeMillis();
            Response res = client.newCall(new Request.Builder()
                    .url(address)
                    .build()).execute();
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            build(akbkt, res, address, duration);
        } catch (QiniuException e) {
            throw e;
        } catch (Exception e) {
            throw new QiniuException(e);
        }
    }

    private void build(AKBKT akbkt, Response res, String address, double duration) throws QiniuException {
        com.qiniu.http.Response qnRes = com.qiniu.http.Response.create(res, address, duration);
        if (!qnRes.isOK()) {
            throw new QiniuException(qnRes);
        }
        try {
            String ucVal = qnRes.bodyString();
            UCRet ret = qnRes.jsonToObject(UCRet.class);
            long deadline = System.currentTimeMillis() / 1000 + ret.ttl;

            List<String> args = null;
            if (akbkt.isHttps) {
                args = ret.https.get("up");
            } else {
                args = ret.http.get("up");
            }

            String[] zoneArgs = new String[2];
            zoneArgs[0] = getZoneHost(args.get(0));
            if (args.size() > 1) {
                zoneArgs[1] = getZoneHost(args.get(1));
            }
            if (zoneArgs[1] == null) {
                zoneArgs[1] = zoneArgs[0];
            }

            Zone new_zone = new Zone(zoneArgs[0], zoneArgs[1]);

            zones.put(akbkt, new ZoneInfo(deadline, new_zone, ucVal));
        } catch (Exception e) {
            throw new QiniuException(e);
        }
    }


    private String getZoneHost(String p) {
        if (p.startsWith("http")) {
            return p;
        } else {
            return null;
        }
    }

    private class AKBKT {
        String ak;
        String bkt;
        boolean isHttps;

        AKBKT(String ak, String bkt, boolean isHttps) {
            this.ak = ak.trim();
            this.bkt = bkt.trim();
            this.isHttps = isHttps;
        }

        @Override
        public int hashCode() {
            return ak.hashCode() * bkt.hashCode() * (isHttps ? 5 : 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AKBKT) {
                AKBKT that = (AKBKT) obj;
                return this.bkt.equals(that.bkt) && this.ak.equals(that.ak) && (this.isHttps == that.isHttps);
            }
            return false;
        }
    }

    private class ZoneInfo {
        public final long deadline;
        public final String ucjson;
        public final Zone zone;

        ZoneInfo(long deadline, Zone zone, String ucjson) {
            this.deadline = deadline;
            this.zone = zone;
            this.ucjson = ucjson;
        }
    }

    private class UCRet {
        int ttl = 86400;
        boolean global = false;
        Map<String, List<String>> http;
        Map<String, List<String>> https;
    }


    private class MConcurrentHashMap extends ConcurrentHashMap<AKBKT, ZoneInfo> {
        @Override
        public ZoneInfo put(AKBKT key, ZoneInfo value) {
            ZoneInfo e = super.put(key, value);
            if (this.size() % 5 == 0) {
                removeOverTimeV();
            }
            return e;
        }

        private void removeOverTimeV() {
            long now = System.currentTimeMillis() / 1000;
            for (Map.Entry<AKBKT, ZoneInfo> entry : this.entrySet()) {
                if (entry.getValue().deadline < now) {
                    this.remove(entry);
                }
            }
        }
    }
}
