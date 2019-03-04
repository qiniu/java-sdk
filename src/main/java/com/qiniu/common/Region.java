package com.qiniu.common;

import java.util.Arrays;
import java.util.List;

public class Region {
	
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    private String region;
    
    /*
     * 源站直传，加速上传，源站下载 使用各个机房对应的域名
     */
    private List<String> srcUpHosts;
    private List<String> accUpHosts;
    private String iovipHost;
    
    /*
     * 资源管理，资源列表，资源处理类域名
     * 默认的这组域名是国内国外共用域名，无论海外国内都可以访问
     * 只有在无法自动查询到机房对应具体域名情况下，使用这组域名
     */
    private String rsHost = "rs.qbox.me";
    private String rsfHost = "rsf.qbox.me";
    private String apiHost = "api.qiniu.com";
    
    public Region() {
    	super();
    }
    
    public Region(String region, List<String> srcUpHosts, List<String> accUpHosts, String iovipHost) {
		super();
		this.region = region;
		this.srcUpHosts = srcUpHosts;
		this.accUpHosts = accUpHosts;
		this.iovipHost = iovipHost;
	}

	/*
     * 获取成员变量
     */
    public String getRegion() {
        return this.region;
    }

	public List<String> getSrcUpHosts() {
		return srcUpHosts;
	}

	public List<String> getAccUpHosts() {
		return accUpHosts;
	}

	public String getIovipHost() {
		return iovipHost;
	}

	public String getRsHost() {
		return rsHost;
	}

	public String getRsfHost() {
		return rsfHost;
	}

	public String getApiHost() {
		return apiHost;
	}

    /**
     * 域名构造器
     */
    static class Builder {
        private Region region;

        public Builder() {
        	region = new Region();
        }

        public Builder(Region originRegion) {
            this();
            region.region = originRegion.region;
            region.srcUpHosts = originRegion.srcUpHosts;
            region.accUpHosts = originRegion.accUpHosts;
            region.iovipHost = originRegion.iovipHost;
            region.rsHost = originRegion.rsHost;
            region.rsfHost = originRegion.rsfHost;
            region.apiHost = originRegion.apiHost;
        }

        public Builder region(String region) {
        	this.region.region = region;
            return this;
        }

        public Builder srcUpHost(String... srcUpHosts) {
        	this.region.srcUpHosts = Arrays.asList(srcUpHosts);
        	return this;
        }
        
        public Builder accUpHost(String... accUpHosts) {
        	this.region.accUpHosts = Arrays.asList(accUpHosts);
        	return this;
        }
        
        public Builder iovipHost(String iovipHost) {
        	this.region.iovipHost = iovipHost;
        	return this;
        }
        
        public Builder rsHost(String rsHost) {
        	this.region.rsHost = rsHost;
        	return this;
        }
        
        public Builder rsfHost(String rsfHost) {
        	this.region.rsfHost = rsfHost;
        	return this;
        }

        public Builder apiHost(String apiHost) {
        	this.region.apiHost = apiHost;
        	return this;
        }

        /**
         * 自动选择,其它参数设置无效
         */
        public Region autoRegion() {
        	return new AutoRegion();
        }

        /**
         * 返回构建好的Region对象
         */
        public Region build() {
            return region;
        }
    }

    /**
     * 华东机房相关域名
     */
    public static Region region0() {
    	return new Builder().
    			region("z0").
    			srcUpHost("up.qiniup.com", "up-jjh.qiniup.com", "up-xs.qiniup.com").
    			accUpHost("upload.qiniup.com", "upload-jjh.qiniup.com", "upload-xs.qiniup.com").
    			iovipHost("iovip.qbox.me").
    			rsHost("rs.qbox.me").
    			rsfHost("rsf.qbox.me").
    			apiHost("api.qiniu.com").
    			build();
    }

    /**
     * 华东机房相关域名
     */
    public static Region huadong() {
        return region0();
    }


    /**
     * 华东机房内网上传相关域名
     */
    public static Region qvmRegion0() {
    	return new Builder().
    			region("z0").
    			srcUpHost("free-qvm-z0-xs.qiniup.com").
    			accUpHost("free-qvm-z0-xs.qiniup.com").
    			iovipHost("iovip.qbox.me").
    			rsHost("rs.qbox.me").
    			rsfHost("rsf.qbox.me").
    			apiHost("api.qiniu.com").
    			build();
    }

    /**
     * 华东机房内网上传相关域名
     */
    public static Region qvmHuadong() {
        return qvmRegion0();
    }


