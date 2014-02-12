package com.qiniu.testing;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.Http;

public class HttpClientTimeOutTest{

	@Test(expected= org.apache.http.conn.ConnectTimeoutException.class)
	public void testCONNECTION_TIMEOUT() throws Exception {
		long s = 0;
		try{
			Config.CONNECTION_TIMEOUT = 5;
			Config.SO_TIMEOUT = 20 * 1000;
			
			Http.setClient(null);
			
			HttpClient client = Http.getClient();
			HttpGet httpget = new HttpGet("http://kyfxbl.iteye.com/blog/1616849"); 
			
			s = System.currentTimeMillis();
			HttpResponse ret = client.execute(httpget);
		}catch(Exception e){
			long end = System.currentTimeMillis();
			System.out.println("CONNECTION_TIMEOUT test : " + (end - s));
			throw e;
		}
	}

	@Test(expected= java.net.SocketTimeoutException.class)
	public void testSO_TIMEOUT() throws Exception {
		long s = 0;
		try{
			Config.CONNECTION_TIMEOUT = 20 * 1000;
			Config.SO_TIMEOUT = 5;
			
			Http.setClient(null);
			
			HttpClient client = Http.getClient();
			HttpGet httpget = new HttpGet("http://www.baidu.com");
			
			s = System.currentTimeMillis();
			HttpResponse ret = client.execute(httpget);
		}catch(Exception e){
			long end = System.currentTimeMillis();
			System.out.println("SO_TIMEOUT test : " + (end - s));
			throw e;
		}
	}

}
