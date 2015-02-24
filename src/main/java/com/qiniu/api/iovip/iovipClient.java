package com.qiniu.api.iovip;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.net.EncodeUtils;
import com.qiniu.api.rs.Entry;

public class iovipClient {

	private Client conn;

	public iovipClient(Mac mac) {
		this.conn = new DigestAuthClient(mac);
	}
	

	/**
	 *  fetch  a   file  from  url  and  save  to  a  bucket
	 * 
	 * @param  url
	 *           the  url  to  fetch
	 * @param bucket
	 *            the bucket name  save  to
	 * @param key
	 *            the file's key   save  to
	 * @return {@FetchRet}
	 * 
	 */
	public CallRet fetch(String url,  String bucketDest,
			String keyDest) {
		String entryURIDest = bucketDest + ":" + keyDest;
		return execute("fetch", url, entryURIDest);
	}
	
	private CallRet execute(String cmd, String srcUrl, String entryURIDest) {
		
		String encodedSrc = EncodeUtils.urlsafeEncode(srcUrl);
		String encodedDest = EncodeUtils.urlsafeEncode(entryURIDest);
		String url = Config.IOVIP + "/" + cmd + "/" + encodedSrc + "/to/"
				+ encodedDest;
		CallRet callRet = this.conn.call(url);
		return callRet;
	}

}
