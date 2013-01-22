package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;


public class ImageMogrify {
	public String thumbnail ;
	public String gravity ;
	public String crop ;
	public int quality ;
	public int rotate ;
	public String format ;
	public boolean autoOrient ;
	
	public String makeParams() {
		StringBuilder params = new StringBuilder() ;
		if (this.thumbnail != null && this.thumbnail != "") {
			params.append("/thumbnail/" + this.thumbnail) ;
		}
		if (this.gravity != null && this.gravity != "") {
			params.append("/gravity/" + this.gravity) ;
		}
		if (this.crop != null && this.crop != "") {
			params.append("/crop/" + this.crop) ;
		}
		if (this.quality > 0) {
			params.append("/quality/" + this.quality) ;
		}
		if (this.rotate > 0) {
			params.append("/rotate/" + this.rotate) ;
		}
		if (this.format != null && this.format != "") {
			params.append("/format/" + this.format) ;
		}
		if (this.autoOrient) {
			params.append("/auto-orient") ;
		}
		return params.toString() ;
	}
	
	public String makeRequest(String url) {
		return url + "?imageMogr" + this.makeParams() ;
	}
	
	public CallRet call(String url) {
		CallRet ret = new Client().call(url) ;
		return ret;
	}
}
