package com.qiniu.api.rs;



public class URLUtils {

	// to do url.escape();
	public static String makeBaseUrl(String domain, String key) {
		return "http://" + domain + "/" + key;
	}
}
