package com.qiniu.storage;

import com.qiniu.common.AutoZone;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.util.StringUtils;

class ConfigHelper {
    private Configuration config;

    ConfigHelper(Configuration config) {
        this.config = config;
        makeSureRegion();
    }

    public String upHost(String upToken) throws QiniuException {
        try {
            return upHost(upToken, null, false, false);
        } catch (QiniuException e) {
            if (e.response == null || e.response.needRetry()) {
                try {
                    Thread.sleep(500);
                } catch (Exception e1) {
                    // do nothing
                }
                return upHost(upToken, null, false, true);
            }
            throw e;
        }
    }

    public String tryChangeUpHost(String upToken, String lastUsedHost) throws QiniuException {
        return upHost(upToken, lastUsedHost, true, false);
    }

    private String upHost(String upToken, String lastUsedHost, boolean changeHost, boolean mustReturnUpHost)
            throws QiniuException {
        return getScheme()
                + getHelper().upHost(config.region, upToken, toDomain(lastUsedHost), changeHost, mustReturnUpHost);
    }


    public String ioHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + config.region.getIovipHost(regionReqInfo);
    }

    public String apiHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + config.region.getApiHost(regionReqInfo);
    }

    public String rsHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
            return getScheme() + config.region.getRsHost(regionReqInfo);
    }

    public String rsfHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + config.region.getRsfHost(regionReqInfo);
    }

    public String rsHost() {
        return getScheme() + config.defaultRsHost;
    }

    public String apiHost() {
        return getScheme() + config.defaultApiHost;
    }

    public String ucHost() {
        return getScheme() + config.defaultUcHost;
    }

    private String getScheme() {
        return config.useHttpsDomains ? "https://" : "http://";
    }

    private void makeSureRegion() {
        if (config.region == null) {
            if (config.zone != null) {
                config.region = toRegion(config.zone);
            } else {
                config.region = Region.autoRegion();
            }
        }
    }

    private UpHostHelper helper;

    private UpHostHelper getHelper() {
        if (helper == null) {
            helper = new UpHostHelper(this.config, 60 * 15);
        }
        return helper;
    }



    /*
    * public Builder(Zone originZone) {
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

        return new Builder().region("z0")
                .upHttp("http://upload.qiniup.com").upHttps("https://upload.qiniup.com")
                .upBackupHttp("http://up.qiniup.com").upBackupHttps("https://up.qiniup.com")
                .iovipHttp("http://iovip.qbox.me").iovipHttps("https://iovip.qbox.me")
                .rsHttp("http://rs.qiniu.com").rsHttps("https://rs.qbox.me")
                .rsfHttp("http://rsf.qiniu.com").rsfHttps("https://rsf.qbox.me")
                .apiHttp("http://api.qiniu.com").apiHttps("https://api.qiniu.com")
                .build();

    *
    * public Builder(Region originRegion) {
            init();
            region.region = originRegion.region;
            region.srcUpHosts = originRegion.srcUpHosts;
            region.accUpHosts = originRegion.accUpHosts;
            region.iovipHost = originRegion.iovipHost;
            region.rsHost = originRegion.rsHost;
            region.rsfHost = originRegion.rsfHost;
            region.apiHost = originRegion.apiHost;
        }

        return new Builder().
                region("z0").
                srcUpHost("up.qiniup.com", "up-jjh.qiniup.com", "up-xs.qiniup.com").
                accUpHost("upload.qiniup.com", "upload-jjh.qiniup.com", "upload-xs.qiniup.com").
                iovipHost("iovip.qbox.me").
                rsHost("rs.qbox.me").
                rsfHost("rsf.qbox.me").
                apiHost("api.qiniu.com").
                build();
    * */

    private Region toRegion(Zone zone) {
        if (zone instanceof AutoZone) {
            AutoZone autoZone = (AutoZone) zone;
            return Region.autoRegion(autoZone.ucServer);
        }
        // accUpHostFirst default value is true
        // from the zone accUpHostFirst must be true, (it is a new field)
        // true, acc map the upHttp, upHttps
        // false, src map to the backs
        // non autozone, zoneRegionInfo is useless
        return new Region.Builder()
                .region(zone.getRegion())
                .accUpHost(getHosts(zone.getUpHttps(null), zone.getUpHttp(null)))
                .srcUpHost(getHosts(zone.getUpBackupHttps(null), zone.getUpBackupHttp(null)))
                .iovipHost(getHost(zone.getIovipHttps(null), zone.getIovipHttp(null)))
                .rsHost(getHost(zone.getRsHttps(), zone.getRsHttp()))
                .rsfHost(getHost(zone.getRsfHttps(), zone.getRsfHttp()))
                .apiHost(getHost(zone.getApiHttps(), zone.getApiHttp()))
                .build();
    }


    private String getHost(String https, String http) {
        if (config.useHttpsDomains) {
            return toDomain(https);
        } else {
            return toDomain(http);
        }
    }

    private String[] getHosts(String https, String http) {
        if (config.useHttpsDomains) {
            // https would not be null
            return new String[]{toDomain(https)};
        } else {
            // http, s1 would not be null
            String s1 = toDomain(http);
            String s2 = toDomain(https);
            if (s2 != null && !s2.equalsIgnoreCase(s1)) {
                return new String[]{s1, s2};
            }
            return new String[]{s1};
        }
    }

    private String toDomain(String d1) {
        if (StringUtils.isNullOrEmpty(d1)) {
            return null;
        }
        int s = d1.indexOf("://");
        if (s > -1) {
            return d1.substring(s + 3);
        }
        return d1;
    }

}

