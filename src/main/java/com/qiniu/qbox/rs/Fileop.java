package com.qiniu.qbox.rs;

import java.util.Map;

public class Fileop {

	public static String getImageMogrURL(String url, String params) {
		return url + "?imageMogr/" + params ;
	}

	public static String getImageInfoURL(String url) {
		return url + "?imageInfo" ;
	}

	public static String getImageExifURL(String url) {
		return url + "?exif" ;
	}


	public static String getImageViewURL(String url, Map<String, String> params) throws Exception {
		return url + "?imageView" + mkImageViewParams(params) ;
	}

	private static String mkImageViewParams(Map<String, String> opts) throws Exception {
		String[] keys = {"w", "h", "q", "format", "sharpen", "watermark"} ;
		StringBuilder params = new StringBuilder() ;
		String modeVal = opts.get("mode") ;
		if (modeVal != null) {
			params.append("/" + modeVal) ;
		} else {
			throw new Exception("No mode value specified!") ;
		}

		for (String key : keys) {
			String val = opts.get(key) ;
			if (val != null) {
				params.append("/" + key + "/" + val) ;
			}
		}

		return params.toString() ;
	}

	static String mkImageMogrifyParams(Map<String, String> opts) {
		String[] keys = {"thumbnail", "gravity", "crop", "quality", "rotate", "format"} ;
		StringBuilder params = new StringBuilder() ;
		for (String key : keys) {
			String val = opts.get(key) ;
			if (val != null) {
				params.append("/" + key + "/" + val) ;
			}
		}
		String autoOrient = opts.get("auto_orient") ;
		if (autoOrient != null && autoOrient == "True") {
			params.append("/auto-orient") ;
		}
		return "imageMogr" + params.toString() ;
	}

	public static String getImageMogrifyURL(String srcImgUrl, Map<String, String> opts) {
		return srcImgUrl + "?" + mkImageMogrifyParams(opts) ;
	}
}