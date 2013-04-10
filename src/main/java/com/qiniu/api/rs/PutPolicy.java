package com.qiniu.api.rs;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.EncodeUtils;

/**
 * The PutPolicy class used to generate a upload token. To upload a file, you
 * should obtain upload authorization from Qiniu cloud strage platform. By a
 * pair of valid accesskey and secretkey, we generate a upload token. When
 * upload a file, the upload token is transmissed as a part of the file stream,
 * or as an accessory part of the HTTP Headers.
 */

public class PutPolicy {
	/** Target bucket that the file will be uploaded to. */
	public String scope;

	/**
	 * Optional, used to set the callback address after the file uploaded
	 * successfully to the qiniu server.
	 */
	public String callbackUrl;

	public String callbackBodyType;

	public String custom;

	public String asyncOps;

	public String returnBody;

	/** The period of validity of the uptoken, in seconds. */
	public long expiry;

	public int escape;

	public int detectMime;

	/**
	 * 
	 * @param scope
	 * @param expiry
	 * @throws IllegalArgumentException
	 *             if expiry is non-positive.
	 */
	public PutPolicy(String scope, long expiry) {
		if (expiry <= 0) {
			throw new IllegalArgumentException(
					"expiry can't be negative or zero!");
		}

		this.scope = scope;
		this.expiry = System.currentTimeMillis() / 1000 + expiry;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	private String marshal() throws JSONException {
		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("scope").value(this.scope);
		if (this.callbackUrl != null) {
			stringer.key("callbackUrl").value(this.callbackUrl);
		}
		stringer.key("deadline").value(this.expiry);
		stringer.endObject();

		return stringer.toString();
	}

	private byte[] makeToken() throws AuthException {
		byte[] accessKey = Config.ACCESS_KEY.getBytes();
		byte[] secretKey = Config.SECRET_KEY.getBytes();

		try {
			String policyJson = this.marshal();
			byte[] policyBase64 = EncodeUtils.urlsafeEncodeBytes(policyJson
					.getBytes());

			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
			mac.init(keySpec);

			byte[] digest = mac.doFinal(policyBase64);
			byte[] digestBase64 = EncodeUtils.urlsafeEncodeBytes(digest);
			byte[] token = new byte[accessKey.length + 30 + policyBase64.length];

			System.arraycopy(accessKey, 0, token, 0, accessKey.length);
			token[accessKey.length] = ':';
			System.arraycopy(digestBase64, 0, token, accessKey.length + 1,
					digestBase64.length);
			token[accessKey.length + 29] = ':';
			System.arraycopy(policyBase64, 0, token, accessKey.length + 30,
					policyBase64.length);

			return token;
		} catch (Exception e) {
			throw new AuthException("Fail to get qiniu put policy!", e);
		}
	}

	/**
	 * Makes an upload token.
	 * 
	 * @return an authorized uptoken.
	 * @throws AuthException
	 *             if any exception occurs.
	 */
	public String token() throws AuthException {
		byte[] token = this.makeToken();
		return new String(token);
	}
	
}
