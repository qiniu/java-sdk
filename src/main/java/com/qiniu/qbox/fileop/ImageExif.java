package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;

public class ImageExif {
	private String url;
	private DefaultHttpClient conn  ;

	public ImageExif(String url) {
		this.url = url;
		this.conn = new DefaultHttpClient();
	}

	public String makeURL() {
		return this.url + "?exif";
	}

	public CallRet call() {
		String url = this.makeURL();
		CallRet ret = conn.call(url);
		return ret;
	}
}
