package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.oauth2.CallRet;

public class GetRet extends CallRet {
	private String hash;
	private long fsize;
	private String mimeType;
	private String url;

	public GetRet(CallRet ret) {
		super(ret.getStatusCode(), ret.getResult());
	}
	
	public GetRet(JSONObject jsonObject) throws JSONException {
		
		super(200, jsonObject.toString());
		
		if (jsonObject.has("hash") && jsonObject.has("fsize") && 
				jsonObject.has("mimeType") && jsonObject.has("url")) {
			this.hash = (String)jsonObject.get("hash");
			Object fsizeObject = jsonObject.get("fsize");
			if (fsizeObject instanceof Long) {
				this.fsize = (Long)fsizeObject;
			} else if (fsizeObject instanceof Integer) {
				this.fsize = new Long((int)(Integer)fsizeObject);
			}
			this.mimeType = (String)jsonObject.get("mimeType");
			this.url = (String)jsonObject.get("url");
		}
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public long getFsize() {
		return this.fsize;
	}

	public String getUrl() {
		return this.url;
	}

	public String getMimeType() {
		return this.mimeType;
	}
}
