package com.qiniu.qbox.fileop;

import java.util.Map;

public class ImageView {
	private Map<String, String> opts  ;
	
	public ImageView(Map<String, String> opts) {
		this.opts = opts ;
	}
	
	public String makeParams() {
		String[] keys = {"w", "h", "q", "format", "sharpen"} ;
		StringBuilder params = new StringBuilder() ;
		String modeVal = opts.get("mode") ;
		if (modeVal != null) {
			params.append("/" + modeVal) ;
		}
		
		for (String key : keys) {
			String val = opts.get(key) ;
			if (val != null) {
				params.append("/" + key + "/" + val) ;
			}
		}

		return params.toString() ;
	}
	
	public String makeURL(String url) {
		return url + "?imageView" + this.makeParams() ;
	}
}
