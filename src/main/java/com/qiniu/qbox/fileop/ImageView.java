package com.qiniu.qbox.fileop;

import org.apache.http.client.methods.HttpPost;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;


public class ImageView {
	public int mode ;
	public int width ;
	public int height ;
	public int quality ;
	public String format ;
	public int sharpen ;
	
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
	
	public String makeRequest(String url) {
		return url + "?imageView" + this.makeParams() ;
	}
	
	public CallRet call(String url) {
		CallRet ret = new Client(){
			public void setAuth(HttpPost post) {
				// nothing to do
			}}.call(url) ;
		return ret;
	}
}
