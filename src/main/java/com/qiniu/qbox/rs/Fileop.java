package com.qiniu.qbox.rs;

import java.util.Map;

public class Fileop {

	public String getImagePreviewURL(String url, int thumbType) {
		return url + "?imagePreview/" + thumbType ;
	}

	public String getImageMogrURL(String url, String params) {
		return url + "?imageMogr/" + params ;
	}

	public String getImageInfoURL(String url) {
		return url + "?imageInfo" ;
	}

	public String getImageExifURL(String url) {
		return url + "?exif" ;
	}


	public String getImage90x90URL(String url) {
		return url += "?imageMogr/auto-orient/thumbnail/!90x90r/gravity/center/crop/90x90" ;
	}

	public String getImageViewURL(String url, Map<String, String> params) {
		return url += "?imageView/" + this.mkImageViewParams(params) ;
	}

	private String mkImageViewParams(Map<String, String> opts) {
		String[] keys = {"w", "h", "q", "format", "sharpen", "watermark"} ;
		String params = "" ;
		String modeVal = opts.get("mode") ;
		params += modeVal ;

		for (String key : keys) {
			String val = opts.get(key) ;
			if (val != null) {
				params += "/" + key + "/" + val ;
			}
		}

		return params ;
	}

	String mkImageMogrifyParams(Map<String, String> opts) {
		String[] keys = {"thumbnail", "gravity", "crop", "quality", "rotate", "format"} ;
		String params = "" ;
		for (String key : keys) {
			String val = opts.get(key) ;
			if (val != null) {
				params += "/" + key + "/" + val ;
			}
		}
		String autoOrient = opts.get("auto_orient") ;
		if (autoOrient != null && autoOrient == "True") {
			params += "/auto-orient" ;
		}
		return "imageMogr" + params ;
	}

	public String getImageMogrifyURL(String srcImgUrl, Map<String, String> opts) {
		return srcImgUrl + "?" + this.mkImageMogrifyParams(opts) ;
	}
}