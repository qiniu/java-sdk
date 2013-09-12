package com.qiniu.api.fop;


import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.*;
import com.qiniu.api.rs.*;
public class ImageInfo {
	
	/**
	 * Makes a request url that we can get the picture's basic information.
	 * 
	 * @param  url
	 *         The picture's url on the qiniu server
	 *         
	 */
    public static String makeRequest(String url) {
         return url + "?imageInfo";
    }
    
    public static ImageInfoRet call(String url) {
          CallRet ret = new Client().call(makeRequest(url)); 
          return new ImageInfoRet(ret);
    }
    
    public static ImageInfoRet call(String url,Mac mac) throws AuthException {
    	String pubUrl = makeRequest(url);
    	GetPolicy policy =new GetPolicy();
    	String priUrl = policy.makeRequest(pubUrl, mac);
        CallRet ret = new Client().call(priUrl); 
        return new ImageInfoRet(ret);
  }
}
