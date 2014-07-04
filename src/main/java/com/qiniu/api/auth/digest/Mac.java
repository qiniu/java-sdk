package com.qiniu.api.auth.digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.net.EncodeUtils;

public class Mac {
	public String accessKey;
	public String secretKey;
	
	public Mac(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}
	/**
	 * makes a download token.
	 * @param data
	 * @return
	 * @throws AuthException
	 */
	public String sign(byte[] data) throws AuthException {
		javax.crypto.Mac mac = null;
		try {
			mac = javax.crypto.Mac.getInstance("HmacSHA1");
			byte[] secretKey = this.secretKey.getBytes();
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA1");
			mac.init(secretKeySpec);
		} catch (InvalidKeyException e) {
			throw new AuthException("invalid key!", e);
		} catch (NoSuchAlgorithmException e) {
			throw new AuthException("no algorithm called HmacSHA1!", e);
		}
		
		String encodedSign = EncodeUtils.urlsafeEncodeString(mac.doFinal(data));
		return this.accessKey + ":" + encodedSign;
	}
	
	/**
	 * makes a upload token.
	 * @param data
	 * @return
	 * @throws AuthException
	 */
	public String signWithData(byte[] data) throws AuthException {
		byte[] accessKey = this.accessKey.getBytes();
		byte[] secretKey = this.secretKey.getBytes();

		try {
			byte[] policyBase64 = EncodeUtils.urlsafeEncodeBytes(data);

			javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
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

			return new String(token);
		} catch (Exception e) {
			throw new AuthException("Fail to sign with data!", e);
		}
	}
	
	/*
	 * makes an access token.
	 */
	public String signRequest(HttpPost post) throws AuthException {
		URI uri = post.getURI();
		String path = uri.getRawPath();
		String query = uri.getRawQuery();
		HttpEntity entity = post.getEntity();

		byte[] secretKey = this.secretKey.getBytes();
		javax.crypto.Mac mac = null;
		try {
			mac = javax.crypto.Mac.getInstance("HmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new AuthException("No algorithm called HmacSHA1!", e);
		}

		SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
		try {
			mac.init(keySpec);
			mac.update(path.getBytes());
		} catch (InvalidKeyException e) {
			throw new AuthException("You've passed an invalid secret key!", e);
		} catch (IllegalStateException e) {
			throw new AuthException(e);
		}

		if (query != null && query.length() != 0) {
			mac.update((byte) ('?'));
			mac.update(query.getBytes());
		}
		mac.update((byte) '\n');
		if (entity != null) {
			org.apache.http.Header ct = entity.getContentType();
			if (ct != null
					&& "application/x-www-form-urlencoded".equals(ct.getValue())) {
				ByteArrayOutputStream w = new ByteArrayOutputStream();
				try {
					entity.writeTo(w);
				} catch (IOException e) {
					throw new AuthException(e);
				}
				mac.update(w.toByteArray());
			}
		}

		byte[] digest = mac.doFinal();
		byte[] digestBase64 = EncodeUtils.urlsafeEncodeBytes(digest);

		StringBuffer b = new StringBuffer();
		b.append(this.accessKey);
		b.append(':');
		b.append(new String(digestBase64));
		
		return b.toString();
	}
	
}
