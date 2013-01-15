package com.qiniu.qbox.rs;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.auth.CallRet;

public class ImageInfoRet extends CallRet {
	private String format ;
	/** The width of the original image, in pixel */
	private int width ;
	/** The height of the original image, in pixel */
	private int height ;
	private String colorMode ;

	public String getFormat() {
		return format;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public String getColorMode() {
		return colorMode;
	}

	public ImageInfoRet(CallRet ret) {
		super(ret) ;
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (JSONException e) {
				this.exception = e ;
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