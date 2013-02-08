package com.qiniu.qbox.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.qbox.Config;

public class GetPolicy {

	public long expiry;
	public String scope;
	
	public GetPolicy(String scope) {
		
		if (scope == null || scope.length() == 0) {
			throw new IllegalArgumentException("scope can't be null or an empty value!");
		}
		this.expiry = System.currentTimeMillis() / 1000 + 3600;
		this.scope = scope;
	}
	
	public GetPolicy(String scope, long expiry) {
		
		if (scope == null || scope.length() == 0) {
			throw new IllegalArgumentException("scope can't be null or an empty value!");
		}
		if (expiry <= 0) {
			throw new IllegalArgumentException("expiry can't be negative or zero!");
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
