package com.qiniu.api.net;

/**
 * The CallRet class represents a general response from qiniu server. 
 * 
 */
public class CallRet {
	/** http status code */
	public int statusCode;

	/** The http response body */
	public String response;

	/** Any exception when dealing with the request */
	public Exception exception;

	public CallRet() {
	}

	/**
	 * Constructs a new CallRet with the specified statusCode and response.
	 * 
	 * @param statusCode
	 *            http status code
	 * @param response
	 *            http reponse body
	 */
	public CallRet(int statusCode, String response) {
		this.statusCode = statusCode;
		this.response = response;
	}

	/**
	 * Construct a new CallRet with the the specifed statusCode and exception.
	 * 
	 * @param statusCode
	 *            the http status code
	 * @param e
	 *            any exception resulting from dealing the use's request.
	 */
	public CallRet(int statusCode, Exception e) {
		this.statusCode = statusCode;
		this.exception = e;
	}

	/**
	 * Construct a new CallRet with the specified ret, behaves like copy
	 * constructor.
	 * 
	 * @param ret
	 */
	public CallRet(CallRet ret) {
		this.statusCode = ret.statusCode;
		this.exception = ret.exception;
		this.response = ret.response;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public String getResponse() {
		return this.response;
	}

	/**
	 * 
	 * 
	 * @return {@code true} if successfully processed the user's request, return
	 *         true. {@code false} otherwise
	 */
	public boolean ok() {
		return this.statusCode / 100 == 2 && this.exception == null;
	}

	public Exception getException() {
		return this.exception;
	}

	@Override
	public String toString() {
		if (this.exception != null) {
			return this.exception.getMessage();
		}
		if (this.response != null) {
			return this.response;
		}

		return String.valueOf(this.statusCode);
	}
}
