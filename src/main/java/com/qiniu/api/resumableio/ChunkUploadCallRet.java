package com.qiniu.api.resumableio;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class ChunkUploadCallRet extends CallRet {
	protected String ctx;
	protected String checksum;
	protected int offset;
	protected String host;
	protected long crc32;
	
	public ChunkUploadCallRet(CallRet ret) {
		super(ret);
		doUnmarshal();
	}

	public ChunkUploadCallRet(int statusCode, String response) {
		super(statusCode, response);
		doUnmarshal();
	}
	
	public ChunkUploadCallRet(int statusCode, Exception e) {
		super(statusCode, e);
	}
	
	private void doUnmarshal() {
		if (this.exception != null || this.response == null
				|| !this.response.trim().startsWith("{")) {
			return;
		}
		try {
			unmarshal();
		} catch (Exception e) {
			e.printStackTrace();
			if (this.exception == null) {
				this.exception = e;
			}
		}

	}

	protected void unmarshal() throws JSONException{
		JSONObject jsonObject = new JSONObject(this.response);
		ctx = jsonObject.optString("ctx", null);
		checksum = jsonObject.optString("checksum", null);
		offset = jsonObject.optInt("offset", 0);
		host = jsonObject.optString("host", null);
		crc32 = jsonObject.optLong("crc32", 0);
	}
	
	public String getCtx() {
		return ctx;
	}

	public String getChecksum() {
		return checksum;
	}

	public long getOffset() {
		return offset;
	}

	public String getHost() {
		return host;
	}

	public long getCrc32() {
		return crc32;
	}

}
