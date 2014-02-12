package com.qiniu.testing;

import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.Http;

public class HttpClientTimeOutTest extends TestCase {
	private int CONNECTION_TIMEOUT;
	private int SO_TIMEOUT;

	@Override
	public void setUp() {
		CONNECTION_TIMEOUT = Config.CONNECTION_TIMEOUT;
		SO_TIMEOUT = Config.SO_TIMEOUT;
		Http.setClient(null);
	}

	@Override
	public void tearDown() {
		Config.CONNECTION_TIMEOUT = CONNECTION_TIMEOUT;
		Config.SO_TIMEOUT = SO_TIMEOUT;
		Http.setClient(null);
	}

	public void testCONNECTION_TIMEOUT() {
		Throwable tx = null;
		long s = 0;
		try {
			Config.CONNECTION_TIMEOUT = 5;
			Config.SO_TIMEOUT = 20 * 1000;

			HttpClient client = Http.getClient();
			HttpGet httpget = new HttpGet("http://kyfxbl.iteye.com/blog/1616849");

			s = System.currentTimeMillis();
			HttpResponse ret = client.execute(httpget);

			fail("应该按预期抛出异常 ConnectTimeoutException，测试失败");
		} catch (Exception e) {
			long end = System.currentTimeMillis();
			System.out.println("CONNECTION_TIMEOUT test : " + (end - s));
			tx = e;
		}

		assertNotNull(tx.getMessage());
		assertEquals(ConnectTimeoutException.class, tx.getClass());
	}

	public void testSO_TIMEOUT() {
		Throwable tx = null;
		long s = 0;
		try {
			Config.CONNECTION_TIMEOUT = 20 * 1000;
			Config.SO_TIMEOUT = 5;

			HttpClient client = Http.getClient();
			HttpGet httpget = new HttpGet("http://www.baidu.com");

			s = System.currentTimeMillis();
			HttpResponse ret = client.execute(httpget);
			fail("应该按预期抛出异常 SocketTimeoutException，测试失败");
		} catch (Exception e) {
			long end = System.currentTimeMillis();
			System.out.println("SO_TIMEOUT test : " + (end - s));
			tx = e;
		}
		assertNotNull(tx.getMessage());
		assertEquals(SocketTimeoutException.class, tx.getClass());
	}

}
