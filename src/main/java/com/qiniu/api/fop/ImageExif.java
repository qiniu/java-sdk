package com.qiniu.api.fop;

import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;


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
	
	
}
