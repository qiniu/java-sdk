package com.qiniu.api.config;

/**
 * The Config class is a global configuration file for the sdk, used for serve
 * side only.
 */
public class Config {
	public static final String CHARSET = "utf-8";
	
	public static String USER_AGENT="qiniu java-sdk v6.1.2";
	
	/**
	 * You can get your accesskey from <a href="https://dev.qiniutek.com"
	 * target="blank"> https://dev.qiniutek.com </a>
	 */
	public static String ACCESS_KEY = "<Please apply your access key>";

	/**
	 * You can get your accesskey from <a href="https://dev.qiniutek.com"
	 * target="blank"> https://dev.qiniutek.com </a>
	 */
	public static String SECRET_KEY = "<Apply your secret key here, and keep it secret!>";

	public static String RS_HOST = "http://rs.qiniu.com";

	public static String UP_HOST = "http://up.qiniu.com";
	
	public static String RSF_HOST = "http://rsf.qiniu.com";

}
