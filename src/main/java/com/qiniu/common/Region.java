package com.qiniu.common;

public class Region {
	
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    private String region;
    /**
     * 上传，备用上传，备用上传IP和源站资源域名使用各个机房对应的域名
     */
    private String upHttp;
    private String upHttps;
    private String upBackupHttp;
    private String upBackupHttps;
    private String upIpHttp;
    private String upIpHttps;
    private String iovipHttp;
    private String iovipHttps;

    /**
     * 资源管理，资源列表，资源处理类域名
     * 默认的这组域名是国内国外共用域名，无论海外国内都可以访问
     * 只有在无法自动查询到机房对应具体域名情况下，使用这组域名
     */
    private String rsHttp = "http://rs.qiniu.com";
    private String rsHttps = "https://rs.qbox.me";
    private String rsfHttp = "http://rsf.qiniu.com";
    private String rsfHttps = "https://rsf.qbox.me";
    private String apiHttp = "http://api.qiniu.com";
    private String apiHttps = "https://api.qiniu.com";

    /**
     * 域名构造器
     */
    static class Builder {
        private Region region;

        public Builder() {
        	region = new Region();
        }

        public Builder(Region originZone) {
            this();
            region.region = originZone.region;
            region.upHttp = originZone.upHttp;
            region.upHttps = originZone.upHttps;
            region.upBackupHttp = originZone.upBackupHttp;
            region.upBackupHttps = originZone.upBackupHttps;
            region.upIpHttp = originZone.upIpHttp;
            region.upIpHttps = originZone.upIpHttps;
            region.iovipHttp = originZone.iovipHttp;
            region.iovipHttps = originZone.iovipHttps;
            region.rsHttp = originZone.rsHttp;
            region.rsHttps = originZone.rsHttps;
            region.rsfHttp = originZone.rsfHttp;
            region.rsfHttps = originZone.rsfHttps;
            region.apiHttp = originZone.apiHttp;
            region.apiHttps = originZone.apiHttps;
        }

        public Builder region(String region) {
        	this.region.region = region;
            return this;
        }

        public Builder upHttp(String upHttp) {
        	region.upHttp = upHttp;
            return this;
        }
        
        public Builder upHttps(String upHttps) {
        	region.upHttps = upHttps;
            return this;
        }

        public Builder upBackupHttp(String upBackupHttp) {
        	region.upBackupHttp = upBackupHttp;
            return this;
        }

        public Builder upBackupHttps(String upBackupHttps) {
        	region.upBackupHttps = upBackupHttps;
            return this;
        }

        public Builder upIpHttp(String upIpHttp) {
        	region.upIpHttp = upIpHttp;
            return this;
        }

        public Builder upIpHttps(String upIpHttps) {
        	region.upIpHttps = upIpHttps;
            return this;
        }

        public Builder iovipHttp(String iovipHttp) {
        	region.iovipHttp = iovipHttp;
            return this;
        }

        public Builder iovipHttps(String iovipHttps) {
        	region.iovipHttps = iovipHttps;
            return this;
        }

        public Builder rsHttp(String rsHttp) {
        	region.rsHttp = rsHttp;
            return this;
        }

        public Builder rsHttps(String rsHttps) {
        	region.rsHttps = rsHttps;
            return this;
        }

        public Builder rsfHttp(String rsfHttp) {
        	region.rsfHttp = rsfHttp;
            return this;
        }

        public Builder rsfHttps(String rsfHttps) {
        	region.rsfHttps = rsfHttps;
            return this;
        }

        public Builder apiHttp(String apiHttp) {
        	region.apiHttp = apiHttp;
            return this;
        }

        public Builder apiHttps(String apiHttps) {
        	region.apiHttps = apiHttps;
            return this;
        }


