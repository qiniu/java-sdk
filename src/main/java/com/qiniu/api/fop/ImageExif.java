package com.qiniu.api.fop;

import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.*;
import com.qiniu.api.rs.*;
public class ImageExif {

	/**
	 * Makes a request url that we can get the image's exif.
	 * 
	 * @param  url 
	 * 		   The picture's download url on the qiniu server
	 *
	 */
	public static String makeRequest(String url) {
		return url + "?exif";
	}

	public static ExifRet call(String url) {
		CallRet ret = new Client().call(makeRequest(url));
		return new ExifRet(ret);
	}
	
	public static ExifRet call(String url,Mac mac) throws AuthException {
		String pubUrl = makeRequest(url);
    	GetPolicy policy =new GetPolicy();
    	String priUrl = policy.makeRequest(pubUrl, mac);
		CallRet ret = new Client().call(priUrl);
		return new ExifRet(ret);
	}
}
