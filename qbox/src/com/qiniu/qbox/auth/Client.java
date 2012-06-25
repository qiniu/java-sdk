package com.qiniu.qbox.auth;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public abstract class Client {
	
	public abstract void setAuth(HttpMessage httpMessage);

	public CallRet call(String url, int retries, int maxRetries) {
		HttpPost postMethod = new HttpPost(url);
		setAuth(postMethod);
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(postMethod);
			
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
	
	public CallRet call(String url, List<NameValuePair> nvps) {
		HttpPost postMethod = new HttpPost(url);
		postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		setAuth(postMethod);
		
		HttpClient client = new DefaultHttpClient();
		try {
			postMethod.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

			HttpResponse response = client.execute(postMethod);
			
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
	
	public CallRet callWithFile(String url, String action, String localFile, HashMap<String, String> params) {
		File file = new File(localFile);
		if (!file.exists() || !file.canRead()) {
			return new CallRet(404, new Exception("File does not exist or not readable."));
		}

		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("action", new StringBody(action));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		if (params != null && !params.isEmpty()) {
			ArrayList<NameValuePair> callbackParamList = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : params.entrySet()) {
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

		setAuth(postMethod);
		
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}
	
	public CallRet callWithBinary(String url, String contentType, byte[] body, long bodyLength) {

		ByteArrayEntity entity = new ByteArrayEntity(body);
		
		HttpPost postMethod = new HttpPost(url);
		
		if (contentType == null || contentType.isEmpty()) {
			postMethod.setHeader("Content-Type", "application/octet-stream");
		} else {
			postMethod.setHeader("Content-Type", contentType);
		}
		postMethod.setEntity(entity);

		setAuth(postMethod);
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		try {
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	private CallRet handleResult(HttpResponse response) {
		
		if (response == null || response.getStatusLine() == null) {
			return new CallRet(400, "No response");
		}
		
		String responseBody;
		try {
			responseBody = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		}
		
		StatusLine status = response.getStatusLine();
		int statusCode = (status == null) ? 400 : status.getStatusCode();
		
		return new CallRet(statusCode, responseBody);
	}
}
