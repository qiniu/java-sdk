package com.qiniu.qbox.fileop;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.auth.CallRet;

public class ImageInfoRet extends CallRet {
	public String format ;
	/** The width of the original image, in pixel */
	public int width ;
	/** The height of the original image, in pixel */
	public int height ;
	public String colorMode ;

	public ImageInfoRet(CallRet ret) {
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
		JSONObject jsonObj = new JSONObject(json) ;
		if (jsonObj.has("format") && jsonObj.has("width")
				&& jsonObj.has("height") && jsonObj.has("colorModel")) {
			this.format = jsonObj.getString("format") ;
			this.width = jsonObj.getInt("width") ;
			this.height = jsonObj.getInt("height") ;
			this.colorMode = jsonObj.getString("colorModel") ;
		} else {
			throw new JSONException("Bad result!") ;
		}
	}
}