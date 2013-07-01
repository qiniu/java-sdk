package com.qiniu.api.fop;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qiniu.api.net.CallRet;

public class ExifRet extends CallRet {

	public Map<String, ExifValueType> result = new HashMap<String, ExifValueType>();

	public ExifRet(CallRet ret) {
		super(ret);
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				e.printStackTrace();
				this.exception = e;
			}
		}
	}

	private void unmarshal(String response) throws Exception {
		JSONObject json = new JSONObject(response);
		JSONArray names = json.names();

		for (int i = 0; i < names.length(); i++) {
			String key = (String) names.get(i);
			JSONObject val = (JSONObject) json.get(key);
			ExifValueType vp = new ExifValueType();

			if (val.has("val")) {
				String value = val.getString("val");
				vp.value = value;
			}
			if (val.has("type")) {
				int type = val.getInt("type");
				vp.type = type;
			}

			result.put(key, vp);
		}
	}

}
