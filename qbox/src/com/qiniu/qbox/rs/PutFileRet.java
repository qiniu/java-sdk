package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.oauth2.CallRet;

public class PutFileRet extends CallRet {

	private String hash;
	
	public PutFileRet(CallRet ret) {
		super(ret.getStatusCode(), ret.getResult());
	}
	
	public PutFileRet(JSONObject jsonObject) throws JSONException {
		super(200, jsonObject.toString());
		
		if (jsonObject.has("hash")) {
			this.hash = (String)jsonObject.get("hash");
		}
	}

	public String getHash() {
		return this.hash;
	}
}
