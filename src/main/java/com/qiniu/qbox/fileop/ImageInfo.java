package com.qiniu.qbox.fileop;

import org.apache.http.client.methods.HttpPost;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class ImageInfo {
	
    public String makeRequest(String url) {
         return url + "?imageInfo" ;
    }
    
    public ImageInfoRet call(String url) {
          CallRet ret = new Client(){
			public void setAuth(HttpPost post) {
				// nothing to do
			}}.call(url) ; 
          return new ImageInfoRet(ret) ;
    }
}
