package com.qiniu.testing;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.iovip.iovipClient;
import com.qiniu.api.net.CallRet;

import junit.framework.TestCase;

public class FetchTest extends TestCase {
	
	public final String srcBucket = System.getenv("QINIU_TEST_SRC_BUCKET");
	public final String destBucket = System.getenv("QINIU_TEST_BUCKET");
	public Mac mac;

	@Override
	public void setUp() throws Exception {
		// get the config
		{
			Config.ACCESS_KEY = "7sEgop7h-Njukh-TvvbveCQ4wrLEYXSW7LAIAS9x";
			Config.SECRET_KEY = "Nuw3OjxghrJzRo_9A4b05ynxHRYq15mg9fxRvGtB";
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}

		// check the config
		{
			assertNotNull(Config.ACCESS_KEY);
			assertNotNull(Config.SECRET_KEY);
			assertNotNull(Config.IOVIP);
		}

	}
	
	public void testFetch() throws Exception {
		// test move
		{
			iovipClient iovip = new iovipClient(mac);
		   CallRet  fetchRet   =  iovip.fetch("http://fanyi.jobbole.com/175/", "testpublic", "testfetch1");
			assertTrue(fetchRet.ok());
		}

	

	}
	
	

}
