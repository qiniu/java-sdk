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
    private final String ucServer;

    /**
     * 空间机房，域名信息缓存
     */
    private Map<RegionIndex, RegionGroup> regions;

    /**
     * 定义HTTP请求管理相关方法
     */
    private Client client;


    AutoRegion(String ucServer) {
        this.ucServer = ucServer;
        this.client = new Client();
        this.regions = new ConcurrentHashMap<>();
    }

    /**
     * 通过 API 接口查询上传域名
     * z0: http://uc.qbox.me/v2/query?ak=vHg2e7nOh7Jsucv2Azr5FH6omPgX22zoJRWa0FN5&bucket=sdk-z0
     * z1: http://uc.qbox.me/v2/query?ak=vHg2e7nOh7Jsucv2Azr5FH6omPgX22zoJRWa0FN5&bucket=sdk-z1
     * z2: http://uc.qbox.me/v2/query?ak=vHg2e7nOh7Jsucv2Azr5FH6omPgX22zoJRWa0FN5&bucket=sdk-z2
     * as0: http://uc.qbox.me/v2/query?ak=vHg2e7nOh7Jsucv2Azr5FH6omPgX22zoJRWa0FN5&bucket=sdk-as0
     * na0: http://uc.qbox.me/v2/query?ak=vHg2e7nOh7Jsucv2Azr5FH6omPgX22zoJRWa0FN5&bucket=sdk-na0
     */
    private UCRet getRegionJson(RegionIndex index) throws QiniuException {
        String address = ucServer + "/v3/query?ak=" + index.accessKey + "&bucket=" + index.bucket;

        Response r = client.get(address);
        return r.jsonToObject(UCRet.class);
    }

    static RegionGroup regionGroup(UCRet ret) {
        if (ret == null || ret.hosts == null || ret.hosts.length == 0) {
            return null;
        }

        RegionGroup group = new RegionGroup();
        for (HostRet host : ret.hosts) {
            long timestamp = host.ttl + System.currentTimeMillis() / 1000;
            List<String> srcUpHosts = new ArrayList<>();
            List<String> accUpHosts = new ArrayList<>();
            if (host.up != null) {
                srcUpHosts = host.up.allSrcHosts();
                accUpHosts = host.up.allAccHosts();
            }

            String iovipHost = null;
            if (host.io != null) {
                iovipHost = host.io.getOneHost();
            }

            String rsHost = null;
            if (host.rs != null) {
                rsHost = host.rs.getOneHost();
            }

            String rsfHost = null;
            if (host.rsf != null) {
                rsfHost = host.rsf.getOneHost();
            }

            String apiHost = null;
            if (host.api != null) {
                apiHost = host.api.getOneHost();
            }

            String ucHost = null;
            if (host.uc != null) {
                ucHost = host.uc.getOneHost();
            }

            // 根据 iovipHost 反推 regionId
            String regionId = host.region;
            if (regionId == null) {
                regionId = "";
            }

            Region region = new Region(timestamp, regionId, srcUpHosts, accUpHosts, iovipHost, rsHost, rsfHost, apiHost, ucHost);
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
    private RegionGroup queryRegionInfo(String accessKey, String bucket) throws QiniuException {
        RegionIndex index = new RegionIndex(accessKey, bucket);
        RegionGroup regionGroup = regions.get(index);

        Exception ex = null;
        // 隔一段时间重新获取 uc 信息 // || regionGroup.createTime < System.currentTimeMillis() - 1000 * 3600 * 8
        if (regionGroup == null || !regionGroup.isValid()) {
            for (int i = 0; i < 2; i++) {
                try {
                    UCRet ret = getRegionJson(index);
                    regionGroup = AutoRegion.regionGroup(ret);
                    if (regionGroup != null) {
                        regions.put(index, regionGroup);
                        break;
                    }
                } catch (Exception e) {
                    ex = e;
                }
            }
        }

        // info 不能为 null //
        if (regionGroup == null) {
            if (ex instanceof QiniuException) {
                throw (QiniuException) ex;
            }
            throw new QiniuException(ex, "auto region get region info from uc failed.");
        }
        return regionGroup;
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param regionReqInfo 封装了 accessKey 和 bucket 的对象
     * @return 机房域名信息
     */
    private RegionGroup queryRegionInfo(RegionReqInfo regionReqInfo) throws QiniuException {
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

    Region getCurrentRegion(RegionReqInfo regionReqInfo) {
        try {
            RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
            return regionGroup.getCurrentRegion(regionReqInfo);
        } catch (QiniuException e) {
            return null;
        }
    }

    /**
     * 获取源站直传域名
     */
    @Override
    List<String> getSrcUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getSrcUpHost(regionReqInfo);
    }

    /**
     * 获取加速上传域名
     */
    @Override
    List<String> getAccUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getAccUpHost(regionReqInfo);
    }

    /**
     * 获取源站下载域名
     */
    @Override
    String getIovipHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getIovipHost(regionReqInfo);
    }

    /**
     * 获取资源管理域名
     */
    @Override
    String getRsHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getRsHost(regionReqInfo);
    }

    /**
     * 获取资源列表域名
     */
    @Override
    String getRsfHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getRsfHost(regionReqInfo);
    }

    /**
     * 获取资源处理HTTP域名
     */
    @Override
    String getApiHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionGroup regionGroup = queryRegionInfo(regionReqInfo);
        return regionGroup.getApiHost(regionReqInfo);
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
        HostRet[] hosts;
    }

    private class HostRet {
        long ttl;
        String region;
        HostInfoRet up;
        HostInfoRet rs;
        HostInfoRet rsf;
        HostInfoRet uc;
        HostInfoRet api;
        HostInfoRet io;
    }

    private class HostInfoRet {
        Map<String, List<String>> acc;
        Map<String, List<String>> src;

        private String getOneHost() {
            List<String> hosts = allHosts();
            if (hosts.size() > 0) {
                return hosts.get(0);
            } else {
                return null;
            }
        }

        private List<String> allHosts() {
            List<String> hosts = new ArrayList<>();
            List<String> srcHosts = allSrcHosts();
            if (srcHosts.size() > 0) {
                hosts.addAll(srcHosts);
            }

            List<String> accHosts = allAccHosts();
            if (accHosts.size() > 0) {
                hosts.addAll(accHosts);
            }
            return hosts;
        }

        private List<String> allSrcHosts() {
            List<String> hosts = new ArrayList<>();
            if (acc != null) {
                List<String> mainHosts = acc.get("main");
                if (mainHosts != null && mainHosts.size() > 0) {
                    hosts.addAll(mainHosts);
                }

                List<String> backupHosts = acc.get("backup");
                if (backupHosts != null && backupHosts.size() > 0) {
                    hosts.addAll(backupHosts);
                }
            }
            return hosts;
        }

        private List<String> allAccHosts() {
            List<String> hosts = new ArrayList<>();
            if (src != null) {
                List<String> mainHosts = src.get("main");
                if (mainHosts != null && mainHosts.size() > 0) {
                    hosts.addAll(mainHosts);
                }

                List<String> backupHosts = src.get("backup");
                if (backupHosts != null && backupHosts.size() > 0) {
                    hosts.addAll(backupHosts);
                }
            }
            return hosts;
        }
    }
}
