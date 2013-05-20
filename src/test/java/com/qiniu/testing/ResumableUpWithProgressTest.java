package com.qiniu.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

import org.json.JSONObject;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.resumable.io.BlockProgress;
import com.qiniu.api.resumable.io.BlockProgressNotifier;
import com.qiniu.api.resumable.io.ProgressNotifier;
import com.qiniu.api.resumable.io.PutExtra;
import com.qiniu.api.resumable.io.ResumableApi;
import com.qiniu.api.resumable.io.UpApi;
import com.qiniu.api.rs.PutFileRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.StatRet;

public class ResumableUpWithProgressTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	public final String key = "ResumableUpWithProgressTest-key";

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

	public void readProgress(String file, String[] checksums,
			BlockProgress[] progresses, int blockCount) throws Exception {

		File fi = new File(file);
		if (!fi.exists()) {
			return;
		}
		FileReader f = new FileReader(file);
		BufferedReader is = new BufferedReader(f);

		try {

			for (;;) {
				String line = is.readLine();
				if (line == null)
					break;

				JSONObject o = new JSONObject(line);

				Object block = o.get("block");

				if (block == null) {
					// error ...
					break;
				}
				int blockIdx = (Integer) block;
				if (blockIdx < 0 || blockIdx >= blockCount) {
					// error ...
					break;
				}

				Object checksum = null;
				if (o.has("checksum")) {
					checksum = o.get("checksum");
				}

				if (checksum != null) {
					checksums[blockIdx] = (String) checksum;
					continue;
				}

				JSONObject progress = null;
				if (o.has("progress")) {
					progress = (JSONObject) o.get("progress");
				}

				if (progress != null) {
					BlockProgress bp = new BlockProgress();
					bp.context = progress.getString("context");
					bp.offset = progress.getInt("offset");
					bp.restSize = progress.getInt("restSize");
					bp.host = progress.getString("host");
					progresses[blockIdx] = bp;

					continue;
				}
				break; // error ...
			}
		} finally {

			if (is != null) {
				is.close();
				is = null;
			}
		}
	}
	
	// just upload an image in testdata.
	public void testResumablePut() throws Exception {

		String uptoken = new PutPolicy(bucketName, 36000).token();
		
		RandomAccessFile file = FileUtils.makeFixedSizeTestFile(4 * 1024 * 1024 + 1);
		
		
		long fsize = file.length();
		int blockCount = UpApi.blockCount(fsize);
		
		String dir = System.getProperty("user.dir");
		String progressFile = dir + "/a.progress" + fsize;
		System.out.println("using progress file : " + progressFile);
		String[] checksums = new String[(int) blockCount];
		BlockProgress[] progresses = new BlockProgress[(int) blockCount];
		
		readProgress(progressFile, checksums, progresses, blockCount);
		
		ResumableNotifier notifier = new ResumableNotifier(progressFile);
		PutExtra extra = new PutExtra(bucketName);
		extra.blockProgressNotifier = (BlockProgressNotifier) notifier;
		extra.progressNotifier = (ProgressNotifier) notifier;
		extra.checksums = checksums;
		extra.progresses = progresses;
		
		PutFileRet putFileRet = ResumableApi.putFile(uptoken, file, key, extra);
		
		if (putFileRet.ok()) {
			new File(progressFile).delete();
		}
		file.close();
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
