package com.qiniu.testing;

import java.io.RandomAccessFile;

import junit.framework.TestCase;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.config.Config;
import com.qiniu.api.resumable.io.PutExtra;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.resumable.io.BlockProgress;
import com.qiniu.api.resumable.io.BlockProgressNotifier;
import com.qiniu.api.resumable.io.ProgressNotifier;
import com.qiniu.api.resumable.io.ResumableApi;
import com.qiniu.api.rs.PutFileRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.StatRet;

public class ResumableUpTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	public final String key = "ResumableUpTest-key";

	public final String expectedHash = "ltpWa7uTHPnnrLxHi2djbeLGQsmR";

	public String bucketName;

	@Override
	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		Config.RS_HOST = System.getenv("QINIU_RS_HOST");
		Config.IO_HOST = System.getenv("QINIU_IO_HOST");
		bucketName = System.getenv("QINIU_TEST_BUCKET");
		assertNotNull(Config.ACCESS_KEY);
		assertNotNull(Config.SECRET_KEY);
		assertNotNull(Config.RS_HOST);
		assertNotNull(Config.IO_HOST);
		assertNotNull(bucketName);
	}

	// just upload an image in testdata.
	public void testResumablePut() throws Exception {
		
		class Notifier implements ProgressNotifier, BlockProgressNotifier {
			@Override
			public void notify(int blockIndex, String checksum) {
				System.out.println("Progress Notify:" + "\n\tBlockIndex: "
						+ String.valueOf(blockIndex) + "\n\tChecksum: "
						+ checksum);
			}

			@Override
			public void notify(int blockIndex, BlockProgress progress) {
				System.out.println("BlockProgress Notify:" + "\n\tBlockIndex: "
						+ String.valueOf(blockIndex) + "\n\tContext: "
						+ progress.context + "\n\tOffset: "
						+ String.valueOf(progress.offset) + "\n\tRestSize: "
						+ String.valueOf(progress.restSize) + "\n\tHost: "
						+ progress.host);
			}
		}
		
		PutPolicy policy = new PutPolicy(bucketName, 36000);
		String uptoken = policy.token();
		
		RandomAccessFile file = FileUtils.makeFixedSizeTestFile(4 * 1024 * 1024 + 1);

		Notifier notifier = new Notifier();

		PutExtra extra = new PutExtra(bucketName);
		extra.blockProgressNotifier = (BlockProgressNotifier) notifier;
		extra.progressNotifier = (ProgressNotifier) notifier;
		PutFileRet putFileRet = ResumableApi.putFile(uptoken, file, key, extra);
		file.close();
		System.out.println(putFileRet);
		assertTrue(putFileRet.ok());
		assertTrue(expectedHash.equals(putFileRet.getHash()));
	}

	@Override
	public void tearDown() {
		// delete the metadata from rs
		// confirms it exists.
		{
			DigestAuthClient conn = new DigestAuthClient();
			RSClient rs = new RSClient(conn);
			StatRet sr = rs.stat(bucketName, key);
			assertTrue(sr.ok());
			assertTrue(expectedHash.equals(sr.getHash()));
		}

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
