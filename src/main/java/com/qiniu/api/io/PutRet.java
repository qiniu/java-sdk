package com.qiniu.api.io;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class PutRet extends CallRet {
	/** Etag of the file */
	private String hash;
	private String key;

	public PutRet(CallRet ret) {
		super(ret);
		if (this.response != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				this.exception = e;
			}
		}
	}

	private void unmarshal(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		if (jsonObject.has("hash")) {
			this.hash = (String) jsonObject.get("hash");
		}
		if (jsonObject.has("key")) {
			this.key = (String) jsonObject.get("key");
		}
	}

	public String getHash() {
		return this.hash;
	}
	
	public String getKey() {
		return this.key;
	}
}
