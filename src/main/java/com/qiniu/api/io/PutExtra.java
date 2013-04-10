package com.qiniu.api.io;

public class PutExtra {
	/**
	 * optional, callbackParams can not be null or empty, when callbackurl is
	 * specified with the uptoken
	 */
	public String callbackParams;

	/**
	 * optional, no more than 256Byte
	 */
	public String customMeta;

	/**
	 * optional, the client side can specify its mime type when no detecMime
	 * specified with the uptoken
	 */
	public String mimeType;
	
	/**
	 * the target bucket name
	 */
	public String bucket;
}
