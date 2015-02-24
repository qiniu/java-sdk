package com.qiniu.api.pfop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

/**
 * The <code>PfopClient</code> only used for server side.
 * 
 */
public class PfopClient {
	public Client conn;

	public PfopClient() {
		this(new Mac(Config.ACCESS_KEY, Config.SECRET_KEY));
	}

	public PfopClient(Mac mac) {
		this.conn = new DigestAuthClient(mac);
	}

	/**
	 * Persist the file with the specified key in the bucket
	 * 
	 * @param bucketName
	 *            the bucket name
	 * @param key
	 *            the file's key
	 * @param fops
	 *            the persist operations
	 * @param notifyURL
	 *            when persist process has completed, Qiniu will notify
	 * @param force
	 * @return 
	 */
	public PfopRet persist(String bucketName, String key, String fops,
			String notifyURL, Boolean force) {
		StringBuilder params = new StringBuilder();
		params.append("bucket=").append(bucketName);
		if (key != null && key.length() != 0) {
			try {
				params.append("&key=").append(URLEncoder.encode(key, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (fops != null && fops.length() != 0) {
			try {
				params.append("&fops=")
						.append(URLEncoder.encode(fops, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (notifyURL != null && notifyURL.length() != 0) {
			try {
				params.append("&notifyURL=").append(
						URLEncoder.encode(notifyURL, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (force != null) {
			params.append("&force=").append(force);
		}

		String url = "http://api.qiniu.com/pfop?" + params.toString();
		CallRet ret = conn.call(url);
		return new PfopRet(ret);
	}

	public PfopResultRet persistResult(String persistentId) {
		String url = String.format(
				"http://api.qiniu.com/status/get/prefop?id=%s", persistentId);
		CallRet ret = conn.get(url);
		return new PfopResultRet(ret);
	}
}
