/**
 * Port of simple oauth2 python library.
 */

package com.qiniu.qbox.oauth2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.qbox.Config;

public class SimpleClient extends Client {

	private String authUrl;
	private String tokenUrl;
	private String clientId;
	private String clientSecret;
	private String accessToken;
	private String refreshToken;

	public SimpleClient() {
		this.authUrl = Config.AUTHORIZATION_ENDPOINT;
		this.tokenUrl = Config.TOKEN_ENDPOINT;
		this.clientId = Config.CLIENT_ID;
		this.clientSecret = Config.CLIENT_SECRET;
	}

	public SimpleClient(String authUrl, String tokenUrl, String clientId,
			String clientSecret) {

		this.authUrl = authUrl;
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String createAuthUrl(Collection<String> scope, String redirectUri,
			String state) {
		String scopeStr = "";
		if (scope.size() > 0) {
			for (Iterator<String> i = scope.iterator(); i.hasNext(); i.next()) {
				if (!scopeStr.isEmpty()) {
					scopeStr += " ";
				}
				scopeStr += i.toString();
			}
		}

		String url = "";
		try {
			url = String
					.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=code",
							this.authUrl,
							URLEncoder.encode(this.clientId, "UTF-8"),
							URLEncoder.encode(redirectUri, "UTF-8"),
							URLEncoder.encode(scopeStr, "UTF-8"));
			if (!state.isEmpty()) {
				url += String.format("&state=%s", state);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	public AuthRet exchange(String code, String redirectUri) throws AuthException {
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("client_id", this.clientId));
		nvps.add(new BasicNameValuePair("client_secret", this.clientSecret));
		nvps.add(new BasicNameValuePair("code", code));
		nvps.add(new BasicNameValuePair("redirect_uri", redirectUri));
		nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));

		return authPost(nvps);
	}

	public AuthRet exchangeByPassword(String userName, String password)
			throws AuthException {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("client_id", this.clientId));
		nvps.add(new BasicNameValuePair("client_secret", this.clientSecret));
		nvps.add(new BasicNameValuePair("username", userName));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("grant_type", "password"));

		return authPost(nvps);
	}

	public AuthRet exchangeByRefreshToken(String token) throws AuthException {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("client_id", this.clientId));
		nvps.add(new BasicNameValuePair("client_secret", this.clientSecret));
		nvps.add(new BasicNameValuePair("refresh_token", this.refreshToken));
		nvps.add(new BasicNameValuePair("grant_type", "refresh_token"));

		return authPost(nvps);
	}

	private AuthRet authPost(List<NameValuePair> nvps) throws AuthException {
		
		HttpPost postMethod = new HttpPost(this.tokenUrl);
		postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		HttpClient client = new DefaultHttpClient();
		try {
			postMethod.setEntity(new UrlEncodedFormEntity(nvps));

			HttpResponse response = client.execute(postMethod);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new AuthException("Could not fetch access token: " + response.getStatusLine());
			}
			return handleResult(response);
		} catch (Exception e) {
			throw new AuthException("Could not fetch access token.", e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	private AuthRet handleResult(HttpResponse response) throws AuthException {
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String entityText = "";
			try {
				entityText = EntityUtils.toString(entity);
				AuthRet authRet = new AuthRet(new JSONObject(entityText));
				this.accessToken = authRet.getAccessToken();
				this.refreshToken = authRet.getRefreshToken();
				
				return authRet;
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
				throw new AuthException("Auth method returns invalid format: " + entityText);
			}
		}
		return null;
	}

	public CallRet call(String url, int retries, int maxRetries)
			throws AuthException {
		
		HttpPost post = new HttpPost(url);
		post.addHeader("Authorization", "Bearer " + this.accessToken);
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 401 && retries < maxRetries) {
				exchangeByRefreshToken(this.refreshToken);
				return call(url, retries + 1, maxRetries);
			}
			CallRet callRet;
			if (statusCode == 200) {
				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					callRet = new CallRet(200, EntityUtils.toString(responseEntity));
				} else {
					callRet = new CallRet(200, "");
				}
			} else {
				callRet = new CallRet(statusCode, "");
			}
			return callRet;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthException("OAuth2 call failed: " + e.getMessage());
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
}
