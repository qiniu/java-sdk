package com.qiniu.api.net;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.qiniu.api.config.Config;

/**
 * URLEncoding is the alternate base64 encoding defined in RFC 4648. It is
 * typically used in URLs and file names.
 *
 */
public class EncodeUtils {

	public static byte[] urlsafeEncodeBytes(byte[] src) {
		return encodeBase64Ex(src);
	}

	public static byte[] urlsafeBase64Decode(String encoded){
		byte[] rawbs = toByte(encoded);
		for(int i=0;i<rawbs.length;i++){
			if(rawbs[i] == '_'){
				rawbs[i] = '/';
			}else if(rawbs[i] == '-'){
				rawbs[i] = '+';
			}
		}
		return Base64.decodeBase64(rawbs);
	}

	public static String urlsafeEncodeString(byte[] src) {
		return toString(urlsafeEncodeBytes(src));
	}

	public static String urlsafeEncode(String text) {
		return toString(urlsafeEncodeBytes(toByte(text)));
	}

	// replace '/' with '_', '+" with '-'
	private static byte[] encodeBase64Ex(byte[] src) {
		// urlsafe version is not supported in version 1.4 or lower.
		byte[] b64 = Base64.encodeBase64(src);

		for (int i = 0; i < b64.length; i++) {
			if (b64[i] == '/') {
				b64[i] = '_';
			} else if (b64[i] == '+') {
				b64[i] = '-';
			}
		}
		return b64;
	}

	public static String encodeParams(Object params) {
		if (params instanceof String) {
			return (String) params;
		}
		if (params instanceof HashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (HashMap<String, String>) params;
			ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : map.entrySet()) {
				list.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
			return URLEncodedUtils.format(list, Config.CHARSET);
		}
		return null;
	}

	public static byte[] toByte(String s){
		try {
			return s.getBytes(Config.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(byte[] bs){
		try {
			return new String(bs, Config.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
