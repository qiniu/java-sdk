package com.qiniu.api.rs;

import com.qiniu.api.url.URLEscape;


public class URLUtils {

	// to do url.escape();
	public static String makeBaseUrl(String domain, String key) {
		return "http://" + domain + "/" + URLEscape.escape(key);
	}
}
