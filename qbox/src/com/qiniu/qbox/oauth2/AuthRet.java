package com.qiniu.qbox.oauth2;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthRet {
	private String accessToken;
	private String refreshToken;
	
	public AuthRet(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("access_token")) {
			this.accessToken = (String) jsonObject.get("access_token");
		}
		if (jsonObject.has("refresh_token")) {
			this.refreshToken = (String) jsonObject.get("refresh_token");
		}
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
