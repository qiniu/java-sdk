package com.qiniu.qbox.oauth2;

public class AuthException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthException(String message) {
		super(message);
	}

	public AuthException(String message, Exception e) {
		super(message, e);
	}

}
