package com.qiniu.common;

/**
 * 多区域上传域名
 */
@Deprecated
public class Zone {

    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    private String region = "z0";
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
     * 华东机房相关域名
     */
    public static Zone zone0() {
        return new Builder().region("z0")
                .upHttp("http://upload.qiniup.com").upHttps("https://upload.qiniup.com")
                .upBackupHttp("http://up.qiniup.com").upBackupHttps("https://up.qiniup.com")
                .iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me")
                .rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com")
                .build();
    }

    /**
     * 华东机房相关域名
     */
    public static Zone huadong() {
        return zone0();
    }

    /**
     * 华东机房内网上传相关域名
     */
    public static Zone qvmZone0() {
        return new Builder().region("z0")
                .upHttp("http://free-qvm-z0-xs.qiniup.com").upHttps("https://free-qvm-z0-xs.qiniup.com")
                .upBackupHttp("http://free-qvm-z0-xs.qiniup.com").upBackupHttps("https://free-qvm-z0-xs.qiniup.com")
                .iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me")
                .rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com")
                .build();
    }

    /**
     * 华东机房内网上传相关域名
     */
    public static Zone qvmHuadong() {
        return qvmZone0();
    }

    /**
     * 华北机房相关域名
     */
    public static Zone zone1() {
        return new Builder().region("z1")
                .upHttp("http://upload-z1.qiniup.com").upHttps("https://upload-z1.qiniup.com")
                .upBackupHttp("http://up-z1.qiniup.com").upBackupHttps("https://up-z1.qiniup.com")
                .iovipHttp("http://iovip-z1.qbox.me").iovipHttps("https://iovip-z1.qbox.me")
                .rsHttp("http://rs-z1.qiniu.com").rsHttps("https://rs-z1.qbox.me")
                .rsfHttp("http://rsf-z1.qiniu.com").rsfHttps("https://rsf-z1.qbox.me")
                .apiHttp("http://api-z1.qiniu.com").apiHttps("https://api-z1.qiniu.com")
                .build();
    }

    /**
     * 华北机房相关域名
     */
    public static Zone huabei() {
        return zone1();
    }

    /**
     * 华北机房内网上传相关域名
     */
    public static Zone qvmZone1() {
        return new Builder().region("z1")
                .upHttp("http://free-qvm-z1-zz.qiniup.com").upHttps("https://free-qvm-z1-zz.qiniup.com")
                .upBackupHttp("http://free-qvm-z1-zz.qiniup.com").upBackupHttps("https://free-qvm-z1-zz.qiniup.com")
                .iovipHttp("http://iovip-z1.qbox.me").iovipHttps("https://iovip-z1.qbox.me")
                .rsHttp("http://rs-z1.qiniu.com").rsHttps("https://rs-z1.qbox.me")
                .rsfHttp("http://rsf-z1.qiniu.com").rsfHttps("https://rsf-z1.qbox.me")
                .apiHttp("http://api-z1.qiniu.com").apiHttps("https://api-z1.qiniu.com")
                .build();
    }

    /**
     * 华北机房内网上传相关域名
     */
    public static Zone qvmHuabei() {
        return qvmZone1();
    }

    /**
     * 华南机房相关域名
     */
    public static Zone zone2() {
        return new Builder().region("z2")
                .upHttp("http://upload-z2.qiniup.com").upHttps("https://upload-z2.qiniup.com")
                .upBackupHttp("http://up-z2.qiniup.com").upBackupHttps("https://up-z2.qiniup.com")
                .iovipHttp("http://iovip-z2.qbox.me").iovipHttps("https://iovip-z2.qbox.me")
                .rsHttp("http://rs-z2.qiniu.com").rsHttps("https://rs-z2.qbox.me")
                .rsfHttp("http://rsf-z2.qiniu.com").rsfHttps("https://rsf-z2.qbox.me")
                .apiHttp("http://api-z2.qiniu.com").apiHttps("https://api-z2.qiniu.com")
                .build();
    }

    /**
     * 华南机房相关域名
     */
    public static Zone huanan() {
        return zone2();
    }

