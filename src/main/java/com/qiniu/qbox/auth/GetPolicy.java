package com.qiniu.qbox.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.qbox.Config;

public class GetPolicy {

	public long expiry;
	public String scope;

	public GetPolicy(long expiry, String scope) throws Exception {
		
		if (expiry <= 0) {
			expiry = 3600; // set to default value, an hour
		}
		if (scope == null || scope.trim().length() == 0) {
			scope = "*/*"; // set to default.
		}
		
		this.expiry = System.currentTimeMillis() / 1000 + expiry;
		this.scope = scope;
	}

	private String generateSignature() throws JSONException {
		
		String jsonScope = new JSONStringer().object().key("S")
				.value(this.scope).key("E").value(this.expiry).endObject()
				.toString();
		
		return  Client.urlsafeEncode(jsonScope);
	}

	private byte[] makeHmac(String signature) throws Exception {

		Mac mac = Mac.getInstance("HmacSHA1");
		byte[] secretKey = Config.SECRET_KEY.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA1");
		mac.init(secretKeySpec);
		mac.update(signature.getBytes());

		return mac.doFinal();
	}

	public String token() throws Exception {

		String signature = generateSignature();
		String checksum = Client.urlsafeEncodeString(makeHmac(signature));
		return Config.ACCESS_KEY + ":" + checksum + ":" + signature;
	}

}
