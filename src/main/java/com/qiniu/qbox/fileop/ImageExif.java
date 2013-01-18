package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class ImageExif {
	private String url;
	private Client conn;

	public ImageExif(String url, Client conn) {
		this.url = url;
		this.conn = conn;
	}

	public String makeURL() {
		return this.url + "?exif";
	}

	public CallRet call() {
		url = this.makeURL();
		CallRet ret = conn.call(url);
		return ret;
	}
}
