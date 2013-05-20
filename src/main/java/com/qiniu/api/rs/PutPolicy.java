package com.qiniu.api.rs;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONStringer;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.EncodeUtils;

/**
 * 类 PutPolicy 用来生成上传凭证(uptoken)。如果要向七牛云存储上传文件，您需要通过一对 accesskey
 * secretkey 从业务服务器获得授权。当上传文件时上传凭证(uptoken)作为 HTTP 请求头信息的一部分进行
 * 传输。
 */

public class PutPolicy {
	
	/**
	 * 一般指文件要上传到的目标存储空间（Bucket）。scope 字段还可以有更灵活的定义：若为”Bucket”，
	 * 表示限定只能传到该Bucket（仅限于新增文件）。若为”Bucket:Key”，表示限定特定的文档，可新增或
	 * 修改文件，必填字段
	 */
	public String scope;

	/**
	 * 定义文件上传完毕后，云存储服务端执行回调的远程URL。注意：若提供该字段，其值必须是公网上可以正
	 * 常进行POST请求并能响应 HTTP Status 200 OK 的有效 URL ，且必须在 multipart/form-data 上
	 * 传流 中指定 params 字段 的值, 可选字段
	 * @see (com.qiniu.resumable.io.PutExtra)
	 * @see (om.qiniu.io.PutExtra)
	 */
	public String callbackUrl;

	/**
	 * 为执行远程回调指定Content-Type，比如可以是：application/x-www-form-urlencoded。可选字段
	 */
	public String callbackBodyType;

	/**
	 * 给上传的文件添加唯一属主标识，特殊场景下非常有用，比如根据终端用户标识给图片打水印。可选字段
	 */
	public String custom;

	/**
	 * 指定文件（图片/音频/视频）上传成功后异步地执行指定的预转操作。每个预转指令是一个API规格字符串，
	 * 多个预转指令可以使用分号“;”隔开。可选字段
	 */
	public String asyncOps;

	/**
	 * 文件上传成功后，自定义从七牛云存储最终返回給终端程序（客户端）的回调参数，允许存在转义符号 $(VarExpression)
	 * <a href="http://docs.qiniutek.com/v3/api/words/#VarExpression">VarExpression</a>.可选字段
	 */
	public String returnBody;

	/**
	 * 定义 uploadToken 的失效时间，Unix时间戳，精确到秒. 必填字段
	 */
	public long expiry;

	/**
	 * 可选值 0 或者 1，缺省为 0。值为 1 表示 callback 传递的自定义数据中允许存在转义符号 $(VarExpression)，参考 
	 * <a href="http://docs.qiniutek.com/v3/api/words/#VarExpression">VarExpression</a>。
	 */
	public int escape;

	/**
	 * 
	 * @param scope 生成 uptoken 的作用域，可以是 bucket 或者 bucket:key 的形式
	 * @param expiry 生成 uptoken 的有效期
	 * @throws IllegalArgumentException
	 *             如果传入的 expiry 为负值
	 */
	public PutPolicy(String scope, long expiry) {
		if (expiry <= 0) {
			throw new IllegalArgumentException(
					"expiry can't be negative or zero!");
		}

		this.scope = scope;
		this.expiry = System.currentTimeMillis() / 1000 + expiry;
	}

	private String marshal() throws JSONException {
		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("scope").value(this.scope);
		if (this.callbackUrl != null) {
			stringer.key("callbackUrl").value(this.callbackUrl);
		}
		stringer.key("deadline").value(this.expiry);
		stringer.endObject();

		return stringer.toString();
	}

	private byte[] makeToken() throws AuthException {
		byte[] accessKey = Config.ACCESS_KEY.getBytes();
		byte[] secretKey = Config.SECRET_KEY.getBytes();

		try {
			String policyJson = this.marshal();
			byte[] policyBase64 = EncodeUtils.urlsafeEncodeBytes(policyJson
					.getBytes());

			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA1");
			mac.init(keySpec);

			byte[] digest = mac.doFinal(policyBase64);
			byte[] digestBase64 = EncodeUtils.urlsafeEncodeBytes(digest);
			byte[] token = new byte[accessKey.length + 30 + policyBase64.length];

			System.arraycopy(accessKey, 0, token, 0, accessKey.length);
			token[accessKey.length] = ':';
			System.arraycopy(digestBase64, 0, token, accessKey.length + 1,
					digestBase64.length);
			token[accessKey.length + 29] = ':';
			System.arraycopy(policyBase64, 0, token, accessKey.length + 30,
					policyBase64.length);

			return token;
		} catch (Exception e) {
			throw new AuthException("Fail to get qiniu put policy!", e);
		}
	}

	/**
	 * 生成 uptoken
	 * 
	 * @return 以字符串表示的 uptoken 值
	 * @throws AuthException
	 *             if any exception occurs.
	 */
	public String token() throws AuthException {
		byte[] token = this.makeToken();
		return new String(token);
	}
	
}
