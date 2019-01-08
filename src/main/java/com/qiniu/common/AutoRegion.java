package com.qiniu.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qiniu.http.Client;
import com.qiniu.http.Response;

public class AutoRegion extends Zone {
    public static AutoRegion instance = new AutoRegion();
    private final String ucServer;

    /**
     * 空间机房，域名信息缓存
     */
    private Map<RegionIndex, RegionInfo> regions;
    /**
     * 根据API返回的上传域名推导出其他资源管理域名
     */
    private Map<String, Region> inferDomainsMap;
    private Client client;


    /**
     * 构建默认的域名接口获取对象
     */
    public AutoRegion() {
        this("https://uc.qbox.me");
    }

    public AutoRegion(String ucServer) {
        this.ucServer = ucServer;
        this.client = new Client();
        this.regions = new ConcurrentHashMap<>();
        this.inferDomainsMap = new ConcurrentHashMap<>();
        this.inferDomainsMap.put("http://up.qiniu.com", region0());
        this.inferDomainsMap.put("http://up-z1.qiniu.com", region1());
        this.inferDomainsMap.put("http://up-z2.qiniu.com", region2());
        this.inferDomainsMap.put("http://up-na0.qiniu.com", regionNa0());
        this.inferDomainsMap.put("http://up-as0.qiniu.com", regionAs0());
    }

    /**
     * 通过 API 接口查询上传域名
     */
    private UCRet getRegionJson(RegionIndex index) throws QiniuException {
        String address = ucServer + "/v1/query?ak=" + index.accessKey + "&bucket=" + index.bucket;

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
    public RegionInfo queryRegionInfo(String accessKey, String bucket) throws QiniuException {
        RegionIndex index = new RegionIndex(accessKey, bucket);
        RegionInfo info = regions.get(index);
        if (info == null) {
            UCRet ret = getRegionJson(index);
            try {
                info = RegionInfo.buildFromUcRet(ret);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (info != null) {
                regions.put(index, info);
            }
        }
        return info;
    }

    /**
     * 首先从缓存读取Region信息，如果没有则发送请求从接口查询
     *
     * @param regionReqInfo 封装了 accessKey 和 bucket 的对象
     * @return 机房域名信息
     */
    public RegionInfo queryRegionInfo(RegionReqInfo regionReqInfo) {
        try {
            return queryRegionInfo(regionReqInfo.getAccessKey(), regionReqInfo.getBucket());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取上传HTTP域名
     */
    @Override
    public String getUpHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upHttp;
    }

    /**
     * 获取上传备用HTTP域名（Cdn加速域名）
     */
    @Override
    public String getUpBackupHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upBackupHttp;
    }

    /**
     * 获取上传入口IP
     */
    @Override
    public String getUpIpHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upIpHttp;
    }

    /**
     * 获取资源高级管理HTTP域名
     */
    @Override
    public String getIovipHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.iovipHttp;
    }

    /**
     * 获取上传HTTPS域名
     */
    @Override
    public String getUpHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upHttps;
    }

    /**
     * 获取上传备用HTTPS域名（Cdn加速域名）
     */
    @Override
    public String getUpBackupHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upBackupHttps;
    }

    /**
     * 获取上传入口IP
     */
    @Override
    public String getUpIpHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.upIpHttps;
    }

    /**
     * 获取资源高级管理HTTPS域名
     */
    @Override
    public String getIovipHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.iovipHttps;
    }

    /**
     * 获取资源管理HTTP域名
     */
    @Override
    public String getRsHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getRsHttp();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getRsHttp();
        } else {
            return super.getRsHttp();
        }
    }

    /**
     * 获取资源管理HTTPS域名
     */
    @Override
    public String getRsHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getRsHttps();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getRsHttps();
        } else {
            return super.getRsHttps();
        }
    }

    /**
     * 获取资源列表HTTP域名
     */
    @Override
    public String getRsfHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getRsfHttp();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getRsfHttp();
        } else {
            return super.getRsfHttp();
        }
    }


    /**
     * 获取资源列表HTTP域名
     */
    @Override
    public String getRsfHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getRsfHttps();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getRsfHttps();
        } else {
            return super.getRsfHttps();
        }
    }

    @Override
    public String getApiHttp(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getApiHttp();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getApiHttp();
        } else {
            return super.getApiHttp();
        }
    }


    /**
     * 获取资源处理HTTP域名
     */
    @Override
    public String getApiHttps(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return super.getApiHttps();
        }
        Region region = this.inferDomainsMap.get(info.upHttp);
        if (region != null) {
            return region.getApiHttps();
        } else {
            return super.getApiHttps();
        }
    }

    /**
     * 从接口获取的域名信息
     */
    static class RegionInfo {
        final String upHttp;
        final String upBackupHttp;
        final String upIpHttp;
        final String iovipHttp;

        final String upHttps;
        final String upBackupHttps;
        final String upIpHttps;
        final String iovipHttps;

        protected RegionInfo(String upHttp, String upBackupHttp, String upIpHttp, String iovipHttp,
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
        static RegionInfo buildFromUcRet(UCRet ret) {
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

            return new RegionInfo(upHttp, upBackupHttp, upIpHttp, ioHttp, upHttps, upBackupHttps, upIpHttps, ioHttps);
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
        Map<String, List<String>> http;
        Map<String, List<String>> https;
    }
}
