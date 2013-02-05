package com.qiniu.qbox.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.qbox.Config;

public final class DownloadPolicy {

	private long deadLine;
	private String pattern;

	public DownloadPolicy(long deadLine, String pattern) {
		
		if (deadLine <= 0) {
			deadLine = 3600; // set to default value, an hour
		}
		if (pattern == null || pattern.trim().length() == 0) {
			pattern = "*/*"; // set to default.
		}
		this.deadLine = deadLine;
		this.pattern = pattern;
	}

	private String makeScope() throws JSONException {
		
		long expires = System.currentTimeMillis() / 1000 + this.deadLine;
		String jsonScope = new JSONStringer().object().key("S")
				.value(this.pattern).key("E").value(expires).endObject()
				.toString();

		return Client.urlsafeEncode(jsonScope);
	}

	private byte[] makeHmac(String scope) throws Exception {

		Mac mac = Mac.getInstance("HmacSHA1");
		byte[] secretKey = Config.SECRET_KEY.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA1");
		mac.init(secretKeySpec);
		mac.update(scope.getBytes());

		return mac.doFinal();
	}

	private String checksum(byte[] hmac) throws Exception {

		return Client.urlsafeEncodeString(hmac);
	}

	public String makeDownloadToken() throws Exception {

		String scope = makeScope();
		String checksum = checksum(makeHmac(scope));

		return Config.ACCESS_KEY + ":" + checksum + ":" + scope;
	}

}
