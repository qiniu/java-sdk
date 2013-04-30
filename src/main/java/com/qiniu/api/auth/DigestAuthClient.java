package com.qiniu.api.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.Client;
import com.qiniu.api.net.EncodeUtils;

public class DigestAuthClient extends Client {

	@Override
	public void setAuth(HttpPost post) throws AuthException {
		URI uri = post.getURI();
		String path = uri.getRawPath();
		String query = uri.getRawQuery();
		HttpEntity entity = post.getEntity();

		byte[] secretKey = Config.SECRET_KEY.getBytes();
		Mac mac = null;
		try {
			mac = Mac.getInstance("HmacSHA1");
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
					&& ct.getValue() == "application/x-www-form-urlencoded") {
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
		b.append("QBox ");
		b.append(Config.ACCESS_KEY);
		b.append(':');
		b.append(new String(digestBase64));
		post.setHeader("Authorization", b.toString());
	}

}