        /**
         * 自动选择,其它参数设置无效
         */
        public Region autoRegion() {
            return AutoRegion.instance;
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
        return new Builder().region("z0").upHttp("http://up.qiniu.com").upHttps("https://up.qbox.me").
                upBackupHttp("http://upload.qiniu.com").upBackupHttps("https://upload.qbox.me").
                iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me").
                rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
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
        return new Builder().region("z0").upHttp("http://free-qvm-z0-xs.qiniup.com").
                upHttps("https://free-qvm-z0-xs.qiniup.com").
                upBackupHttp("http://free-qvm-z0-xs.qiniup.com").upBackupHttps("https://free-qvm-z0-xs.qiniup.com").
                iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me").
                rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
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
        return new Builder().region("z1").upHttp("http://up-z1.qiniu.com").upHttps("https://up-z1.qbox.me").
                upBackupHttp("http://upload-z1.qiniu.com").upBackupHttps("https://upload-z1.qbox.me").
                iovipHttp("http://iovip-z1.qbox.me").iovipHttps("https://iovip-z1.qbox.me").
                rsHttp("http://rs-z1.qiniu.com").rsHttps("https://rs-z1.qbox.me")
                .rsfHttp("http://rsf-z1.qiniu.com").rsfHttps("https://rsf-z1.qbox.me")
                .apiHttp("http://api-z1.qiniu.com").apiHttps("https://api-z1.qiniu.com").build();
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
        return new Builder().region("z1").upHttp("http://free-qvm-z1-zz.qiniup.com").
                upHttps("https://free-qvm-z1-zz.qiniup.com").
                upBackupHttp("http://free-qvm-z1-zz.qiniup.com").upBackupHttps("https://free-qvm-z1-zz.qiniup.com").
                iovipHttp("http://iovip-z1.qbox.me").iovipHttps("https://iovip-z1.qbox.me").
                rsHttp("http://rs-z1.qiniu.com").rsHttps("https://rs-z1.qbox.me")
                .rsfHttp("http://rsf-z1.qiniu.com").rsfHttps("https://rsf-z1.qbox.me")
                .apiHttp("http://api-z1.qiniu.com").apiHttps("https://api-z1.qiniu.com").build();
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
        return new Builder().region("z2").upHttp("http://up-z2.qiniu.com").upHttps("https://up-z2.qbox.me").
                upBackupHttp("http://upload-z2.qiniu.com").upBackupHttps("https://upload-z2.qbox.me").
                iovipHttp("http://iovip-z2.qbox.me").iovipHttps("https://iovip-z2.qbox.me").
                rsHttp("http://rs-z2.qiniu.com").rsHttps("https://rs-z2.qbox.me")
                .rsfHttp("http://rsf-z2.qiniu.com").rsfHttps("https://rsf-z2.qbox.me")
                .apiHttp("http://api-z2.qiniu.com").apiHttps("https://api-z2.qiniu.com").build();
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
        return new Builder().region("na0").upHttp("http://up-na0.qiniu.com").upHttps("https://up-na0.qbox.me").
                upBackupHttp("http://upload-na0.qiniu.com").upBackupHttps("https://upload-na0.qbox.me").
                iovipHttp("http://iovip-na0.qbox.me").iovipHttps("https://iovip-na0.qbox.me").
                rsHttp("http://rs-na0.qiniu.com").rsHttps("https://rs-na0.qbox.me")
                .rsfHttp("http://rsf-na0.qiniu.com").rsfHttps("https://rsf-na0.qbox.me")
                .apiHttp("http://api-na0.qiniu.com").apiHttps("https://api-na0.qiniu.com").build();
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
        return new Builder().region("as0").upHttp("http://up-as0.qiniu.com").upHttps("https://up-as0.qbox.me").
                upBackupHttp("http://upload-as0.qiniu.com").upBackupHttps("https://upload-as0.qbox.me").
                iovipHttp("http://iovip-as0.qbox.me").iovipHttps("https://iovip-as0.qbox.me").
                rsHttp("http://rs-as0.qiniu.com").rsHttps("https://rs-as0.qbox.me")
                .rsfHttp("http://rsf-as0.qiniu.com").rsfHttps("https://rsf-as0.qbox.me")
                .apiHttp("http://api-as0.qiniu.com").apiHttps("https://api-as0.qiniu.com").build();
    }

    /**
     * 新加坡机房相关域名
     */
    public static Region xinjiapo() {
        return regionAs0();
    }

    /**
     * 自动根据AccessKey和Bucket来判断所在机房，并获取相关的域名
     * 空间所在的对应机房可以在空间创建的时候选择，或者创建完毕之后，从后台查看
     */
    public static Region autoRegion() {
        return new Builder().autoRegion();
    }


    public String getRegion(RegionReqInfo regionReqInfo) {
        return this.region;
    }

    /**
     * 保留自动获取上传和资源抓取，更新相关域名接口
     */

    public String getUpHttp(RegionReqInfo regionReqInfo) {
        return this.upHttp;
    }

    public String getUpHttps(RegionReqInfo regionReqInfo) {
        return this.upHttps;
    }

    public String getUpBackupHttp(RegionReqInfo regionReqInfo) {
        return this.upBackupHttp;
    }

    public String getUpBackupHttps(RegionReqInfo regionReqInfo) {
        return this.upBackupHttps;
    }

    public String getUpIpHttp(RegionReqInfo regionReqInfo) {
        return this.upIpHttp;
    }

    public String getUpIpHttps(RegionReqInfo regionReqInfo) {
        return this.upIpHttps;
    }

    public String getIovipHttp(RegionReqInfo regionReqInfo) {
        return this.iovipHttp;
    }

    public String getIovipHttps(RegionReqInfo regionReqInfo) {
        return this.iovipHttps;
    }


    /**
     * 保留自动获取资源管理，资源列表，资源处理相关域名接口
     */
    public String getRsHttp(RegionReqInfo regionReqInfo) {
        return rsHttp;
    }

    public String getRsHttps(RegionReqInfo regionReqInfo) {
        return rsHttps;
    }

    public String getRsfHttp(RegionReqInfo regionReqInfo) {
        return rsfHttp;
    }

    public String getRsfHttps(RegionReqInfo regionReqInfo) {
        return rsfHttps;
    }

    public String getApiHttp(RegionReqInfo regionReqInfo) {
        return apiHttp;
    }

    public String getApiHttps(RegionReqInfo regionReqInfo) {
        return apiHttps;
    }

    /**
     * 获取资源管理，资源列表，资源处理相关域名
     */
    public String getRegion() {
        return this.region;
    }

    public String getRsHttp() {
        return rsHttp;
    }

    public String getRsHttps() {
        return rsHttps;
    }

    public String getRsfHttp() {
        return rsfHttp;
    }

    public String getRsfHttps() {
        return rsfHttps;
    }

    public String getApiHttp() {
        return apiHttp;
    }

    public String getApiHttps() {
        return apiHttps;
    }
    
}
