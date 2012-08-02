package com.qiniu.qbox;

public class Config {

	//public static String ACCESS_KEY	= "<Please apply your access key>";
	//public static String SECRET_KEY	= "<Dont change here>";
	
	public static String ACCESS_KEY	= "bE21M6FW9V7zAFrBY5psgKOKJQLiBj12qMWTpc57";
	public static String SECRET_KEY	= "uMo7Nyq_eDK_CuQ8_FYCxoTHQZqjiaPh-cbiKO7L";

	public static String REDIRECT_URI  = "<RedirectURL>";
	public static String AUTHORIZATION_ENDPOINT = "<AuthURL>";
	public static String TOKEN_ENDPOINT = "https://acc.qbox.me/oauth2/token";
/*
	public static String IO_HOST = "http://iovip.qbox.me";
	public static String FS_HOST = "https://fs.qbox.me";
	public static String RS_HOST = "http://rs.qbox.me:10100";
	public static String UP_HOST = "http://up.qbox.me";*/
	
	public static String IO_HOST = "http://m1.qbox.me:13004";
	public static String FS_HOST = "http://m1.qbox.me:13002";
	public static String RS_HOST = "http://m1.qbox.me:13003";
	public static String UP_HOST = "http://m1.qbox.me:13019";
	public static String WM_HOST = "http://m1.qbox.me:15000";
	public static String PU_HOST = "http://m1.qbox.me:13012";

	public static int BLOCK_SIZE = 1024 * 1024 * 4;
	public static int PUT_CHUNK_SIZE = 1024 * 256;
	public static int PUT_RETRY_TIMES = 3;
	public static int PUT_TIMEOUT = 300000; // 300s = 5m
}