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

public class RSStatTest extends TestCase {

	public final String domain = System.getenv("QINIU_TEST_DOMAIN");
	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key = "RSTest-key";

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
	}

	public void testRsStat() throws Exception {
		// stat an exist entry
		{
			RSClient rs = new RSClient(mac);
			Entry ret = rs.stat(bucketName, key);
			assertTrue(ret.ok());
			assertTrue(ret.getHash().equals(expectedHash));
		}

		// stat an entry does not exist
		{
			RSClient rs = new RSClient(mac);
			Entry ret = rs.stat(bucketName, "A_KEY_DOES_NOT_EXIST");
			assertTrue(!ret.ok());
		}
	}

	@Override
	public void tearDown() {
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
