package com.qiniu.api.io;

import java.io.File;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.net.EncodeUtils;

public class IoApi {

	private static PutRet put(String uptoken, String key, File file,
			PutExtra extra) {
		if (!file.exists() || !file.canRead()) {
			return new PutRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}
		String entryURI = extra.bucket + ":" + key;
		// set to default mime type.
		String mimeType = "application/octet-stream";
		if (extra.mimeType != null && extra.mimeType.length() != 0) {
			mimeType = extra.mimeType;
		}
		String action = "/rs-put/" + EncodeUtils.urlsafeEncode(entryURI)
				+ "/mimeType/" + EncodeUtils.urlsafeEncode(mimeType);
		if (extra.customMeta != null && extra.customMeta.length() != 0) {
			action += "/meta/" + EncodeUtils.urlsafeEncode(extra.customMeta);
		}

		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("auth", new StringBody(uptoken));
			requestEntity.addPart("action", new StringBody(action));
			FileBody fileBody = new FileBody(file);
			requestEntity.addPart("file", fileBody);
			
			if (extra.callbackParams != null) {
				String callbackParam1 = EncodeUtils
						.encodeParams(extra.callbackParams);
				requestEntity.addPart("params", new StringBody(callbackParam1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new PutRet(new CallRet(400, e));
		}

		String url = Config.UP_HOST + "/upload";
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutRet(ret);
	}

	/**
	 * Uploads a file to the Qiniu cloud server to the specified bucket with the
	 * given key.
	 * 
	 * @param uptoken
	 *            the authorized uptoken
	 * @param bucket
	 *            the target bucket
	 * @param key
	 *            the file's key
	 * @param localFile
	 *            the local file to be uploaded
	 * @param extra
	 *            extra upload options, see {@code PutExtra}
	 * @return
	 */
	public static PutRet putFile(String uptoken, String key, String localFile,
			PutExtra extra) {
		return put(uptoken, key, new File(localFile), extra);
	}

	/**
	 * Makes a download url for the private resource.
	 * 
	 * @param domain
	 * @param key
	 * @param downloadToken
	 * @return
	 */
	public static String getUrl(String domain, String key, String downloadToken) {
		String url = domain + "/" + key;
		if (downloadToken == null || downloadToken == "") {
			return url;
		}
		return url + "?token=" + downloadToken;
	}

}
