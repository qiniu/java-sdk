package com.qiniu.qbox.fileop;

import java.util.Map;

public class ImageMogrify {
	private Map<String, String> opts ;
	
	public ImageMogrify(Map<String, String> params) {
		this.opts = params ;
	}
	
	public String makeParams() {
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
		return params.toString() ;
	}
	
	public String makeURL(String url) {
		return url + "?imageMogr" + this.makeParams() ;
	}
}
