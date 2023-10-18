package com.qiniu.storage;

import com.qiniu.common.AutoZone;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlUtils;

import java.util.*;

class ConfigHelper {
    private Configuration config;
    private UpHostHelper helper;

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
                + getHelper().upHost(config.region, upToken, UrlUtils.removeHostScheme(lastUsedHost), changeHost, mustReturnUpHost);
    }

    public String ioHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return getScheme() + config.region.getIovipHost(regionReqInfo);
    }

    public String ioSrcHost(String ak, String bucket) throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(ak, bucket);
        return config.region.getIoSrcHost(regionReqInfo);
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
        String host = "";
        try {
            host = config.region.getRsHost(null);
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }
        if (host == null || host.length() == 0) {
            host = Configuration.defaultRsHost;
        }
        return getScheme() + host;
    }

    public String apiHost() {
        String host = "";
        try {
            host = config.region.getApiHost(null);
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }
        if (host == null || host.length() == 0) {
            host = Configuration.defaultApiHost;
        }
        return getScheme() + host;
    }

    public String ucHost() {
        String host = "";
        try {
            host = config.region.getUcHost(null);
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }
        if (host == null || host.length() == 0) {
            host = Configuration.defaultUcHost;
        }
        return getScheme() + host;
    }

    List<String> ucHostsWithoutScheme() {
        List<String> hosts = new ArrayList<>();
        try {
            List<String> hostList = config.region.getUcHosts(null);
            if (hostList != null) {
                hosts.addAll(hostList);
            }
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }

        hosts = Arrays.asList(removeHostsSchemeAndHostsNoDuplication(hosts));

        if (hosts.size() == 0) {
            hosts = Arrays.asList(Configuration.defaultUcHosts);
        }

        return new ArrayList<>(hosts);
    }

    List<String> upHostsWithoutScheme() {
        List<String> hosts = new ArrayList<>();
        try {
            List<String> srcUpHost = config.region.getSrcUpHost(null);
            if (srcUpHost != null) {
                hosts.addAll(srcUpHost);
            }
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }

        try {
            List<String> accUpHost = config.region.getAccUpHost(null);
            if (accUpHost != null) {
                hosts.addAll(accUpHost);
            }
        } catch (QiniuException exception) {
            exception.printStackTrace();
        }

        hosts = Arrays.asList(removeHostsSchemeAndHostsNoDuplication(hosts));
        return new ArrayList<>(hosts);
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

    private UpHostHelper getHelper() {
        if (helper == null) {
            helper = new UpHostHelper(this.config, 60 * 15);
        }
        return helper;
    }

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
            return UrlUtils.removeHostScheme(https);
        } else {
            return UrlUtils.removeHostScheme(http);
        }
    }

    private String[] getHosts(String https, String http) {
        List<String> hosts = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(https)) {
            hosts.add(https);
        }
        if (!config.useHttpsDomains && !StringUtils.isNullOrEmpty(http)) {
            hosts.add(http);
        }
        return removeHostsSchemeAndHostsNoDuplication(hosts);
    }

    private String[] removeHostsSchemeAndHostsNoDuplication(List<String> hosts) {
        if (hosts == null || hosts.size() == 0) {
            return new String[]{};
        }

        Set<String> newHosts = new HashSet<>();
        for (int i = 0; i < hosts.size(); i++) {
            String host = UrlUtils.removeHostScheme(hosts.get(i));
            if (!StringUtils.isNullOrEmpty(host)) {
                newHosts.add(host);
            }
        }
        return newHosts.toArray(new String[0]);
    }
}

