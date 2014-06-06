package com.qiniu.api.rs;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.DigestAuth;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.net.EncodeUtils;

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
	/** 可选 */
	public String saveKey;
	/** 可选。 若非0, 即使Scope为 Bucket:Key 的形式也是insert only*/
	public int insertOnly;
	/** 可选。若非0, 则服务端根据内容自动确定 MimeType */
	public int detectMime;
	/** 可选 */
	public long fsizeLimit;
	/**限定用户上传的文件类型
 	 * 可选
 	 * */
	public String mimeLimit;
	/** 可选 */
	public String persistentNotifyUrl;
	/** 可选 */
	public String persistentOps;

	private long deadline;
	
	/**
	 * 转码队列名，须预先开通
	 * 资源上传成功后，触发转码时指定独立的队列进行转码
	 */
	public String persistentPipeline;


	public PutPolicy(String scope) {
		this.scope = scope;
	}

	public String marshal() throws JSONException {
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
		if (this.saveKey != null && this.saveKey.length() > 0) {
			stringer.key("saveKey").value(this.saveKey);
		}
		if(this.insertOnly>0){
			stringer.key("insertOnly").value(this.insertOnly);
		}
		if(this.detectMime>0){
			stringer.key("detectMime").value(this.detectMime);
		}
		if(this.fsizeLimit>0){
			stringer.key("fsizeLimit").value(this.fsizeLimit);
		}
		if(this.mimeLimit != null && this.mimeLimit.length() > 0){
			stringer.key("mimeLimit").value(this.mimeLimit);
		}
		if (this.endUser != null && this.endUser.length() > 0) {
			stringer.key("endUser").value(this.endUser);
		}
		if (this.persistentNotifyUrl != null && this.persistentNotifyUrl.length() > 0) {
			stringer.key("persistentNotifyUrl").value(this.persistentNotifyUrl);
		}
		if (this.persistentOps != null && this.persistentOps.length() > 0) {
			stringer.key("persistentOps").value(this.persistentOps);
		}
		if(persistentPipeline != null && persistentPipeline.trim().length()  > 0){
			stringer.key("persistentPipeline").value(this.persistentPipeline);
		}
		
		stringer.key("deadline").value(this.deadline);
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
		this.deadline = System.currentTimeMillis() / 1000 + this.expires;
		byte[] data = EncodeUtils.toByte(this.marshal());
		return DigestAuth.signWithData(mac, data);
	}

}
