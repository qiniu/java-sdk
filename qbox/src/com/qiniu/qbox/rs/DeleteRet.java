package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.oauth2.CallRet;

public class DeleteRet extends CallRet {

	public DeleteRet(CallRet ret) {
		super(ret.getStatusCode(), ret.getResult());
	}
	
	public DeleteRet(JSONObject jsonObject) throws JSONException {
		super(200, jsonObject.toString());
	}
}
