package com.qiniu.testing;

import java.io.File;

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

public class CopyTest extends TestCase {

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key = "CopyTest-key";

	public final String srcBucket = "junit_bucket_src";
	public final String destBucket = "junit_bucket_dest";
	public Mac mac;
	
	@Override
	public void setUp() throws Exception {
		// get the config
		{
			Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
			Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
			Config.RS_HOST = System.getenv("QINIU_RS_HOST");
			bucketName = System.getenv("QINIU_TEST_BUCKET");
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}

		// check the config
		{
			assertNotNull(Config.ACCESS_KEY);
			assertNotNull(Config.SECRET_KEY);
			assertNotNull(Config.RS_HOST);
			assertNotNull(bucketName);
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
		// upload a file to the srcbucket
		{
			String uptoken = "";
			try {
				uptoken = new PutPolicy(bucketName).token(mac);
			} catch (AuthException ignore) {
			}
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();
			PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
		// test move
		{
			RSClient rs = new RSClient(mac);
			CallRet ret = rs.copy(srcBucket, key, destBucket, key);
			System.out.println(ret);
			assertTrue(ret.ok());
		}

		// the src keys should still be availabe in src bucket
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(srcBucket, key);
			assertTrue(sr.ok());
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

		// deletes file form the dest bucket
		{
			RSClient rs = new RSClient(mac);
			CallRet ret = rs.delete(srcBucket, key);
			assertTrue(ret.ok());
		}

		// confirms that it's deleted
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(srcBucket, key);
			assertTrue(!sr.ok());
		}
		// confirms that it's deleted
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(destBucket, key);
			assertTrue(!sr.ok());
		}
	}
}
