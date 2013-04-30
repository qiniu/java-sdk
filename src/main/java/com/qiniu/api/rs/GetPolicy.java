package com.qiniu.api.rs;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.EncodeUtils;

/**
 * The GetPolicy class used to generate download token. As a result of we can
 * not download private resource in a anonymous way, we use download token to
 * allow use's to get private resource.
 */

public class GetPolicy {
	/** optional, 3600 seconds, default */
	public long expiry;
	
	/** like domainPattern/keyPattern */
	public String scope;

	public GetPolicy(String scope) {
		if (scope == null || scope.length() == 0) {
			throw new IllegalArgumentException(
					"scope can't be null or an empty value!");
		}
		this.expiry = System.currentTimeMillis() / 1000 + 3600;
		this.scope = scope;
	}

	public GetPolicy(String scope, long expiry) {
		if (scope == null || scope.length() == 0) {
			throw new IllegalArgumentException(
					"scope can't be null or an empty value!");
		}
		if (expiry <= 0) {
			throw new IllegalArgumentException(
					"expiry can't be negative or zero!");
		}

		this.expiry = System.currentTimeMillis() / 1000 + expiry;
		this.scope = scope;
	}

	private String generateSignature() throws JSONException {
		String jsonScope = new JSONStringer().object().key("S")
				.value(this.scope).key("E").value(this.expiry).endObject()
				.toString();

		return EncodeUtils.urlsafeEncode(jsonScope);
	}

	private byte[] makeHmac(String signature) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA1");
		byte[] secretKey = Config.SECRET_KEY.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA1");
		mac.init(secretKeySpec);
		mac.update(signature.getBytes());

		return mac.doFinal();
	}

	/**
	 * Makes a download token.
	 * 
	 * @return
	 * @throws AuthException
	 */
	public String token() throws AuthException {
		String signature = null;
		try {
			signature = generateSignature();
		} catch (JSONException e) {
			throw new AuthException("Fail to make a signature.", e);
		}
		String checksum = null;
		try {
			checksum = EncodeUtils.urlsafeEncodeString(makeHmac(signature));
		} catch (Exception e) {
			throw new AuthException(e);
		}
		return Config.ACCESS_KEY + ":" + checksum + ":" + signature;
	}

}
