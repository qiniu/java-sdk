package com.qiniu.api.net;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;

import com.qiniu.api.auth.AuthException;

/**
 * The class {@code Client} is a typical wrapper of RPC. Also see
 * {@code com.qiniu.api.auth.DigestAuthClient} 
 */
public class Client {

	/**
	 * 
	 * @param post
	 * @throws AuthException
	 */
	public void setAuth(HttpPost post) throws AuthException {

	}

	/**
	 * Sends a http post request to the specified url.
	 * 
	 * @param url
	 *            the request url
	 * @return A general response
	 */
	public CallRet call(String url) {
		HttpClient client = Http.getClient();
		HttpPost postMethod = new HttpPost(url);
		
		try {
			setAuth(postMethod);
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		}
	}

	/**
	 * Sends a http post request to the specified url with a list of
	 * <code>NameValuePair<code>.
	 * 
	 * @param url
	 *            the request url
	 * @param nvps
	 * @return A general response
	 */
	public CallRet call(String url, List<NameValuePair> nvps) {
		HttpClient client = Http.getClient();
		HttpPost postMethod = new HttpPost(url);
		try {
			StringEntity entity = new UrlEncodedFormEntity(nvps, "UTF-8");
			entity.setContentType("application/x-www-form-urlencoded");
			postMethod.setEntity(entity);

			setAuth(postMethod);
			HttpResponse response = client.execute(postMethod);

			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		}
	}

	/**
	 * Sends a http request to the specified url with the specified entity.
	 * @param url
	 * @param entity
	 * @return A general response
	 */
	public CallRet callWithBinary(String url, AbstractHttpEntity entity) {
		HttpClient client = Http.getClient();
		HttpPost postMethod = new HttpPost(url);
		postMethod.setEntity(entity);

		try {
			setAuth(postMethod);
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		}
	}

	/**
	 * 
	 * @param url
	 *            the request url
	 * @param contentType
	 *            the request content type
	 * @param body
	 *            the request body
	 * @param bodyLength
	 *            the length of the request body
	 * @return A general response
	 */
	public CallRet callWithBinary(String url, String contentType, byte[] body) {
		ByteArrayEntity entity = new ByteArrayEntity(body);

		if (contentType == null || contentType.isEmpty()) {
			contentType = "application/octet-stream";
		}
		entity.setContentType(contentType);
		return callWithBinary(url, entity);
	}

	/**
	 * 
	 * @param url
	 * @param requestEntity
	 * @return A general response format
	 */
	public CallRet callWithMultiPart(String url, MultipartEntity requestEntity) {
		HttpPost postMethod = new HttpPost(url);
		postMethod.setEntity(requestEntity);
		HttpClient client = Http.getClient();
		
		try {
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new CallRet(400, e);
		}
	}

	/**
	 * Transforms a httpresponse to user expected format.
	 * 
	 * @param response
	 *            http response body
	 * @return a formated general response structure
	 */
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
