package com.qiniu.api.rs;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.DigestAuth;
import com.qiniu.api.auth.digest.Mac;

/**
 * The GetPolicy class used to generate download token. As a result of we can
 * not download private resource in a anonymous way, we use download token to
 * allow use's to get private resource.
 */

public class GetPolicy {
	/** 可选，默认3600秒 */
	public int expires;
	
	public String makeRequest(String baseUrl, Mac mac) throws AuthException {
		if (this.expires == 0) {
			this.expires = 3600;
		}
		int expires_ = (int) (System.currentTimeMillis() / 1000 + this.expires);
		
		if (baseUrl.contains("?")) {
			baseUrl += "&e=";
		} else {
			baseUrl += "?e=";
		}
		baseUrl += expires_;
		
		String downloadToken = DigestAuth.sign(mac, baseUrl.getBytes());
		return baseUrl + "&token=" + downloadToken;
	}
}
