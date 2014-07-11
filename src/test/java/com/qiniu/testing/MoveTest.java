package com.qiniu.testing;

import junit.framework.TestCase;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class MoveTest extends TestCase {

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public final String key = "java-MoveTest-key";

	public final String srcBucket = System.getenv("QINIU_TEST_SRC_BUCKET");
	public final String destBucket = System.getenv("QINIU_TEST_BUCKET");

	public Mac mac;

	@Override
	public void setUp() throws Exception {
		// get the config
		{
			Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
			Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
			Config.RS_HOST = System.getenv("QINIU_RS_HOST");
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}

		// check the config
		{
			assertNotNull(Config.ACCESS_KEY);
			assertNotNull(Config.SECRET_KEY);
			assertNotNull(Config.RS_HOST);
		}

		// upload a file to the bucket
		{
			String uptoken = "";
			try {
				uptoken = new PutPolicy(srcBucket).token(mac);
			} catch (AuthException ignore) {
			}
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();

			PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
	}

	public void testMove() throws Exception {
		// test move
		{
			RSClient rs = new RSClient(mac);
			CallRet ret = rs.move(srcBucket, key, destBucket, key);
			assertTrue(ret.ok());
		}

		// the src keys should not be availabe in src bucket any more
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(srcBucket, key);
			assertTrue(!sr.ok());
		}
		// the dest bucket should have the key
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(destBucket, key);
			assertTrue(sr.ok());
		}

	}

	@Override
	public void tearDown() {
		// deletes file form the dest bucket
		{
			RSClient rs = new RSClient(mac);
			CallRet ret = rs.delete(destBucket, key);
			assertTrue(ret.ok());
		}
		// confirms that it's deleted
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(destBucket, key);
			assertTrue(!sr.ok());
		}
	}
}
