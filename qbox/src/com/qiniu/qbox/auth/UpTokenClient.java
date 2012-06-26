package com.qiniu.qbox.auth;

import org.apache.http.HttpMessage;

public class UpTokenClient extends Client {

	private String token;
	
	public UpTokenClient(String token) {
		this.token = "UpToken " + token;
	}
	
	@Override
	public void setAuth(HttpMessage httpMessage) {
		httpMessage.setHeader("Authorization", token);
	}

	public String getToken() {
		return token;
	}
}
