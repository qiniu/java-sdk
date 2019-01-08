package com.qiniu.common;

/**
 * 建议用AutoRegion
 */
@Deprecated
public class AutoZone extends AutoRegion {
	
	@Deprecated
    public static AutoZone instance = new AutoZone();

	@Deprecated
    public AutoZone() {
        this("https://uc.qbox.me");
    }

	@Deprecated
    public AutoZone(String ucServer) {
    	super(ucServer);
    }
    
	@Deprecated
    public ZoneInfo queryZoneInfo(String accessKey, String bucket) throws QiniuException {
    	return (ZoneInfo) super.queryRegionInfo(accessKey, bucket);
    }
    
	@Deprecated
    public ZoneInfo queryZoneInfo(ZoneReqInfo zoneReqInfo) {
    	return (ZoneInfo) super.queryRegionInfo(zoneReqInfo);
    }
    
	@Deprecated
    public String getUpHttp(ZoneReqInfo zoneReqInfo) {
    	return super.getUpHttp(zoneReqInfo);
    }
	
	@Deprecated
	public String getUpHttps(ZoneReqInfo zoneReqInfo) {
		return super.getUpHttps(zoneReqInfo);
	}
    
	@Deprecated
    public String getUpBackupHttp(ZoneReqInfo zoneReqInfo) {
    	return super.getUpBackupHttp(zoneReqInfo);
    }
	
	@Deprecated
	public String getUpBackupHttps(ZoneReqInfo zoneReqInfo) {
		return super.getUpBackupHttps(zoneReqInfo);
	}
    
	@Deprecated
    public String getUpIpHttp(ZoneReqInfo zoneReqInfo) {
    	return super.getUpIpHttp(zoneReqInfo);
    }
	
	@Deprecated
	public String getUpIpHttps(ZoneReqInfo zoneReqInfo) {
		return super.getUpIpHttps(zoneReqInfo);
	}
    
	@Deprecated
    public String getIovipHttp(ZoneReqInfo zoneReqInfo) {
    	return super.getIovipHttp(zoneReqInfo);
    }

	@Deprecated
	public String getIovipHttps(ZoneReqInfo zoneReqInfo) {
		return super.getIovipHttps(zoneReqInfo);
	}
	
	@Deprecated
	public String getRsHttp(ZoneReqInfo zoneReqInfo) {
		return super.getRsHttp(zoneReqInfo);
	}
	
	@Deprecated
	public String getRsHttps(ZoneReqInfo zoneReqInfo) {
		return super.getRsfHttps(zoneReqInfo);
	}
	
	@Deprecated
	public String getRsfHttp(ZoneReqInfo zoneReqInfo) {
		return super.getRsfHttp(zoneReqInfo);
	}
	
	@Deprecated
	public String getRsfHttps(ZoneReqInfo zoneReqInfo) {
		return super.getRsfHttps(zoneReqInfo);
	}
	
	@Deprecated
	public String getApiHttp(ZoneReqInfo zoneReqInfo) {
		return super.getApiHttp(zoneReqInfo);
	}
	
	@Deprecated
	public String getApiHttps(ZoneReqInfo zoneReqInfo) {
		return super.getApiHttps(zoneReqInfo);
	}
    
    @Deprecated
    static class ZoneInfo extends AutoRegion.RegionInfo {

		protected ZoneInfo(String upHttp, String upBackupHttp, String upIpHttp, String iovipHttp, String upHttps,
				String upBackupHttps, String upIpHttps, String iovipHttps) {
			super(upHttp, upBackupHttp, upIpHttp, iovipHttp, upHttps, upBackupHttps, upIpHttps, iovipHttps);
		}
    }
}
