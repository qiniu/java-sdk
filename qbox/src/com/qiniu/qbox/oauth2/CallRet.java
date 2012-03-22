package com.qiniu.qbox.oauth2;

public class CallRet {
	private int statusCode;
	private String result;
	
	public CallRet(int statusCode, String result) {
		this.statusCode = statusCode;
		this.result = result;
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}
	
	public String getResult() {
		return this.result;
	}
	
	public boolean ok() {
		return this.statusCode == 200;
	}
}
