package com.qiniu.api.rs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.qiniu.api.net.CallRet;

public class BatchStatRet extends CallRet {

	public List<Entry> results = new ArrayList<Entry>();

	public BatchStatRet(CallRet ret) {
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
			JSONObject jsonObj = arr.getJSONObject(i);
			if (jsonObj.has("code") && jsonObj.has("data")) {
				int code = jsonObj.getInt("code");
				JSONObject body = jsonObj.getJSONObject("data");
				CallRet ret = new CallRet(code, body.toString());
				Entry statRet = new Entry(ret);
				results.add(statRet);
			} else {
				new JSONException("Bad BatchStat result!");
			}
		}
	}
	
}
