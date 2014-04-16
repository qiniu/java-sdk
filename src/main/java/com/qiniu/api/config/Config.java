package com.qiniu.api.config;

/**
 * The Config class is a global configuration file for the sdk, used for serve
 * side only.
 */
public class Config {
	
	public static String USER_AGENT="qiniu java-sdk v6.0.0";
	
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

	public static String RS_HOST = "http://rs.qbox.me";

	public static String UP_HOST = "http://up.qbox.me";
	
	public static String RSF_HOST = "http://rsf.qbox.me";
	
	/**
	 * HTTP连接超时的时间毫秒(ms)
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     * 
     * Please note this parameter can only be applied to connections that
     * are bound to a particular local address.
     */
	public static int CONNECTION_TIMEOUT = 30 * 1000;
	/**
	 * 读取response超时的时间毫秒(ms)
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * A timeout value of zero is interpreted as an infinite timeout.
     * @see java.net.SocketOptions#SO_TIMEOUT
     */
	public static int SO_TIMEOUT = 30 * 1000;

}
