package com.qiniu.api.rs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.qiniu.api.net.CallRet;

public class BatchCallRet extends CallRet {

	public List<CallRet> results = new ArrayList<CallRet>(); 
	
	public BatchCallRet(CallRet ret) {
		super(ret);
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				e.printStackTrace();
				this.exception = e;
			}
		}
	}
	
	@Override
	public boolean ok() {
		// partial ok
		if (this.statusCode == 298) {
			return false;
		}
		return super.ok();
	}
	
	private void unmarshal(String response) throws JSONException {
		JSONTokener tokens = new JSONTokener(response);
		JSONArray arr = new JSONArray(tokens);
		
		for (int i = 0; i < arr.length(); i++) {
			CallRet ret = new CallRet();
			JSONObject jsonObj = arr.getJSONObject(i);
			if (jsonObj.has("code")) {
				int code = jsonObj.getInt("code");
				ret.statusCode = code;
			}
			if (jsonObj.has("data")) {
				JSONObject body = jsonObj.getJSONObject("data");
				ret.response = body.toString();
			}
			results.add(ret);
		}
	}
	
}
