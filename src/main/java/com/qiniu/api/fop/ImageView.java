package com.qiniu.api.fop;

import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.rs.GetPolicy;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.*;
import com.qiniu.api.rs.*;

public class ImageView {
	/**
	 * The processing mode of the thumbnail.
	 * <pre>
	 * as follows : 
	 * 
	 * mode=1, limit the width and height of the target thumbnail, amplifying it 
	 * and cutting from the center of thumbnail with the specified width and 
	 * height.
	 * 
	 * mode=2 with specified width and height, indicates that limiting the long side of
	 * the thumbnail, the short side geometric abbreviated adaptive, and the size of the
	 * thumbnail is limited to the speicifid widht and height within the rectangle.
	 *  
	 * mode=2 with specified width but without height, indicates that limiting the width
	 * of the target thumbnail, height geometric abbreviated adaptive.
	 * 
	 * mode=2 with specified height but without width, indicates that limiting the height
	 * of the target thumbnail, width geometric abbreviated adaptive.
	 * </pre>
	 */
	public int mode;
	
	/** The width of the target thumbnail, in pixel. */
	public int width;
	
	/** The height of the target thumbnail, in pixel. */
	public int height;
	
	/** The quality of the target thumbnail, in range 1-100. */
	public int quality;
	
	/** The output format of the target thumbnail, such as jpg, gif, png, etc. */
	public String format;
	
	
	/**
	 * Concatenates the request parameter into a string.
	 * 
	 * @return a string value with all the parameters  
	 */
	public String makeParams() {
		StringBuilder params = new StringBuilder();
		if (this.mode != 1 && this.mode != 2) {
			throw new IllegalArgumentException("Mode value must be 1 or 2!");
		} else {
			params.append("/" + this.mode);
		}
		if (this.width > 0) {
			params.append("/w/" + this.width);
		}
		if (this.height > 0) {
			params.append("/h/" + this.height);
		}
		if (this.quality > 0) {
			params.append("/q/" + this.quality);
		}
		if (this.format != null && this.format != "") {
			params.append("/format/" + this.format);
		}
		
		return params.toString();
	}
	
	/**
	 * Makes a request url for imageview operation.
	 * 
	 * @param  url 
	 * 		   The image's url on qiniu server.
	 */
	public String makeRequest(String url) {
		return url + "?imageView" + this.makeParams();
	}
	
	public CallRet call(String url) {
		CallRet ret = new Client().call(this.makeRequest(url));
		return ret;
	}
	
	public CallRet call(String url,Mac mac) throws AuthException {
		String pubUrl = makeRequest(url);
		GetPolicy policy =new GetPolicy();
		String priUrl = policy.makeRequest(pubUrl, mac);
		CallRet ret = new Client().call(priUrl);
		return ret;
	}
}
