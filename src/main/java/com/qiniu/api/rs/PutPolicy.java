package com.qiniu.api.rs;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.DigestAuth;
import com.qiniu.api.auth.digest.Mac;

/**
 * The PutPolicy class used to generate a upload token. To upload a file, you
 * should obtain upload authorization from Qiniu cloud strage platform. By a
 * pair of valid accesskey and secretkey, we generate a upload token. When
 * upload a file, the upload token is transmissed as a part of the file stream,
 * or as an accessory part of the HTTP Headers.
 */

public class PutPolicy {
	/** 必选。可以是 bucketName 或者 bucketName:key */
	public String scope;
	/** 可选 */
	public String callbackUrl;
	/** 可选 */
	public String callbackBody;
	/** 可选 */
	public String returnUrl;
	/** 可选 */
	public String returnBody;
	/** 可选 */
	public String asyncOps;
	/** 可选 */
	public String endUser;
	/** 可选 */
	public long expires;

	public PutPolicy(String scope) {
		this.scope = scope;
	}

	private String marshal() throws JSONException {
		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("scope").value(this.scope);
		if (this.callbackUrl != null && this.callbackUrl.length() > 0) {
			stringer.key("callbackUrl").value(this.callbackUrl);
		}
		if (this.callbackBody != null && this.callbackBody.length() > 0) {
			stringer.key("callbackBody").value(this.callbackBody);
		}
		if (this.returnUrl != null && this.returnUrl.length() > 0) {
			stringer.key("returnUrl").value(this.returnUrl);
		}
		if (this.returnBody != null && this.returnBody.length() > 0) {
			stringer.key("returnBody").value(this.returnBody);
		}
		if (this.asyncOps != null && this.asyncOps.length() > 0) {
			stringer.key("asyncOps").value(this.asyncOps);
		}
		stringer.key("deadline").value(this.expires);
		stringer.endObject();

		return stringer.toString();
	}

	
	/**
	 * makes an upload token.
	 * @param mac
	 * @return
	 * @throws AuthException
	 * @throws JSONException
	 */
	
	public String token(Mac mac) throws AuthException, JSONException {
		if (this.expires == 0) {
			this.expires = 3600; // 3600s, default.
		}
		this.expires = System.currentTimeMillis() / 1000 + expires;
		byte[] data = this.marshal().getBytes();
		return DigestAuth.signWithData(mac, data);
	}

}
