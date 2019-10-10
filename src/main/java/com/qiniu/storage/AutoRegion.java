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
    private Map<RegionIndex, RegionInfo> regions;

    /**
     * 根据API返回的上传域名推导出其他资源管理域名
     */
    private Map<String, Region> inferDomainsMap;

    /**
     * 定义HTTP请求管理相关方法
     */
    private Client client;

    AutoRegion(String ucServer) {
        this.ucServer = ucServer;
        this.client = new Client();
        this.regions = new ConcurrentHashMap<>();
        this.inferDomainsMap = new ConcurrentHashMap<>();
        this.inferDomainsMap.put("up.qiniup.com", region0());
        this.inferDomainsMap.put("up-jjh.qiniup.com", region0());
        this.inferDomainsMap.put("up-xs.qiniup.com", region0());
        this.inferDomainsMap.put("up-z1.qiniup.com", region1());
        this.inferDomainsMap.put("up-z2.qiniup.com", region2());
        this.inferDomainsMap.put("up-dg.qiniup.com", region2());
        this.inferDomainsMap.put("up-fs.qiniup.com", region2());
        this.inferDomainsMap.put("up-na0.qiniup.com", regionNa0());
        this.inferDomainsMap.put("up-as0.qiniup.com", regionAs0());
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
        String address = ucServer + "/v2/query?ak=" + index.accessKey + "&bucket=" + index.bucket;

        Response r = client.get(address);
        return r.jsonToObject(UCRet.class);
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param accessKey 账号 accessKey
     * @param bucket    空间名
     * @return 机房域名信息
     */
    private RegionInfo queryRegionInfo(String accessKey, String bucket) throws QiniuException {
        RegionIndex index = new RegionIndex(accessKey, bucket);
        RegionInfo info = regions.get(index);

        Exception ex = null;
        // 隔一段时间重新获取 uc 信息 //
        if (info == null || info.createTime < System.currentTimeMillis() - 1000 * 3600 * 8) {
            try {
                // 1
                UCRet ret = getRegionJson(index);
                RegionInfo info2 = RegionInfo.buildFromUcRet(ret);
                // 初次获取报错，info == null ，响应 null //
                // 后续重新获取，正常获取则替换以前的 //
                if (info2 != null) {
                    regions.put(index, info2);
                    info = info2;
                }
            } catch (Exception e) {
                ex = e;
                if (info == null) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e1) {
                        // do nothing
                    }
                    try {
                        // 2
                        UCRet ret = getRegionJson(index);
                        RegionInfo info2 = RegionInfo.buildFromUcRet(ret);
                        // 初次获取报错，info == null ，响应 null //
                        // 后续重新获取，正常获取则替换以前的 //
                        if (info2 != null) {
                            regions.put(index, info2);
                            info = info2;
                        }
                    } catch (Exception e1) {
                        ex = e1;
                    }
                }
            }
        }
        // info 不能为 null //
        if (info == null) {
            if (ex instanceof QiniuException) {
                throw (QiniuException) ex;
            }
            throw new QiniuException(ex, "auto region get region info from uc failed.");
        }
        return info;
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param regionReqInfo 封装了 accessKey 和 bucket 的对象
     * @return 机房域名信息
     */
    private RegionInfo queryRegionInfo(RegionReqInfo regionReqInfo) throws QiniuException {
        return queryRegionInfo(regionReqInfo.getAccessKey(), regionReqInfo.getBucket());
    }

    @Override
    String getRegion(RegionReqInfo regionReqInfo) {
        return "";
    }

    /**
     * 获取源站直传域名
     */
    @Override
    List<String> getSrcUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        return info.srcUpHosts;
    }

    /**
     * 获取加速上传域名
     */
    @Override
    List<String> getAccUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        return info.accUpHosts;
    }

    /**
     * 获取源站下载域名
     */
    @Override
    String getIovipHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        return info.iovipHost;
    }

    /**
     * 获取资源管理域名
     */
    @Override
    String getRsHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info;
        try {
            info = queryRegionInfo(regionReqInfo);
        } catch (QiniuException e) {
            return super.getRsHost(regionReqInfo);
        }
        Region region = this.inferDomainsMap.get(info.srcUpHosts.get(0));
        if (region != null) {
            return region.getRsHost(regionReqInfo);
        } else {
            return super.getRsHost(regionReqInfo);
        }
    }

    /**
     * 获取资源列表域名
     */
    @Override
    String getRsfHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info;
        try {
            info = queryRegionInfo(regionReqInfo);
        } catch (QiniuException e) {
            return super.getRsfHost(regionReqInfo);
        }
        Region region = this.inferDomainsMap.get(info.srcUpHosts.get(0));
        if (region != null) {
            return region.getRsfHost(regionReqInfo);
        } else {
            return super.getRsfHost(regionReqInfo);
        }
    }

    /**
     * 获取资源处理HTTP域名
     */
    @Override
    String getApiHost(RegionReqInfo regionReqInfo) throws QiniuException {
        RegionInfo info;
        try {
            info = queryRegionInfo(regionReqInfo);
        } catch (QiniuException e) {
            return super.getApiHost(regionReqInfo);
        }
        Region region = this.inferDomainsMap.get(info.srcUpHosts.get(0));
        if (region != null) {
            return region.getApiHost(regionReqInfo);
        } else {
            return super.getApiHost(regionReqInfo);
        }
    }

    /**
     * 从接口获取的域名信息
     */
    static class RegionInfo {
        final List<String> srcUpHosts;
        final List<String> accUpHosts;
        final String iovipHost;
        final long createTime;

        protected RegionInfo(List<String> srcUpHosts, List<String> accUpHosts, String iovipHost) {
            this.srcUpHosts = srcUpHosts;
            this.accUpHosts = accUpHosts;
            this.iovipHost = iovipHost;
            createTime = System.currentTimeMillis();
        }

        /**
         * {
         *   "io": {"src": {"main": ["iovip.qbox.me"]}},
         *   "up": {
         *     "acc": {
         *       "main": ["upload.qiniup.com"],
         *       "backup": ["upload-jjh.qiniup.com", "upload-xs.qiniup.com"]
         *     },
         *     "src": {
         *       "main": ["up.qiniup.com"],
         *       "backup": ["up-jjh.qiniup.com", "up-xs.qiniup.com"]
         *     }
         *   }
         * }
         *
         * @param ret
         * @return
         */
        static RegionInfo buildFromUcRet(UCRet ret) {
            List<String> srcUpHosts = new ArrayList<>();
            addAll(srcUpHosts, ret.up.src.get("main"));
            addAll(srcUpHosts, ret.up.src.get("backup"));
            List<String> accUpHosts = new ArrayList<>();
            addAll(accUpHosts, ret.up.acc.get("main"));
            addAll(accUpHosts, ret.up.acc.get("backup"));
            String iovipHost = ret.io.src.get("main").get(0);
            return new RegionInfo(srcUpHosts, accUpHosts, iovipHost);
        }

        static void addAll(List<String> s, List<String> p) {
            if (p != null) {
                s.addAll(p);
            }
        }
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
        UPRet up;
        IORet io;
    }

    private class UPRet {
        Map<String, List<String>> acc;
        Map<String, List<String>> src;
    }

    private class IORet {
        Map<String, List<String>> src;
    }

}
