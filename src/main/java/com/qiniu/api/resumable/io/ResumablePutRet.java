package com.qiniu.api.resumable.io;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class ResumablePutRet extends CallRet {
	public String ctx;
	public String checksum;
	public long crc32;
	public String host;
	public int offset;

	public ResumablePutRet(CallRet ret) {
		super(ret);

		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				e.printStackTrace();
				ret.exception = e;
			}
		}
	}

	protected void unmarshal(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);

		this.ctx = jsonObject.getString("ctx");
		this.checksum = jsonObject.getString("checksum");
		this.host = jsonObject.getString("host");
		this.offset = jsonObject.getInt("offset");
		Object crc32Object = jsonObject.get("crc32");
		if (crc32Object instanceof Long) {
			this.crc32 = (long) (Long) crc32Object;
		} else if (crc32Object instanceof Integer) {
			this.crc32 = (long) (int) (Integer) crc32Object;
		}
	}
}