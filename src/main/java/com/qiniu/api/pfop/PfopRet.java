package com.qiniu.api.pfop;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class PfopRet extends CallRet{
	/** persistentId **/
	public String persistentId;
	public PfopRet(CallRet ret) {
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
		if (jsonObject.has("persistentId")) {
			this.persistentId = (String) jsonObject.get("persistentId");
		}
	}
	@Override
	public String toString() {
		return "PfopRet ["
				+ (persistentId != null ? "persistentId=" + persistentId + ", "
						: "") + "statusCode=" + statusCode + ", "
				+ (response != null ? "response=" + response + ", " : "")
				+ (exception != null ? "exception=" + exception : "") + "]";
	}
}
