package com.qiniu.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qiniu.http.Client;
import com.qiniu.http.Response;

public class AutoRegion extends Region {
	
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
     * 获取源站直传域名
     */
    public String getSrcUpHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.srcUpHosts.get(0);
    }
    
    /**
     * 获取加速上传域名
     */
    public String getAccUpHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.accUpHosts.get(0);
    }

    /**
     * 获取源站下载域名
     */
    public String getIovipHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
            return "";
        }
        return info.iovipHost;
    }

    /**
     * 获取资源管理域名
     */
    public String getRsHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
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
    public String getRsfHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
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
    public String getApiHost(RegionReqInfo regionReqInfo) {
        RegionInfo info = queryRegionInfo(regionReqInfo);
        if (info == null) {
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

        protected RegionInfo(List<String> srcUpHosts, List<String> accUpHosts, String iovipHost) {
        	this.srcUpHosts = srcUpHosts;
        	this.accUpHosts = accUpHosts;
        	this.iovipHost = iovipHost;
        }

        /**
	      {
	        "io": {"src": {"main": ["iovip.qbox.me"]}},
	        "up": {
	          "acc": {
	            "main": ["upload.qiniup.com"],
	            "backup": ["upload-jjh.qiniup.com", "upload-xs.qiniup.com"]
	          },
	          "src": {
	            "main": ["up.qiniup.com"],
	            "backup": ["up-jjh.qiniup.com", "up-xs.qiniup.com"]
	          }
	        }
	      }
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
