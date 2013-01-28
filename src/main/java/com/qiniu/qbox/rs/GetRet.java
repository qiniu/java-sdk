package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.auth.CallRet;

public class GetRet extends CallRet {
	private String hash;
	private long fsize;
	private String mimeType;
	private String url;
	private long expires ;
	
	public GetRet(CallRet ret) {
		super(ret);
		
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				this.exception = e;
			}
		}
	}
	
	public void unmarshal(String json) throws JSONException {
		
		JSONObject jsonObject = new JSONObject(json);
		
		if (jsonObject.has("hash") && jsonObject.has("fsize") && 
				jsonObject.has("mimeType") && jsonObject.has("url")
				&& jsonObject.has("expires")) {
			this.hash = (String)jsonObject.get("hash");
			Object fsizeObject = jsonObject.get("fsize");
			if (fsizeObject instanceof Long) {
				this.fsize = (Long)fsizeObject;
			} else if (fsizeObject instanceof Integer) {
				this.fsize = new Long((int)(Integer)fsizeObject);
			}
			this.mimeType = (String)jsonObject.get("mimeType");
			this.url = (String)jsonObject.get("url");
			this.expires = jsonObject.getLong("expires") ;
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
	
	public long getExpires() {
		return this.expires ;
	}
}
