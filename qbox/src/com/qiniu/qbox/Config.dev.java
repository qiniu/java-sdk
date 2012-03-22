package com.qiniu.qbox;

public class Config {
	public static final String CLIENT_ID     = "abcd0c7edcdf914228ed8aa7c6cee2f2bc6155e2";
	public static final String CLIENT_SECRET = "fc9ef8b171a74e197b17f85ba23799860ddf3b9c";
	public static final String REDIRECT_URI  = "<RedirectURL>";
	public static final String AUTHORIZATION_ENDPOINT = "<AuthURL>";
	public static final String TOKEN_ENDPOINT = "http://dev.qbox.us:9100/oauth2/token";
	// QBox
	public static final int PUT_TIMEOUT = 300000; // 300s = 5m
	public static final String IO_HOST = "http://dev.qbox.us:9200";
	public static final String FS_HOST = "http://dev.qbox.us:9300";
	public static final String RS_HOST = "http://dev.qbox.us:10100";
	// Demo
	public static final String DEMO_DOMAIN = "dev.qbox.us:9200";
}
