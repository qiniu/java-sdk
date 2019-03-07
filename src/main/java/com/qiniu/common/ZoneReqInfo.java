package com.qiniu.common;

/**
 * 建议用RegionReqInfo
 */
@Deprecated
public class ZoneReqInfo extends RegionReqInfo {

	@Deprecated
	public ZoneReqInfo(String token) throws QiniuException {
		super(token);
	}
	
	@Deprecated
    public ZoneReqInfo(String accessKey, String bucket) {
    	super(accessKey, bucket);
    }
}
