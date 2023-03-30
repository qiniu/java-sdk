package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AutoRegion extends Region {

    /**
     * uc接口域名
     */
    private String ucServer;

    /**
     * 空间机房，域名信息缓存，此缓存绑定了 token、bucket，且仅 AutoRegion 对象内部有效。
     */
    private Map<String, Region> regions;

    /**
     * 全局空间信息缓存，此缓存绑定了 token、bucket，全局有效。
     */
    private static Map<String, UCRet> globalRegionCache = new ConcurrentHashMap<>();

    /**
     * 定义HTTP请求管理相关方法
     */
    private Client client;

    private AutoRegion() {
    }

    AutoRegion(String ucServer) {
        this.ucServer = ucServer;
        this.client = new Client();
        this.regions = new ConcurrentHashMap<>();
    }

    /**
     * 通过 API 接口查询上传域名
     */
    private UCRet queryRegionInfoFromServerIfNeeded(RegionIndex index) throws QiniuException {
        String cacheKey = index.accessKey + index.bucket;
        UCRet ret = globalRegionCache.get(cacheKey);
        if (ret != null && ret.isValid()) {
            return ret;
        }

        String address = ucServer + "/v3/query?ak=" + index.accessKey + "&bucket=" + index.bucket;
        Response r = client.get(address);
        ret = r.jsonToObject(UCRet.class);
        if (ret != null) {
            ret.setupDeadline();
            globalRegionCache.put(cacheKey, ret);
        }
        return ret;
    }

    static Region regionGroup(UCRet ret) {
        if (ret == null || ret.hosts == null || ret.hosts.length == 0) {
            return null;
        }

        RegionGroup group = new RegionGroup();
        for (ServerRets host : ret.hosts) {
            Region region = host.createRegion();
            group.addRegion(region);
        }

        return group;
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param accessKey 账号 accessKey
     * @param bucket    空间名
     * @return 机房域名信息
     */
    private Region queryRegionInfo(String accessKey, String bucket) throws QiniuException {
        RegionIndex index = new RegionIndex(accessKey, bucket);
        String cacheKey = index.accessKey + "::" + index.bucket;
        Region region = regions.get(cacheKey);

        Exception ex = null;
        if (region == null || !region.isValid()) {
            for (int i = 0; i < 2; i++) {
                try {
                    UCRet ret = queryRegionInfoFromServerIfNeeded(index);
                    region = AutoRegion.regionGroup(ret);
                    if (region != null) {
                        regions.put(cacheKey, region);
                        break;
                    }
                } catch (Exception e) {
                    ex = e;
                }
            }
        }

        // info 不能为 null //
        if (region == null) {
            if (ex instanceof QiniuException) {
                throw (QiniuException) ex;
            }
            throw new QiniuException(ex, "auto region get region info from uc failed.");
        }
        return region;
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param regionReqInfo 封装了 accessKey 和 bucket 的对象
     * @return 机房域名信息
     */
    private Region queryRegionInfo(RegionReqInfo regionReqInfo) throws QiniuException {
        return queryRegionInfo(regionReqInfo.getAccessKey(), regionReqInfo.getBucket());
    }

    @Override
    boolean switchRegion(RegionReqInfo regionReqInfo) {
        Region currentRegion = getCurrentRegion(regionReqInfo);
        if (currentRegion == null) {
            return false;
        } else {
            return currentRegion.switchRegion(regionReqInfo);
        }
    }

    @Override
    String getRegion(RegionReqInfo regionReqInfo) {
        Region currentRegion = getCurrentRegion(regionReqInfo);
        if (currentRegion == null) {
            return "";
        } else {
            return currentRegion.getRegion(regionReqInfo);
        }
    }

    @Override
    boolean isValid() {
        return true;
    }

    @Override
    Region getCurrentRegion(RegionReqInfo regionReqInfo) {
        try {
            Region region = queryRegionInfo(regionReqInfo);
            return region.getCurrentRegion(regionReqInfo);
        } catch (QiniuException e) {
            return null;
        }
    }

    /**
     * 获取源站直传域名
     */
    @Override
    List<String> getSrcUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return null;
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getSrcUpHost(regionReqInfo);
    }

    /**
     * 获取加速上传域名
     */
    @Override
    List<String> getAccUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return null;
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getAccUpHost(regionReqInfo);
    }

    /**
     * 获取源站下载域名
     */
    @Override
    String getIovipHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return "";
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getIovipHost(regionReqInfo);
    }

    @Override
    String getIoSrcHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return "";
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getIoSrcHost(regionReqInfo);
    }

    /**
     * 获取资源管理域名
     */
    @Override
    String getRsHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return "";
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getRsHost(regionReqInfo);
    }

    /**
     * 获取资源列表域名
     */
    @Override
    String getRsfHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return "";
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getRsfHost(regionReqInfo);
    }

    /**
     * 获取资源处理HTTP域名
     */
    @Override
    String getApiHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (regionReqInfo == null) {
            return "";
        }
        Region region = queryRegionInfo(regionReqInfo);
        return region.getApiHost(regionReqInfo);
    }

    @Override
    String getUcHost(RegionReqInfo regionReqInfo) throws QiniuException {
        String host = ucServer.replace("http://", "");
        host = host.replace("https://", "");
        return host;
    }

    public Object clone() {
        AutoRegion newRegion = new AutoRegion();
        newRegion.ucServer = ucServer;
        newRegion.regions = regions;
        newRegion.client = client;
        return newRegion;
    }

    private static class RegionIndex {
        private final String accessKey;
        private final String bucket;

        RegionIndex(String accessKey, String bucket) {
            this.accessKey = accessKey;
            this.bucket = bucket;
        }

        public int hashCode() {
            return accessKey.hashCode() * 37 + bucket.hashCode();
        }

        public boolean equals(Object obj) {
            return obj == this || !(obj == null || !(obj instanceof RegionIndex))
                    && ((RegionIndex) obj).accessKey.equals(accessKey) && ((RegionIndex) obj).bucket.equals(bucket);
        }
    }

    private class UCRet {
        // 有效期, 单位秒
        long deadline;

        ServerRets[] hosts;

        private boolean isValid() {
            return System.currentTimeMillis() < deadline * 1000;
        }

        private void setupDeadline() {
            long ttl = (1L << 31) - 1;
            if (hosts != null && hosts.length > 0) {
                for (ServerRets hostRet : hosts) {
                    if (hostRet != null && hostRet.ttl < ttl) {
                        ttl = hostRet.ttl;
                    }
                }
            }
            deadline = System.currentTimeMillis() / 1000 + ttl;
        }
    }
    // CHECKSTYLE:OFF
    private class ServerRets {
        long ttl;

        String region;
        ServerRet up;
        ServerRet rs;
        ServerRet rsf;
        ServerRet uc;
        ServerRet api;
        ServerRet io;
        @SuppressWarnings({"MemberName"})
        ServerRet io_src;

        Region createRegion() {
            long timestamp = ttl + System.currentTimeMillis() / 1000;
            List<String> srcUpHosts = new ArrayList<>();
            List<String> accUpHosts = new ArrayList<>();
            if (up != null) {
                srcUpHosts = up.allSrcHosts();
                accUpHosts = up.allAccHosts();
            }

            String iovipHost = null;
            if (io != null) {
                iovipHost = io.getOneHost();
            }

            String ioSrcHost = null;
            if (io_src != null) {
                ioSrcHost = io_src.getOneHost();
            }

            String rsHost = null;
            if (rs != null) {
                rsHost = rs.getOneHost();
            }

            String rsfHost = null;
            if (rsf != null) {
                rsfHost = rsf.getOneHost();
            }

            String apiHost = null;
            if (api != null) {
                apiHost = api.getOneHost();
            }

            String ucHost = null;
            if (uc != null) {
                ucHost = uc.getOneHost();
            }

            // 根据 iovipHost 反推 regionId
            String regionId = region;
            if (regionId == null) {
                regionId = "";
            }

            return new Region(timestamp, regionId, srcUpHosts, accUpHosts, iovipHost,
                    ioSrcHost, rsHost, rsfHost, apiHost, ucHost);
        }
    }
    // CHECKSTYLE:ON
    private class ServerRet {
        HostInfoRet src;
        HostInfoRet acc;

        private String getOneHost() {
            String host = null;
            if (src != null) {
                host = src.getOneHost();
            }

            if (host == null && acc != null) {
                host = acc.getOneHost();
            }
            return host;
        }

        private List<String> allSrcHosts() {
            if (src != null) {
                return src.allHosts();
            } else {
                return new ArrayList<>();
            }
        }

        private List<String> allAccHosts() {
            if (acc != null) {
                return acc.allHosts();
            } else {
                return new ArrayList<>();
            }
        }
    }

    private class HostInfoRet {
        List<String> main;
        List<String> backup;

        private String getOneHost() {
            if (main != null && main.size() > 0) {
                return main.get(0);
            } else if (backup != null && backup.size() > 0) {
                return backup.get(0);
            } else {
                return null;
            }
        }

        private List<String> allHosts() {
            List<String> hosts = new ArrayList<>();
            if (main != null && main.size() > 0) {
                hosts.addAll(main);
            }
            if (backup != null && backup.size() > 0) {
                hosts.addAll(backup);
            }
            return hosts;
        }
    }
}
