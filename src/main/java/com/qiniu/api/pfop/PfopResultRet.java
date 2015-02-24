package com.qiniu.api.pfop;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class PfopResultRet extends CallRet{
	public static int CODE_SUCCESS=0;
	public static int CODE_WAITING=1;
	public static int CODE_PROCESSING=2;
	public static int CODE_FAIL=3;
	public static int CODE_CALLBACK_FAIL=4;
	/** persistentId **/
	public String id;
	public int code;
	public String desc;
	public List<PfopResultItem> items;
	public PfopResultRet(CallRet ret) {
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
		if (jsonObject.has("id")) {
			this.id = (String) jsonObject.get("id");
		}
		if (jsonObject.has("code")) {
			this.code = (Integer) jsonObject.get("code");
		}
		if (jsonObject.has("desc")) {
			this.desc = (String) jsonObject.get("desc");
		}
		if (jsonObject.has("items")) {
			JSONArray arr= jsonObject.getJSONArray("items");
			items =new ArrayList<PfopResultItem>();
			for(int i=0;i<arr.length();i++){
				items.add(new PfopResultItem(arr.optJSONObject(i)));
			}
		}
	}
	@Override
	public String toString() {
		return "PfopResultRet [id=" + id + ", code=" + code + ", desc=" + desc
				+ ", items=" + items + "]";
	}
}
