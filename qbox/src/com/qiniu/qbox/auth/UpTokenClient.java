package com.qiniu.qbox.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpMessage;
import org.json.JSONException;

public class UpTokenClient extends Client {

	private String token;
	
	public UpTokenClient(String token) {
		this.token = token;
	}
	
	public UpTokenClient(byte[] key, byte[] secret, AuthPolicy policy) {
		this.token = makeAuthTokenString(key, secret, policy);
		
		System.out.println("Token: " + this.token);
	}
	
	@Override
	public void setAuth(HttpMessage httpMessage) {
		httpMessage.setHeader("Authorization", "UpToken " + token);
	}

	public byte[] makeAuthToken(byte[] key, byte[] secret, AuthPolicy policy) {
		
		try {
			String policyJson = policy.marshal();
			byte[] policyBase64 = urlsafeEncode(policyJson.getBytes());

			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA1");
			mac.init(keySpec);
			byte[] digest = mac.doFinal(policyBase64);
			
			byte[] digestBase64 = urlsafeEncode(digest);
			
			// Make the token string.
			
			byte[] token = new byte[key.length + 30 + policyBase64.length];
			
			System.arraycopy(key, 0, token, 0, key.length);
			token[key.length] = ':';
			System.arraycopy(digestBase64, 0, token, key.length + 1, digestBase64.length);
			//token[key.length + 28] = '=';
			token[key.length + 29] = ':';
			System.arraycopy(policyBase64, 0, token, key.length + 30, policyBase64.length);

			return token;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String makeAuthTokenString(byte[] key, byte[] secret, AuthPolicy policy) {
		byte[] authToken = makeAuthToken(key, secret, policy);
		
		return new String(authToken);
	}
	
	private Base64 encoder = new Base64(false);
	
	public byte[] urlsafeEncode(byte[] src) {
		return Base64.encodeBase64(src, false, false);
	}
}
