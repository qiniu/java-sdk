package com.code4docs;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.EncodeUtils;
import com.qiniu.api.pfop.PfopClient;
import com.qiniu.api.pfop.PfopResultRet;
import com.qiniu.api.pfop.PfopRet;

public class PfopPersist {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		PfopClient c = new PfopClient(mac);
		
		String key_100 = EncodeUtils
				.urlsafeEncode("<destBucket_100>:<destKey_100>");
		String key_75 = EncodeUtils
				.urlsafeEncode("<destBucket_75>:<destKey_75>");
		String fop_100 = "imageView2/0/w/100|saveas/" + key_100;
		String fop_75 = "imageView2/0/w/75|saveas/" + key_75;
		
		PfopRet ret = c.persist("<srcBucket>", "<srcKey>", fop_100 + ";" + fop_75, null,
				null);
		System.out.println(ret);
		String persistentId = ret.persistentId;
		if (persistentId != null) {
			PfopResultRet result = c.persistResult(persistentId);
			while (result.ok()
					&& (result.code == PfopResultRet.CODE_WAITING || result.code == PfopResultRet.CODE_PROCESSING)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				result = c.persistResult(persistentId);
			}
			System.out.println(result);
		}
	}

}
