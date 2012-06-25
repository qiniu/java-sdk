package com.qiniu.qbox.rs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import sun.misc.BASE64Encoder;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.ResumablePutRet;
import com.qiniu.qbox.up.UpClient;

public class RSClient {
	
	private static Base64 encoder = new Base64(true);

	/**
	 * func PutFile(url, key, mimeType, localFile, customMeta, callbackParams string) => (data PutRet, code int, err Error)
	 * 匿名上传一个文件(上传用的临时 url 通过 $rs->PutAuth 得到)
	 * @throws IOException 
	 * @throws RSException 
	 * @throws JSONException 
	 */
	public static PutFileRet putFile(String url, String tblName, String key, String mimeType, String localFile, String customMeta, HashMap<String, String> callbackParams) throws RSException {
		
		File file = new File(localFile);
		if (!file.exists() || !file.canRead()) {
			return new PutFileRet(new CallRet(400, new Exception("File does not exist or not readable.")));
		}

		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}

		String entryURI = tblName + ":" + key;
		String action = "/rs-put/" + encoder.encodeBase64(entryURI.getBytes()) + 
				"/mimeType/" + encoder.encodeBase64(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			action += "/meta/" + encoder.encodeBase64(customMeta.getBytes());
		}
		
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("action", new StringBody(action));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		if (callbackParams != null && !callbackParams.isEmpty()) {
			ArrayList<NameValuePair> callbackParamList = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : callbackParams.entrySet()) {
				callbackParamList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			try {
				requestEntity.addPart("params", new StringBody(URLEncodedUtils.format(callbackParamList, "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		HttpPost postMethod = new HttpPost(url);
		postMethod.setEntity(requestEntity);
		
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new PutFileRet(new CallRet(400, e));
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
	
	public static PutFileRet resumablePutFile(UpClient c, String[] checksums, BlockProgress[] progresses, 
			ProgressNotifier progressNotifier, BlockProgressNotifier blockProgressNotifier,
			String entryUri, String mimeType, RandomAccessFile f, long fsize, String customMeta, String callbackParams) {
		
		ResumablePutRet ret = c.resumablePut(f, fsize, checksums, progresses, progressNotifier, blockProgressNotifier);
		if (!ret.ok()) {
			return new PutFileRet(ret);
		}
		
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}
		
		String params = "/mimeType/" + encoder.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			params += "/meta/" + encoder.encodeBase64String(customMeta.getBytes());
		}
		
		PutFileRet putFileRet = c.makeFile("/rs-mkfile/", entryUri, fsize, params, callbackParams, checksums);
		
		return putFileRet;
	}

	private static PutFileRet handleResult(HttpResponse response) {
		
		if (response == null || response.getStatusLine() == null) {
			return new PutFileRet(new CallRet(400, "No response"));
		}
		
		try {
			String responseBody = EntityUtils.toString(response.getEntity());
			
			StatusLine status = response.getStatusLine();
			int statusCode = (status == null) ? 400 : status.getStatusCode();
			
			return new PutFileRet(new CallRet(statusCode, responseBody));
		} catch (Exception e) {
			e.printStackTrace();
			return new PutFileRet(new CallRet(400, e));
		}
	}
}
