package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.oauth2.CallRet;

public class PublishRet extends CallRet {

	public PublishRet(CallRet ret) {
		super(ret.getStatusCode(), ret.getResult());
	}
	
	public PublishRet(JSONObject jsonObject) throws JSONException {
		super(200, jsonObject.toString());
	}
}
