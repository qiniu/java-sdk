package com.qiniu.qbox.auth;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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

	public static byte[] urlsafeEncodeBytes(byte[] src) {
		if (src.length % 3 == 0) {
			return Base64.encodeBase64(src, false, true);
		}
		byte[] b = Base64.encodeBase64(src, false, true);
		int pad = 4 - b.length % 4;
		byte[] b2 = new byte[b.length + pad];
		System.arraycopy(b, 0, b2, 0, b.length);
		b2[b.length] = '=';
		if (pad > 1) {
			b2[b.length+1] = '=';
		}
		return b2;
	}

	public static String urlsafeEncodeString(byte[] src) {
		return new String(urlsafeEncodeBytes(src));
	}

	public static String urlsafeEncode(String text) {
		return new String(urlsafeEncodeBytes(text.getBytes()));
	}
}
