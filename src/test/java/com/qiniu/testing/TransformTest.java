package com.qiniu.testing;

import java.io.*;

import junit.framework.TestCase;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class TransformTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	public final String key = "IOTest-key";
		
	public final String expectedHash = "FivxSqsM1SyWCnYeIGPUqZM5LL4b";

	public String bucketName;
	
	public Mac mac;
	@Override
	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		Config.RS_HOST = System.getenv("QINIU_RS_HOST");
		bucketName = System.getenv("QINIU_TEST_BUCKET");

		assertNotNull(Config.ACCESS_KEY);
		assertNotNull(Config.SECRET_KEY);
		assertNotNull(Config.RS_HOST);
		assertNotNull(bucketName);
		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
	}

	public void testPutTransform() throws Exception {
		PutPolicy putPolicy = new PutPolicy(bucketName);
		putPolicy.transform = "imageView/2/w/100/h/100";
		putPolicy.fopTimeout = 10;

		String uptoken = putPolicy.token(mac);
		String dir = System.getProperty("user.dir");
		String localFile = dir + "/testdata/" + "logo.png";

		PutExtra extra = new PutExtra();

		PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
		assertTrue(ret.ok());
	}

	@Override
	public void tearDown() {
		// delete the metadata from rs
		// confirms it exists.
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(bucketName, key);
			System.out.println(sr.getHash());
			assertTrue(sr.ok());
			assertTrue(expectedHash.equals(sr.getHash()));
		}

		// deletes it from rs
		{
			RSClient rs = new RSClient(mac);
			CallRet cr = rs.delete(bucketName, key);
			assertTrue(cr.ok());
		}

		// confirms that it's deleted
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(bucketName, key);
			assertTrue(!sr.ok());
		}
	}
}