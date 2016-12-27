package com.qiniu.common;

/**
 * 上传多区域。若参数不设置, 上传、下载、拉取使用华东区域地址,管理使用转发地址。
 */
public class Zone {
    // 地址:  https://cf.qiniu.io/pages/viewpage.action?pageId=16092953

    private String upHttp = "http://up.qiniu.com";
    private String upHttps = "https://up.qbox.me";
    private String upBackupHttp = "http://upload.qiniu.com";
    private String upBackupHttps = "https://upload.qbox.me";
    private String upIpHttp = "";
    private String upIpHttps = "";
    private String iovipHttp = "http://iovip.qbox.me";
    private String iovipHttps = "https://iovip.qbox.me";


    private String rsHttp = "http://rs.qiniu.com";
    private String rsHttps = "https://rs.qbox.me";
    private String rsfHttp = "http://rsf.qiniu.com";
    private String rsfHttps = "https://rsf.qbox.me";
    private String apiHttp = "http://api.qiniu.com";
    private String apiHttps = "https://api.qiniu.com";


    /**
     * 华东
     */
    public static Zone zone0() {
        return new Builder().up("http://up.qiniu.com").upHttps("https://up.qbox.me").
                upBackup("http://upload.qiniu.com").upBackupHttps("https://upload.qbox.me").
                iovip("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me").
                rs("http://rs-z0.qiniu.com").rsHttps("https://rs-z0.qbox.me").
                rsf("http://rsf-z0.qiniu.com").rsfHttps("https://rsf-z0.qbox.me").
                api("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
    }

    /**
     * 华北
     */
    public static Zone zone1() {
        return new Builder().up("http://up-z1.qiniu.com").upHttps("https://up-z1.qbox.me").
                upBackup("http://upload-z1.qiniu.com").upBackupHttps("https://upload-z1.qbox.me").
                iovip("http://iovip-z1.qbox.me").iovipHttps("https://iovip-z1.qbox.me").
                rs("http://rs-z1.qiniu.com").rsHttps("https://rs-z1.qbox.me").
                rsf("http://rsf-z1.qiniu.com").rsfHttps("https://rsf-z1.qbox.me").
                api("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
    }

    /**
     * 华南
     */
    public static Zone zone2() {
        return new Builder().up("http://up-z2.qiniu.com").upHttps("https://up-z2.qbox.me").
                upBackup("http://upload-z2.qiniu.com").upBackupHttps("https://upload-z2.qbox.me").
                iovip("http://iovip-z2.qbox.me").iovipHttps("https://iovip-z2.qbox.me").
                rs("http://rs-z2.qiniu.com").rsHttps("https://rs-z2.qbox.me").
                rsf("http://rsf-z2.qiniu.com").rsfHttps("https://rsf-z2.qbox.me").
                api("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
    }

    /**
     * 北美
     */
    public static Zone zoneNa0() {
        return new Builder().up("http://up-na0.qiniu.com").upHttps("https://up-na0.qbox.me").
                upBackup("http://upload-na0.qiniu.com").upBackupHttps("https://upload-na0.qbox.me").
                iovip("http://iovip-na0.qbox.me").iovipHttps("https://iovip-na0.qbox.me").
                rs("http://rs-na0.qiniu.com").rsHttps("https://rs-na0.qbox.me").
                rsf("http://rsf-na0.qiniu.com").rsfHttps("https://rsf-na0.qbox.me").
                api("http://api.qiniu.com").apiHttps("https://api.qiniu.com").build();
    }

    /**
     * 自动判断区域,用于快速接入。实际中推荐使用固定 zone 。
     * 空间所属区域,可到 portal 查询。
     */
    public static Zone autoZone() {
        return new Builder().autoZone();
    }

    public String getUpHttp(ZoneReqInfo ab) {
        return upHttp;
    }

    public String getUpBackupHttp(ZoneReqInfo ab) {
        return upBackupHttp;
    }

    public String getUpHttps(ZoneReqInfo ab) {
        return upHttps;
    }

    public String getUpBackupHttps(ZoneReqInfo ab) {
        return upBackupHttps;
    }

    public String getRsHttp(ZoneReqInfo ab) {
        return rsHttp;
    }

    public String getRsHttps(ZoneReqInfo ab) {
        return rsHttps;
    }

    public String getRsfHttp(ZoneReqInfo ab) {
        return rsfHttp;
    }

    public String getRsfHttps(ZoneReqInfo ab) {
        return rsfHttps;
    }

    public String getApiHttp(ZoneReqInfo ab) {
        return apiHttp;
    }

    public String getApiHttps(ZoneReqInfo ab) {
        return apiHttps;
    }

    public String getIovipHttp(ZoneReqInfo ab) {
        return iovipHttp;
    }

    public String getIovipHttps(ZoneReqInfo ab) {
        return iovipHttps;
    }

    public String getUpIpHttp(ZoneReqInfo ab) {
        return upIpHttp;
    }

    public String getUpIpHttps(ZoneReqInfo ab) {
        return upIpHttps;
    }


    /**
     * 默认为华东机房的配置
     */
    static class Builder {
        private Zone zone;

        public Builder() {
            zone = new Zone();
        }

        public Builder up(String up) {
            zone.upHttp = up;
            return this;
        }

        public Builder upBackup(String upBackup) {
            zone.upBackupHttp = upBackup;
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

        public Builder rs(String rs) {
            zone.rsHttp = rs;
            return this;
        }

        public Builder rsHttps(String rsHttps) {
            zone.rsHttps = rsHttps;
            return this;
        }

        public Builder api(String api) {
            zone.apiHttp = api;
            return this;
        }

        public Builder apiHttps(String apiHttps) {
            zone.apiHttps = apiHttps;
            return this;
        }

        public Builder iovip(String iovip) {
            zone.iovipHttp = iovip;
            return this;
        }

        public Builder iovipHttps(String iovipHttps) {
            zone.iovipHttps = iovipHttps;
            return this;
        }

        public Builder rsf(String rsf) {
            zone.rsfHttp = rsf;
            return this;
        }

        public Builder rsfHttps(String rsfHttps) {
            zone.rsfHttps = rsfHttps;
            return this;
        }

        public Builder upIpHttp(String upIpHttp) {
            zone.upIpHttp = upIpHttp;
            return this;
        }

        public Builder ipHttps(String upIpHttps) {
            zone.upIpHttps = upIpHttps;
            return this;
        }

        /**
         * 自动选择,其它参数设置无效
         */
        public Zone autoZone() {
            zone = AutoZone.instance;
            return zone;
        }

        public Zone build() {
            return zone;
        }
    }

}