    /**
     * 北美机房相关域名
     */
    public static Zone zoneNa0() {
        return new Builder().region("na0")
                .upHttp("http://upload-na0.qiniup.com").upHttps("https://upload-na0.qiniup.com")
                .upBackupHttp("http://up-na0.qiniup.com").upBackupHttps("https://up-na0.qiniup.com")
                .iovipHttp("http://iovip-na0.qbox.me").iovipHttps("https://iovip-na0.qbox.me")
                .rsHttp("http://rs-na0.qiniu.com").rsHttps("https://rs-na0.qbox.me")
                .rsfHttp("http://rsf-na0.qiniu.com").rsfHttps("https://rsf-na0.qbox.me")
                .apiHttp("http://api-na0.qiniu.com").apiHttps("https://api-na0.qiniu.com")
                .build();
    }

    /**
     * 北美机房相关域名
     */
    public static Zone beimei() {
        return zoneNa0();
    }

    /**
     * 新加坡相关域名
     */
    public static Zone zoneAs0() {
        return new Builder().region("as0")
                .upHttp("http://upload-as0.qiniup.com").upHttps("https://upload-as0.qiniup.com")
                .upBackupHttp("http://up-as0.qiniup.com").upBackupHttps("https://up-as0.qiniup.com")
                .iovipHttp("http://iovip-as0.qbox.me").iovipHttps("https://iovip-as0.qbox.me")
                .rsHttp("http://rs-as0.qiniu.com").rsHttps("https://rs-as0.qbox.me")
                .rsfHttp("http://rsf-as0.qiniu.com").rsfHttps("https://rsf-as0.qbox.me")
                .apiHttp("http://api-as0.qiniu.com").apiHttps("https://api-as0.qiniu.com")
                .build();
    }

    /**
     * 新加坡机房相关域名
     */
    public static Zone xinjiapo() {
        return zoneAs0();
    }

    /**
     * 雾存储 华东-1
     */
    public static Zone zoneFogCnEast1() {
        return new Builder()
                .region("fog-cn-east-1")
                .upHttp("http://upload-fog-cn-east-1.qiniup.com").upHttps("https://upload-fog-cn-east-1.qiniup.com")
                .upBackupHttp("http://up-fog-cn-east-1.qiniup.com").upBackupHttps("https://up-fog-cn-east-1.qiniup.com")
                .iovipHttp("http://iovip-fog-cn-east-1.qbox.me").iovipHttps("https://iovip-fog-cn-east-1.qbox.me")
                .rsHttp("http://rs-fog-cn-east-1.qbox.me").rsHttps("https://rs-fog-cn-east-1.qbox.me")
                .rsfHttp("http://rsf-fog-cn-east-1.qbox.me").rsfHttps("https://rsf-fog-cn-east-1.qbox.me")
                .apiHttp("http://api-fog-cn-east-1.qiniuapi.com").apiHttps("https://api-fog-cn-east-1.qiniuapi.com")
                .build();
    }

    /**
     * 自动根据AccessKey和Bucket来判断所在机房，并获取相关的域名
     * 空间所在的对应机房可以在空间创建的时候选择，或者创建完毕之后，从后台查看
     */
    public static Zone autoZone() {
        return new Builder().autoZone();
    }

    public String getRegion(ZoneReqInfo zoneReqInfo) {
        return this.region;
    }

    /**
     * 保留自动获取上传和资源抓取，更新相关域名接口
     */

    public String getUpHttp(ZoneReqInfo zoneReqInfo) {
        return this.upHttp;
    }

    public String getUpHttps(ZoneReqInfo zoneReqInfo) {
        return this.upHttps;
    }

    public String getUpBackupHttp(ZoneReqInfo zoneReqInfo) {
        return this.upBackupHttp;
    }

    public String getUpBackupHttps(ZoneReqInfo zoneReqInfo) {
        return this.upBackupHttps;
    }

    public String getUpIpHttp(ZoneReqInfo zoneReqInfo) {
        return this.upIpHttp;
    }

