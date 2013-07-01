package com.qiniu.api.rs;

import org.apache.commons.codec.EncoderException;

import com.qiniu.api.url.URLEscape;



public class URLUtils {

	// to do url.escape();
	public static String makeBaseUrl(String domain, String key) throws EncoderException {
		return "http://" + domain + "/" + URLEscape.escape(key);
	}
}
