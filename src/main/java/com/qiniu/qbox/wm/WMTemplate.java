package com.qiniu.qbox.wm;

import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.auth.CallRet;

public class WMTemplate {
    private String font;
    private String fill;
    private String text;
	private String bucket;
    private String dissolve;
    private String gravity;
    private int pointSize;
    private int dx;
    private int dy;
    
    public WMTemplate (String text, int dx, int dy) {
    	this.text = text;
    	this.dx = dx;
    	this.dy = dy;
    }
    
    public String getFont() {
		return font;
	}
	public void setFont(String font) {
		this.font = font;
	}
	public String getFill() {
		return fill;
	}
	public void setFill(String fill) {
		this.fill = fill;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getDissolve() {
		return dissolve;
	}
	public void setDissolve(String dissolve) {
		this.dissolve = dissolve;
	}
	public String getGravity() {
		return gravity;
	}
	public void setGravity(String gravity) {
		this.gravity = gravity;
	}
	public int getPointSize() {
		return pointSize;
	}
	public void setPointSize(int pointSize) {
		this.pointSize = pointSize;
	}
	public int getDx() {
		return dx;
	}
	public void setDx(int dx) {
		this.dx = dx;
	}
	public int getDy() {
		return dy;
	}
	public void setDy(int dy) {
		this.dy = dy;
	}
    public WMTemplate(CallRet ret){
    	if (ret.response != null){
    		try {
    			unmarshal(ret.getResponse());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
	private void unmarshal(String json) throws JSONException{
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(json);
		this.text = (String)jsonObject.get("text");
		this.dx = (Integer)jsonObject.get("dx");
		this.dy = (Integer)jsonObject.get("dy");
		if (jsonObject.has("font")) {
			this.font = (String)jsonObject.get("font");
		}
		if (jsonObject.has("fill")) {
			this.fill = (String)jsonObject.get("fill");
		}
		if (jsonObject.has("bucket")) {
			this.bucket = (String)jsonObject.get("bucket");
		}
		if (jsonObject.has("dissolve")) {
			this.dissolve = (String)jsonObject.get("dissolve");
		}
		if (jsonObject.has("gravity")) {
			this.gravity = (String)jsonObject.get("gravity");
		}
		if (jsonObject.has("pointsize")) {
			this.pointSize = (Integer)jsonObject.get("pointSize");
		}		
	}
}
