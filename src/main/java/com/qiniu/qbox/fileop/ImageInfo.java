package com.qiniu.qbox.fileop;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class ImageInfo {
	
    public static String makeRequest(String url) {
         return url + "?imageInfo";
    }
    
    public static ImageInfoRet call(String url) {
          CallRet ret = new Client().call(makeRequest(url)); 
          return new ImageInfoRet(ret);
    }
}
