package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class ImageInfo {
	private String url;
	private Client conn ;
	
	public ImageInfo(String url, Client conn) {
		this.url = url ;
		this.conn = conn ;
	}
	
    public String makeURL() {
         return this.url + "?imageInfo" ;
    }
    
    public ImageInfoRet call() {
          url = this.makeURL() ;
          CallRet ret = conn.call(url) ; 
          return new ImageInfoRet(ret) ;
    }
}