    public String getUpIpHttps(ZoneReqInfo zoneReqInfo) {
        return this.upIpHttps;
    }

    public String getIovipHttp(ZoneReqInfo zoneReqInfo) {
        return this.iovipHttp;
    }

    public String getIovipHttps(ZoneReqInfo zoneReqInfo) {
        return this.iovipHttps;
    }

    /**
     * 保留自动获取资源管理，资源列表，资源处理相关域名接口
     */
    public String getRsHttp(ZoneReqInfo zoneReqInfo) {
        return rsHttp;
    }

    public String getRsHttps(ZoneReqInfo zoneReqInfo) {
        return rsHttps;
    }

    public String getRsfHttp(ZoneReqInfo zoneReqInfo) {
        return rsfHttp;
    }

    public String getRsfHttps(ZoneReqInfo zoneReqInfo) {
        return rsfHttps;
    }

    public String getApiHttp(ZoneReqInfo zoneReqInfo) {
        return apiHttp;
    }

    public String getApiHttps(ZoneReqInfo zoneReqInfo) {
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

    /**
     * 域名构造器
     */
    public static class Builder {
        private Zone zone;

        public Builder() {
            zone = new Zone();
        }

        public Builder(Zone originZone) {
            this();
            zone.region = originZone.region;
            zone.upHttp = originZone.upHttp;
            zone.upHttps = originZone.upHttps;
            zone.upBackupHttp = originZone.upBackupHttp;
            zone.upBackupHttps = originZone.upBackupHttps;
            zone.upIpHttp = originZone.upIpHttp;
            zone.upIpHttps = originZone.upIpHttps;
            zone.iovipHttp = originZone.iovipHttp;
            zone.iovipHttps = originZone.iovipHttps;
            zone.rsHttp = originZone.rsHttp;
            zone.rsHttps = originZone.rsHttps;
            zone.rsfHttp = originZone.rsfHttp;
            zone.rsfHttps = originZone.rsfHttps;
            zone.apiHttp = originZone.apiHttp;
            zone.apiHttps = originZone.apiHttps;
        }

        public Builder region(String region) {
            zone.region = region;
            return this;
        }

        public Builder upHttp(String upHttp) {
            zone.upHttp = upHttp;
            return this;
        }

        public Builder upBackupHttp(String upBackupHttp) {
            zone.upBackupHttp = upBackupHttp;
            return this;
        }

        public Builder upHttps(String upHttps) {
            zone.upHttps = upHttps;
            return this;
        }

        public Builder upBackupHttps(String upBackupHttps) {
            zone.upBackupHttps = upBackupHttps;
            return this;
        }

        public Builder upIpHttp(String upIpHttp) {
            zone.upIpHttp = upIpHttp;
            return this;
        }

        public Builder upIpHttps(String upIpHttps) {
            zone.upIpHttps = upIpHttps;
            return this;
        }

        public Builder iovipHttp(String iovipHttp) {
            zone.iovipHttp = iovipHttp;
            return this;
        }

        public Builder iovipHttps(String iovipHttps) {
            zone.iovipHttps = iovipHttps;
            return this;
        }

        public Builder rsHttp(String rsHttp) {
            zone.rsHttp = rsHttp;
            return this;
        }

        public Builder rsHttps(String rsHttps) {
            zone.rsHttps = rsHttps;
            return this;
        }

        public Builder rsfHttp(String rsfHttp) {
            zone.rsfHttp = rsfHttp;
            return this;
        }

        public Builder rsfHttps(String rsfHttps) {
            zone.rsfHttps = rsfHttps;
            return this;
        }

        public Builder apiHttp(String apiHttp) {
            zone.apiHttp = apiHttp;
            return this;
        }

        public Builder apiHttps(String apiHttps) {
            zone.apiHttps = apiHttps;
            return this;
        }


        /**
         * 自动选择,其它参数设置无效
         */
        public Zone autoZone() {
            return AutoZone.instance;
        }

        /**
         * 返回构建好的Zone对象
         */
        public Zone build() {
            return zone;
        }
    }

}
