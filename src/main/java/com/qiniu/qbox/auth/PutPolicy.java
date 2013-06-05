package com.qiniu.qbox.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.qbox.Config;

public class PutPolicy {
	
	public String scope;
	public String callbackUrl;
	public String returnUrl;
	public long expiry;

	public int escape;
	public String asyncOps;
	public String returnBody;
	
	public PutPolicy(String scope, long expiry) {
		
		if (expiry <= 0) {
			throw new IllegalArgumentException("expiry can't be negative or zero!");
		}
		
		this.scope = scope;
		this.expiry = System.currentTimeMillis() / 1000 + expiry;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	private String marshal() throws JSONException {

		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("scope").value(this.scope);
		if (this.callbackUrl != null && this.callbackUrl.length() > 0) {
			stringer.key("callbackUrl").value(this.callbackUrl);
		}
		if (this.returnUrl != null && this.returnUrl.length() > 0) {
			stringer.key("returnUrl").value(this.returnUrl);
		}
		if (this.asyncOps != null && this.asyncOps.length() > 0) {
			stringer.key("asyncOps").value(this.asyncOps);
		}
		
		if (this.escape != 0) {
			stringer.key("escape").value(this.escape);
		}
		if (this.returnBody != null && this.returnBody.length() > 0) {
			stringer.key("returnBody").value(this.returnBody);
		}
		
		stringer.key("deadline").value(this.expiry);
		stringer.endObject();

		return stringer.toString();
	}

	private byte[] makeToken() throws Exception {

		byte[] accessKey = Config.ACCESS_KEY.getBytes();
		byte[] secretKey = Config.SECRET_KEY.getBytes();
		
		try {
			String policyJson = this.marshal();
			byte[] policyBase64 = Client.urlsafeEncodeBytes(policyJson.getBytes());

			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
			mac.init(keySpec);

			byte[] digest = mac.doFinal(policyBase64);
			byte[] digestBase64 = Client.urlsafeEncodeBytes(digest);			
			byte[] token = new byte[accessKey.length + 30 + policyBase64.length];

			System.arraycopy(accessKey, 0, token, 0, accessKey.length);
			token[accessKey.length] = ':';
			System.arraycopy(digestBase64, 0, token, accessKey.length + 1, digestBase64.length);
			token[accessKey.length + 29] = ':';
			System.arraycopy(policyBase64, 0, token, accessKey.length + 30, policyBase64.length);

			return token;
		} catch (Exception e) {
			throw new Exception("Fail to get qiniu put policy!", e);
		}
	}

	public String token() throws Exception {
		
		byte[] token = this.makeToken();
		return new String(token);
	}

}
