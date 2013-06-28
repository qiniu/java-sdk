package com.qiniu.api.rsf;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class ListPrefixRet extends CallRet {
	public String marker;
	public List<ListItem> results = new ArrayList<ListItem>();
	
	public ListPrefixRet(CallRet ret) {
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
	
	private void unmarshal(String response) throws JSONException {
		JSONObject obj = new JSONObject(response);
		if (obj.has("marker")) {
			this.marker = obj.getString("marker");
		}
		JSONArray arr = obj.getJSONArray("items");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject jsonObj = arr.getJSONObject(i);
			ListItem ret = new ListItem(jsonObj);
			results.add(ret);
		}
	}
	
}
