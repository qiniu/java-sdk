package com.qiniu.api.rsf;

import org.json.JSONException;
import org.json.JSONObject;

public class ListItem {
	public String key;
	public String hash;
	public long fsize;
	public long putTime;
	public String mimeType;
	public String endUser;
	
	public ListItem() {
		
	}
	
	public ListItem(JSONObject obj) throws JSONException {
		this.unmarshal(obj);
	}

	private void unmarshal(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("key")) {
			this.key = jsonObject.getString("key");
		}
		if (jsonObject.has("hash")) {
			this.hash = jsonObject.getString("hash");
		}
		if (jsonObject.has("fsize")) {
			this.fsize = jsonObject.getLong("fsize");
		}
		if (jsonObject.has("putTime")) {
			this.putTime = jsonObject.getLong("putTime");
		}
		if (jsonObject.has("mimeType")) {
			this.mimeType = jsonObject.getString("mimeType");
		}
		if (jsonObject.has("endUser")) {
			this.endUser = jsonObject.getString("endUser");
		}
	}
}
