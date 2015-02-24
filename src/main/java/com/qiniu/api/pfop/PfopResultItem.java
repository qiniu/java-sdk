package com.qiniu.api.pfop;

import org.json.JSONException;
import org.json.JSONObject;

public class PfopResultItem {
	/** result code of the process */
	public int code;
	/** Etag of the process */
	public String hash;
	/** commands of the process */
	public String cmd;
	/** the key of the file  */
	public String key;
	/** the key of the file  */
	public String error;
	public String desc;
	public PfopResultItem(JSONObject json) throws JSONException{
		if(json!=null){
			unmarshal(json);
		}
	}
	private void unmarshal(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("code")) {
			this.code = (Integer) jsonObject.get("code");
		}
		if (jsonObject.has("hash")) {
			this.hash = (String) jsonObject.get("hash");
		}
		if (jsonObject.has("cmd")) {
			this.cmd = (String) jsonObject.get("cmd");
		}
		if (jsonObject.has("key")) {
			this.key = (String) jsonObject.get("key");
		}
		if (jsonObject.has("error")) {
			this.error = (String) jsonObject.get("error");
		}
		if (jsonObject.has("desc")) {
			this.desc = (String) jsonObject.get("desc");
		}
	}
	@Override
	public String toString() {
		return "PfopResultItem [code=" + code + ", hash=" + hash + ", cmd="
				+ cmd + ", key=" + key + ", error=" + error + ", desc=" + desc
				+ "]";
	}
}
