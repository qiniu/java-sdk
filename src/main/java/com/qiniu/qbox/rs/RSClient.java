package com.qiniu.qbox.rs;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.ResumablePutRet;
import com.qiniu.qbox.up.UpService;

public class RSClient {
	
	public static PutFileRet putFileWithToken(String upToken, String bucketName,
			String key, String localFile, String mimeType, String customMeta,
			Object callbackParam, String rotate) throws Exception {

		File file = new File(localFile);
		if (!file.exists() || !file.canRead()) {
			return new PutFileRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}

		if (mimeType == null || mimeType.length() == 0) {
			mimeType = "application/octet-stream";
		}

		String entryURI = bucketName + ":" + key;
		String action = "/rs-put/" + Client.urlsafeEncode(entryURI)
				+ "/mimeType/" + Client.urlsafeEncode(mimeType);

		if (customMeta != null && customMeta.length() != 0) {
			action += "/meta/" + Client.urlsafeEncode(customMeta);
		}

		if (rotate != null && rotate.length() != 0) {
			action += "/rotate/" + rotate;
		}

		MultipartEntity requestEntity = new MultipartEntity();
		requestEntity.addPart("auth", new StringBody(upToken));
		requestEntity.addPart("action", new StringBody(action));
		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		if (callbackParam != null) {
			String callbackParam1 = Client.encodeParams(callbackParam);
			if (callbackParam1 != null) {
				requestEntity.addPart("params", new StringBody(callbackParam1));
			}
		}
		String url = Config.UP_HOST + "/upload";
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutFileRet(ret);
	}

	/**
	 * func PutFile(url, bucketName, key, mimeType, localFile, customMeta, callbackParams string)
	 * 匿名上传一个文件(上传用的临时 url 通过 $rs->PutAuth 得到)
	 * @throws Exception 
	 */
	@Deprecated
	public static PutFileRet putFile(
		String url, String bucketName, String key, String mimeType, String localFile,
		String customMeta, Object callbackParams1) throws Exception {

		File file = new File(localFile);
		if (!file.exists() || !file.canRead()) {
			return new PutFileRet(new CallRet(400, new Exception("File does not exist or not readable.")));
		}

		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}

		String entryURI = bucketName + ":" + key;
		String action = "/rs-put/" + Base64.encodeBase64String(entryURI.getBytes()) + 
				"/mimeType/" + Base64.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			action += "/meta/" + Base64.encodeBase64String(customMeta.getBytes());
		}

		MultipartEntity requestEntity = new MultipartEntity();
		requestEntity.addPart("action", new StringBody(action));

		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		if (callbackParams1 != null) {
			String callbackParams = Client.encodeParams(callbackParams1);
			if (callbackParams != null) {
				requestEntity.addPart("params", new StringBody(callbackParams));
			}
		}
		
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutFileRet(ret);
	}


	public static PutFileRet resumablePutFile(
			UpService c, String[] checksums, BlockProgress[] progresses, 
			ProgressNotifier progressNotifier, BlockProgressNotifier blockProgressNotifier,
			String bucketName, String key, String mimeType,
			RandomAccessFile f, long fsize, String customMeta, String callbackParams) {
		
		ResumablePutRet ret = c.resumablePut(f, fsize, checksums, progresses, progressNotifier, blockProgressNotifier);
		if (!ret.ok()) {
			return new PutFileRet(ret);
		}
		
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}
		
		String params = "/mimeType/" + Base64.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			params += "/meta/" + Base64.encodeBase64String(customMeta.getBytes());
		}

		String entryUri = bucketName + ":" + key;
		String upHost = ret.getHost() ;
		CallRet callRet = c.makeFile(upHost, "/rs-mkfile/", entryUri, fsize, params, callbackParams, checksums);
		
		return new PutFileRet(callRet);
	}
}
