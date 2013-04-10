package com.qiniu.testing;

import junit.framework.TestCase;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.StatRet;

public class MoveTest extends TestCase {

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key = "MoveTest-key";

	public final String srcBucket = "junit_bucket_src";
	public final String destBucket = "junit_bucket_dest";

	@Override
	public void setUp() {
		// get the config
		{
			Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
			Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
			Config.RS_HOST = System.getenv("QINIU_RS_HOST");
			Config.IO_HOST = System.getenv("QINIU_IO_HOST");
			bucketName = System.getenv("QINIU_TEST_BUCKET");
		}

		// check the config
		{
			assertNotNull(Config.ACCESS_KEY);
			assertNotNull(Config.SECRET_KEY);
			assertNotNull(Config.RS_HOST);
			assertNotNull(Config.IO_HOST);
			assertNotNull(bucketName);
		}

		// upload a file to the bucket
		{
			String uptoken = "";
			try {
				uptoken = new PutPolicy(srcBucket, 36000).token();
			} catch (AuthException ignore) {
			}
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();
			extra.bucket = srcBucket;
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
				uptoken = new PutPolicy(bucketName, 36000).token();
			} catch (AuthException ignore) {
			}
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();
			extra.bucket = bucketName;
			PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
		// test move
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			CallRet ret = rs.move(srcBucket, key, destBucket, key);
			System.out.println(ret);
			assertTrue(ret.ok());
		}

		// the src keys should not be availabe in src bucket any more
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet sr = rs.stat(srcBucket, key);
			assertTrue(!sr.ok());
		}
		// the dest bucket should have the key
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet sr = rs.stat(destBucket, key);
			assertTrue(sr.ok());
		}

	}

	@Override
	public void tearDown() {
		// deletes file form the dest bucket
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			CallRet ret = rs.delete(destBucket, key);
			assertTrue(ret.ok());
		}
		// confirms that it's deleted
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet sr = rs.stat(destBucket, key);
			assertTrue(!sr.ok());
		}
	}
}
