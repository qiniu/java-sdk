package com.qiniu.api.rsf;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

public class RSFClient {
	public Client conn;
	
	public RSFClient(Mac mac) {
		this.conn = new DigestAuthClient(mac);
	}
	
	public ListPrefixRet listPrifix(String bucketName, String prefix, String marker, int limit) {
		StringBuilder params = new StringBuilder();
		params.append("bucket=").append(bucketName);
		if (marker != null && marker.length() != 0) {
			params.append("&marker=").append(marker);
		}
		if (prefix != null && marker.length() != 0) {
			params.append("&prefix=").append(prefix);
		}
		if (limit > 0) {
			params.append("&limit=").append(limit);
		}
		
		String url = Config.RSF_HOST + "/list?" + params.toString();
		CallRet ret = conn.call(url);
		return new ListPrefixRet(ret);
	}
	
}
