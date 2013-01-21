package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;

public class ImageInfo {
	private String url;
	private DefaultHttpClient conn ;
	
	public ImageInfo(String url) {
		this.url = url ;
		this.conn = new DefaultHttpClient() ;
	}
	
    public String makeURL() {
         return this.url + "?imageInfo" ;
    }
    
    public ImageInfoRet call() {
          String url = this.makeURL() ;
          CallRet ret = conn.call(url) ; 
          return new ImageInfoRet(ret) ;
    }
}
