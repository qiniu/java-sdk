package com.qiniu.qbox.fileop;

import org.apache.http.client.methods.HttpPost;

import com.qiniu.qbox.auth.Client;

public class DefaultHttpClient extends Client {

	@Override
	public void setAuth(HttpPost post) {
		// nothing to do  
	}

}
