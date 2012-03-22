package com.qiniu.qbox.rs;

public class RSException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4682085767579517447L;

	public RSException(String message) {
		super(message);
	}
	
	public RSException(String message, Exception e) {
		super(message, e);
	}
}
