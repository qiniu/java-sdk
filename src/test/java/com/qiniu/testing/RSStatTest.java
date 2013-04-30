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

public class RSStatTest extends TestCase {

	public final String domain = "http://junit-bucket.qiniudn.com";
	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key = "RSTest-key";

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
	}

	public void testRsStat() throws Exception {
		// stat an exist entry
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet ret = rs.stat(bucketName, key);
			assertTrue(ret.ok());
			assertTrue(ret.getHash().equals(expectedHash));
		}

		// stat an entry does not exist
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet ret = rs.stat(bucketName, "A_KEY_DOES_NOT_EXIST");
			assertTrue(!ret.ok());
		}
	}

	@Override
	public void tearDown() {
		// deletes it from rs
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			CallRet cr = rs.delete(bucketName, key);
			assertTrue(cr.ok());
		}

		// confirms that it's deleted
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet sr = rs.stat(bucketName, key);
			assertTrue(!sr.ok());
		}
	}
}