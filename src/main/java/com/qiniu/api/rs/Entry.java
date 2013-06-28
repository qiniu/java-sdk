package com.qiniu.api.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class Entry extends CallRet {
	/** Etag value of the file */
	private String hash;
	
	/** The size of the file */
	private long fsize;
	
	/** The qiniu server timestamp when file uploaded successfully */
	private long putTime;
	
	/** The mime type of the file */
	private String mimeType;
	
	public Entry(CallRet ret) {
		super(ret);
		
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				this.exception = e;
			}
		}
	}

	private void unmarshal(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		
		this.hash = (String)jsonObject.get("hash");
		Object fsizeObject = jsonObject.get("fsize");
		if (fsizeObject instanceof Long) {
			this.fsize = (Long)fsizeObject;
		} else if (fsizeObject instanceof Integer) {
			this.fsize = new Long((int)(Integer)fsizeObject);
		}
		Object putTimeObject = jsonObject.get("putTime");
		if (putTimeObject instanceof Long) {
			this.putTime = (Long)putTimeObject;
		} else if (putTimeObject instanceof Integer) {
			this.putTime = new Long((int)(Integer)putTimeObject);
		}
		this.mimeType = (String)jsonObject.get("mimeType");
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public long getFsize() {
		return this.fsize;
	}

	public long getPutTime() {
		return this.putTime;
	}

	public String getMimeType() {
		return this.mimeType;
	}
}