    /**
     * 华北机房相关域名
     */
    public static Region region1() {
    	return new Builder().
    			region("z1").
    			srcUpHost("up-z1.qiniup.com").
    			accUpHost("upload-z1.qiniup.com").
    			iovipHost("iovip-z1.qbox.me").
    			rsHost("rs-z1.qbox.me").
    			rsfHost("rsf-z1.qbox.me").
    			apiHost("api-z1.qiniu.com").
    			build();
    }

    /**
     * 华北机房相关域名
     */
    public static Region huabei() {
        return region1();
    }


    /**
     * 华北机房内网上传相关域名
     */
    public static Region qvmRegion1() {
    	return new Builder().
    			region("z1").
    			srcUpHost("free-qvm-z1-zz.qiniup.com").
    			accUpHost("free-qvm-z1-zz.qiniup.com").
    			iovipHost("iovip-z1.qbox.me").
    			rsHost("rs-z1.qbox.me").
    			rsfHost("rsf-z1.qbox.me").
    			apiHost("api-z1.qiniu.com").
    			build();
    }

    /**
     * 华北机房内网上传相关域名
     */
    public static Region qvmHuabei() {
        return qvmRegion1();
    }

    /**
     * 华南机房相关域名
     */
    public static Region region2() {
    	return new Builder().
    			region("z2").
    			srcUpHost("up-z2.qiniup.com", "up-dg.qiniup.com", "up-fs.qiniup.com").
    			accUpHost("upload-z2.qiniup.com", "upload-dg.qiniup.com", "upload-fs.qiniup.com").
    			iovipHost("iovip-z2.qbox.me").
    			rsHost("rs-z2.qbox.me").
    			rsfHost("rsf-z2.qbox.me").
    			apiHost("api-z2.qiniu.com").
    			build();
    }

    /**
     * 华南机房相关域名
     */
    public static Region huanan() {
        return region2();
    }

    /**
     * 北美机房相关域名
     */
    public static Region regionNa0() {
    	return new Builder().
    			region("na0").
    			srcUpHost("up-na0.qiniup.com").
    			accUpHost("upload-na0.qiniup.com").
    			iovipHost("iovip-na0.qbox.me").
    			rsHost("rs-na0.qbox.me").
    			rsfHost("rsf-na0.qbox.me").
    			apiHost("api-na0.qiniu.com").
    			build();
    }

    /**
     * 北美机房相关域名
     */
    public static Region beimei() {
        return regionNa0();
    }

    /**
     * 新加坡相关域名
     */
    public static Region regionAs0() {
    	return new Builder().
    			region("na0").
    			srcUpHost("up-as0.qiniup.com").
    			accUpHost("upload-as0.qiniup.com").
    			iovipHost("iovip-as0.qbox.me").
    			rsHost("rs-as0.qbox.me").
    			rsfHost("rsf-as0.qbox.me").
    			apiHost("api-as0.qiniu.com").
    			build();
    }

    /**
     * 新加坡机房相关域名
     */
    public static Region xinjiapo() {
        return regionAs0();
    }

    /*
     * 自动根据AccessKey和Bucket来判断所在机房，并获取相关的域名
     * 空间所在的对应机房可以在空间创建的时候选择，或者创建完毕之后，从后台查看
     */
    public static Region autoRegion() {
        return new Builder().autoRegion();
    }

    public String getRegion(RegionReqInfo regionReqInfo) {
        return this.region;
    }

    public String getSrcUpHost(RegionReqInfo regionReqInfo) {
    	return this.srcUpHosts.get(0);
    }
    
    public String getAccUpHost(RegionReqInfo regionReqInfo) {
    	return this.accUpHosts.get(0);
    }
    
    public String getSrcUpHostBackup(RegionReqInfo regionReqInfo) {
    	if (srcUpHosts.size() > 1) {
    		return srcUpHosts.get(1);
    	}
    	return getSrcUpHost(regionReqInfo);
    }
    
    public String getAccUpHostBackup(RegionReqInfo regionReqInfo) {
    	if (accUpHosts.size() > 1) {
    		return accUpHosts.get(1);
    	}
    	return getAccUpHost(regionReqInfo);
    }
    
    public String getIovipHost(RegionReqInfo regionReqInfo) {
    	return iovipHost;
    }
    
    public String getRsHost(RegionReqInfo regionReqInfo) {
    	return rsHost;
    }
    
    public String getRsfHost(RegionReqInfo regionReqInfo) {
    	return rsfHost;
    }
    
    public String getApiHost(RegionReqInfo regionReqInfo) {
    	return apiHost;
    }

}
