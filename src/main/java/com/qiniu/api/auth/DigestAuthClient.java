package com.qiniu.api.auth;

import org.apache.http.client.methods.HttpPost;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.net.Client;

public class DigestAuthClient extends Client {
	public Mac mac;
	
	public DigestAuthClient(Mac mac) {
		this.mac = mac;
	}

	@Override
	public void setAuth(HttpPost post) throws AuthException {
		String accessToken = mac.signRequest(post);
		post.setHeader("Authorization", "QBox " + accessToken);
	}

}
