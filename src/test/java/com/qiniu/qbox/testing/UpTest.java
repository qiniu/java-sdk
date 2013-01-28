package com.qiniu.qbox.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

import org.json.JSONObject;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.UpTokenClient;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.UpService;

public class UpTest extends TestCase {

	public String bucketName;

	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		Config.UP_HOST = System.getenv("QINIU_UP_HOST");
		Config.RS_HOST = System.getenv("QINIU_RS_HOST");
		Config.IO_HOST = System.getenv("QINIU_IO_HOST");
		this.bucketName = System.getenv("QINIU_TEST_BUCKET");

		assertNotNull(Config.ACCESS_KEY);
		assertNotNull(Config.SECRET_KEY);
		assertNotNull(Config.UP_HOST);
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
	
	
	public void testResumablePut() throws Exception {
		
		String key = "upload.file";  //upload.file > 4M
		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/res/" + key;
		
		String expectHash = "ltpWa7uTHPnnrLxHi2djbeLGQsmR" ;
		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();
		UpTokenClient upTokenClient = new UpTokenClient(token);
		UpService upClient = new UpService(upTokenClient);

		RandomAccessFile f = null;
		try {
			
			// fsize = 4M + 1byte
			f = FileUtils.makeFixedSizeTestFile(1024 * 1024 * 4 + 1) ;
			long fsize = f.length();
			int blockCount = UpService.blockCount(fsize);
			
			String progressFile = absFilePath + ".progress" + fsize;
			String[] checksums = new String[(int) blockCount];
			BlockProgress[] progresses = new BlockProgress[(int) blockCount];
			readProgress(progressFile, checksums, progresses, blockCount);
			
			ResumableNotifier notif = new ResumableNotifier(progressFile);	
			PutFileRet putFileRet = RSClient.resumablePutFile(upClient, checksums,
					progresses, (ProgressNotifier) notif,
					(BlockProgressNotifier) notif, bucketName, key, "", f, fsize,
					"CustomMeta", "");
	
			String hash = putFileRet.getHash() ;
			assertTrue(putFileRet.ok() &&(expectHash.equals(hash))) ;
			
			// if upload successfully, delete the progress file.
			File progress = new File(progressFile) ;
			progress.delete() ;
			
		} finally {
			
			if (f != null) {
				f.close();
				f = null;
			}
		}
	}
}
