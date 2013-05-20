package com.qiniu.api.auth;

import org.apache.http.client.methods.HttpPost;

import com.qiniu.api.net.Client;

public class UptokenClient extends Client {

	private String token;

	public UptokenClient(String token) {
		this.token = "UpToken " + token;
	}

	@Override
	public void setAuth(HttpPost post) {
		post.setHeader("Authorization", token);
	}

	public String getToken() {
		return token;
	}
}
