package com.qiniu.qbox.rs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

import com.qiniu.qbox.oauth2.CallRet;

public class RSClient {

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
			return new PutFileRet(new CallRet(400, "File does not exist or not readable."));
		}

		BASE64Encoder encoder = new BASE64Encoder();
		
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}

		String entryURI = tblName + ":" + key;
		String action = "/rs-put/" + encoder.encode(entryURI.getBytes()) + "/mimeType/" + encoder.encode(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			action += "/meta/" + encoder.encode(customMeta.getBytes());
		}
		
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("action", new StringBody(action));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
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

		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		HttpPost postMethod = new HttpPost(url);
		postMethod.setEntity(requestEntity);
		
		String responseText = "";
		
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(postMethod);
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				responseText = EntityUtils.toString(responseEntity);
				return new PutFileRet(new JSONObject(responseText));
			}
		} catch (JSONException e) {
			throw new RSException("PutFile returned invalid response format " + responseText);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}
}
