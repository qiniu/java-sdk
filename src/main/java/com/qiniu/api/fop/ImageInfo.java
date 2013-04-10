package com.qiniu.api.fop;

import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

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
}
