package com.qiniu.qbox.oauth2;

import java.util.Collection;

public abstract class Client {

	public abstract String createAuthUrl(Collection<String> scope, String redirectUri, String state);
	
	public abstract AuthRet exchange(String code, String redirectUri) throws AuthException;
	
	public abstract AuthRet exchangeByPassword(String userName, String password) throws AuthException;
	
	public abstract AuthRet exchangeByRefreshToken(String token) throws AuthException;
	
	public abstract CallRet call(String url, int retries, int maxRetries) throws AuthException;
}
