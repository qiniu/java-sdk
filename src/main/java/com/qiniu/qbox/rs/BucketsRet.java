package com.qiniu.qbox.rs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import com.qiniu.qbox.auth.CallRet;

public class BucketsRet extends CallRet {

	public List<String> items;

	public BucketsRet(CallRet ret) {
		super(ret);
		
		if (ret.ok() && ret.getResponse() != null) {
			try {
				items = new ArrayList<String>();
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				this.exception = e;
			}
		}
	}

	private void unmarshal(String response) throws JSONException {
		
		JSONTokener tokens = new JSONTokener(response);
		JSONArray arr = new JSONArray(tokens);
		for (int i = 0; i < arr.length(); i++) {
			items.add(arr.getString(i));
		}
	}

}
