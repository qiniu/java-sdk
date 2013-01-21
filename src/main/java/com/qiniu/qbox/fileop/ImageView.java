package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;


public class ImageView {
	public int mode ;
	public int width ;
	public int height ;
	public int quality ;
	public String format ;
	public int sharpen ;
	private String url ;
	private DefaultHttpClient conn;
	
	public ImageView(String url) {
		this.url = url ;
		this.conn = new DefaultHttpClient() ;
	}
	
	public String makeParams() {
		StringBuilder params = new StringBuilder() ;
		if (this.mode != 1 && this.mode != 2) {
			throw new IllegalArgumentException("Mode value must be 1 or 2!") ;
		} else {
			params.append("/" + this.mode) ;
		}
		if (this.width > 0) {
			params.append("/w/" + this.width) ;
		}
		if (this.height > 0) {
			params.append("/h/" + this.height) ;
		}
		if (this.quality > 0) {
			params.append("/q/" + this.quality) ;
		}
		if (this.format != null && this.format != "") {
			params.append("/format/" + this.format) ;
		}
		if (this.sharpen > 0) {
			params.append("/sharpen/" + this.sharpen) ;
		}
		return params.toString() ;
	}
	
	public String makeURL() {
		return this.url + "?imageView" + this.makeParams() ;
	}
	
	public CallRet call() {
		String url = this.makeURL();
		CallRet ret = conn.call(url);
		return ret;
	}
}
