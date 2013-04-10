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
import com.qiniu.api.rs.EntryPathPair;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.StatRet;

public class BatchCopyTest extends TestCase {

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	public final String key1 = "BatchCopyTest-key1";
	public final String key2 = "BatchCopyTest-key2";

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

		// upload a file to src bucket with key1, key2
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
			PutRet ret = IoApi.putFile(uptoken, key1, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));

			// upload a second one
			ret = IoApi.putFile(uptoken, key2, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
	}

	public void testBatchCopy() throws Exception {

		// use batchStat to make sure they're existed in src bucket.
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = srcBucket;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = srcBucket;
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
		// do batchCopy
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPathPair> entries = new ArrayList<EntryPathPair>();

			EntryPathPair pair1 = new EntryPathPair();

			EntryPath src = new EntryPath();
			src.bucket = srcBucket;
			src.key = key1;

			EntryPath dest = new EntryPath();
			dest.bucket = destBucket;
			dest.key = key1;

			pair1.src = src;
			pair1.dest = dest;

			EntryPathPair pair2 = new EntryPathPair();

			EntryPath src2 = new EntryPath();
			src2.bucket = srcBucket;
			src2.key = key2;

			EntryPath dest2 = new EntryPath();
			dest2.bucket = destBucket;
			dest2.key = key2;

			pair2.src = src2;
			pair2.dest = dest2;

			entries.add(pair1);
			entries.add(pair2);

			BatchCallRet ret = rs.batchCopy(entries);

			// check batchMove
			assertTrue(ret.ok());

			List<CallRet> results = ret.results;
			for (CallRet r : results) {
				assertTrue(r.ok());
			}
		}
		// the src keys should still be available in src bucket
		{
			DigestAuthClient client = new DigestAuthClient();
			RSClient rs = new RSClient(client);

			List<EntryPath> entries = new ArrayList<EntryPath>();
			EntryPath e1 = new EntryPath();
			e1.bucket = srcBucket;
			e1.key = key1;

			EntryPath e2 = new EntryPath();
			e2.bucket = srcBucket;
			e2.key = key2;

			entries.add(e1);
			entries.add(e2);

			BatchStatRet ret = rs.batchStat(entries);
			assertTrue(ret.ok());

			List<StatRet> results = ret.results;
			for (StatRet r : results) {
				assertTrue(r.ok());
			}
		}
		// the dest bucket should have the keys
		{
			DigestAuthClient client = new DigestAuthClient();
			RSClient rs = new RSClient(client);

			List<EntryPath> entries = new ArrayList<EntryPath>();
			EntryPath e1 = new EntryPath();
			e1.bucket = destBucket;
			e1.key = key1;

			EntryPath e2 = new EntryPath();
			e2.bucket = destBucket;
			e2.key = key2;

			entries.add(e1);
			entries.add(e2);

			BatchStatRet ret = rs.batchStat(entries);
			assertTrue(ret.ok());

			List<StatRet> results = ret.results;
			for (StatRet r : results) {
				assertTrue(r.ok());
			}
		}

	}

	@Override
	public void tearDown() {

		// delete keys from the src bucket
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = srcBucket;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = srcBucket;
			e2.key = key2;
			entries.add(e2);

			BatchCallRet bret = rs.batchDelete(entries);
			assertTrue(bret.ok());

			List<CallRet> results = bret.results;
			for (CallRet r : results) {
				assertTrue(r.ok());
			}
		}

		// delete keys from the dest bucket
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = destBucket;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = destBucket;
			e2.key = key2;
			entries.add(e2);

			BatchCallRet bret = rs.batchDelete(entries);
			assertTrue(bret.ok());

			List<CallRet> results = bret.results;
			for (CallRet r : results) {
				assertTrue(r.ok());
			}
		}
		
		// use batchStat to check src bucket
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = srcBucket;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = srcBucket;
			e2.key = key2;
			entries.add(e2);

			BatchStatRet bsRet = rs.batchStat(entries);
			// check batchCall
			assertTrue(!bsRet.ok());
		}
		
		// use batchstat checks dest bucket again.
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			List<EntryPath> entries = new ArrayList<EntryPath>();

			EntryPath e1 = new EntryPath();
			e1.bucket = destBucket;
			e1.key = key1;
			entries.add(e1);

			EntryPath e2 = new EntryPath();
			e2.bucket = destBucket;
			e2.key = key2;
			entries.add(e2);

			BatchStatRet bsRet = rs.batchStat(entries);
			// check batchCall
			assertTrue(!bsRet.ok());
		}
	}
}
