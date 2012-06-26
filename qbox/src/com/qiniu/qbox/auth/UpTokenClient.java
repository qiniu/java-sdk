package com.qiniu.qbox.auth;

import org.apache.http.HttpMessage;

public class UpTokenClient extends Client {

	private String token;
	
	public UpTokenClient(String token) {
		this.token = token;
	}
	
	@Override
	public void setAuth(HttpMessage httpMessage) {
		httpMessage.setHeader("Authorization", "UpToken " + token);
	}
}
