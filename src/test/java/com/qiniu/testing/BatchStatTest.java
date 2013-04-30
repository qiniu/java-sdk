package com.qiniu.testing;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.BatchCallRet;
import com.qiniu.api.rs.BatchStatRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.StatRet;

public class BatchStatTest extends TestCase {

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key1 = "BatchStatTest-key1";
	public final String key2 = "BatchStatTest-key2";

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

		// upload a file to the bucket with key1, key2
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
			PutRet ret = IoApi.putFile(uptoken, key1, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));

			// upload a second one
			ret = IoApi.putFile(uptoken, key2, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
	}

	public void testBatchStat() throws Exception {

		// test BatchStat
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = bucketName;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = bucketName;
			e2.key = key2;
			entries.add(e2);

			BatchStatRet bsRet = rs.batchStat(entries);
			// check batchCall
			assertTrue(bsRet.ok());

			List<StatRet> results = bsRet.results;
			for (StatRet r : results) {
				// check each result
				assertTrue(r.ok());
				assertTrue(r.getHash().equals(expectedHash));
			}

		}

	}

	@Override
	public void tearDown() {
		// deletes files from the bucket
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = bucketName;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = bucketName;
			e2.key = key2;
			entries.add(e2);

			BatchCallRet bret = rs.batchDelete(entries);
			assertTrue(bret.ok());

			List<CallRet> results = bret.results;
			for (CallRet r : results) {
				assertTrue(r.ok());
			}
		}

		// use batchstat checks it again.
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = bucketName;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = bucketName;
			e2.key = key2;
			entries.add(e2);

			BatchStatRet bsRet = rs.batchStat(entries);
			// check batchCall
			assertTrue(!bsRet.ok());
		}
	}
}
