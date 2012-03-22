package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.oauth2.CallRet;

public class PutAuthRet extends CallRet {
	private int expires;
	private String url;
	
	public PutAuthRet(CallRet ret) {
		super(ret.getStatusCode(), ret.getResult());
	}
	
	public PutAuthRet(JSONObject jsonObject) throws JSONException {
		super(200, jsonObject.toString());
		
		if (jsonObject.has("expiresIn")) {
			this.expires = (Integer)jsonObject.get("expiresIn");
		}
		if (jsonObject.has("url")) {
			this.url = (String)jsonObject.get("url");
		}
	}
	
	public int getExpiresIn() {
		return this.expires;
	}
	
	public String getUrl() {
		return this.url;
	}
}
