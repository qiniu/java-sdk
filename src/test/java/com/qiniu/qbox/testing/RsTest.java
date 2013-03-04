package com.qiniu.qbox.testing;

import junit.framework.TestCase;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.BucketsRet;
import com.qiniu.qbox.rs.DeleteRet;
import com.qiniu.qbox.rs.DropRet;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.PublishRet;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;
import com.qiniu.qbox.rs.StatRet;

public class RsTest extends TestCase {
	
	public final String DEMO_DOMAIN = "junit-bucket.qiniudn.com" ;
	public final String key = "logo.png" ;
	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR" ;
	public RSService rs ;
	public String bucketName ;
	
	// use fixed buckets
	public final String srcBucket = "junit_bucket_src";
	public final String destBucket = "junit_bucket_dest";
	
	public void setUp() {
		
		Config.ACCESS_KEY =  System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY =  System.getenv("QINIU_SECRET_KEY");
		Config.UP_HOST = System.getenv("QINIU_UP_HOST") ;
		Config.RS_HOST = System.getenv("QINIU_RS_HOST") ;
		Config.IO_HOST = System.getenv("QINIU_IO_HOST") ;
		bucketName = System.getenv("QINIU_TEST_BUCKET") ;
		
		assertNotNull(Config.ACCESS_KEY) ;
		assertNotNull(Config.SECRET_KEY) ;
		assertNotNull(Config.UP_HOST) ;
		assertNotNull(Config.RS_HOST) ;
		assertNotNull(Config.IO_HOST) ;
		assertNotNull(bucketName) ;
		
		DigestAuthClient conn = new DigestAuthClient();
		rs = new RSService(conn, bucketName) ;
	}
	
	
	public void testPutFile() throws Exception {
	
		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/testdata/" + key ;
		
		PutAuthRet putAuthRet = rs.putAuth() ;
		String authorizedUrl = putAuthRet.getUrl() ;
		
		@SuppressWarnings("deprecation")
		PutFileRet putFileRet = RSClient.putFile(authorizedUrl, bucketName, key, "", absFilePath, "", "") ;
		assertTrue(putFileRet.ok() && expectedHash.equals(putFileRet.getHash())) ;
	}
	
	public void testRsGet() throws Exception {
		
		GetRet getRet = rs.get(key, key) ;
		assertTrue(getRet.ok() && expectedHash.equals(getRet.getHash())) ;
	}
	
	public void testRsStat() throws Exception {
		
		StatRet statRet = rs.stat(key) ;
		assertTrue(statRet.ok() && expectedHash.equals(statRet.getHash())) ;
	}

	public void testRsGetIfNotModified() throws Exception {
		
		GetRet getIfNotModifiedRet = rs.getIfNotModified(key, key, expectedHash) ;
		assertTrue(getIfNotModifiedRet.ok() && expectedHash.equals(getIfNotModifiedRet.getHash())) ;
	}
	
	public void testPublish() throws Exception {
		
		PublishRet publishRet = rs.publish(DEMO_DOMAIN);
		assertTrue(publishRet.ok()) ;
	}
	
	public void testUnpublish() throws Exception {
		
		PublishRet unpublishRet = rs.unpublish(DEMO_DOMAIN);
		assertTrue(unpublishRet.ok()) ;
	}
	
	public void testDelete() throws Exception {
		
		DeleteRet deleteRet = rs.delete(key) ;
		assertTrue(deleteRet.ok()) ;
	}
	
	public void testMkBucket() throws Exception {
		
		String newBucketName = "a-new-bucketname-for-junit-test" ;
		CallRet mkBucketRet = rs.mkBucket(newBucketName) ;
		assertTrue(mkBucketRet.ok()) ;
	}
	
	public void testDropBucket() throws Exception {
		
		String delBucketName = "a-new-bucketname-for-junit-test" ;
		DigestAuthClient conn = new DigestAuthClient() ;
		RSService rs = new RSService(conn, delBucketName) ;
		DropRet dropRet = rs.drop() ;
		assertTrue(dropRet.ok()) ;
	}
	
	public void testMove() throws Exception {
		
		String srcKey = "src-key-move";
		String destKey = "dest-key-move";
		String entryUriSrc = srcBucket + ":" + srcKey;
		String entryUriDest = destBucket + ":" + destKey;
		DigestAuthClient conn = new DigestAuthClient();
		rs = new RSService(conn, srcBucket) ;
		
		try {
			// upload a file to the source bucket
			String dir = System.getProperty("user.dir");
			String absFilePath = dir + "/testdata/" + key;

			PutAuthRet putAuthRet = rs.putAuth();
			String authorizedUrl = putAuthRet.getUrl();

			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, srcBucket,
					srcKey, "", absFilePath, "", "");
			assertTrue(putFileRet.ok()&& expectedHash.equals(putFileRet.getHash()));

			// move
			rs = new RSService(conn, srcBucket);
			CallRet moveRet = rs.move(entryUriSrc, entryUriDest);
			assertTrue(moveRet.ok());

			// stat to check the result
			// stat the src, should not exist!
			rs = new RSService(conn, srcBucket);
			StatRet srcStatRet = rs.stat(srcKey);
			assertTrue(!srcStatRet.ok());

			// stat the dest, should exist!
			rs = new RSService(conn, destBucket);
			StatRet statRet = rs.stat(destKey);
			assertTrue(statRet.ok() && expectedHash.equals(statRet.getHash()));

		} finally {
			// delete file in the dest bucket
			rs = new RSService(conn, destBucket);
			DeleteRet destDeleteRet = rs.delete(destKey);
			assertTrue(destDeleteRet.ok());
		}
	}
	
	public void testCopy() throws Exception {
		
		String srcKey = "src-key-copy";
		String destKey = "dest-key-copy";
		String entryUriSrc = srcBucket + ":" + srcKey;
		String entryUriDest = destBucket + ":" + destKey;
		DigestAuthClient conn = new DigestAuthClient();
		rs = new RSService(conn, srcBucket) ;
		
		try {
			// upload a file to the source bucket
			String dir = System.getProperty("user.dir");
			String absFilePath = dir + "/testdata/" + key;

			PutAuthRet putAuthRet = rs.putAuth();
			String authorizedUrl = putAuthRet.getUrl();

			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, srcBucket,
					srcKey, "", absFilePath, "", "");
			assertTrue(putFileRet.ok()&& expectedHash.equals(putFileRet.getHash()));

			// copy
			rs = new RSService(conn, srcBucket);
			CallRet copyRet = rs.copy(entryUriSrc, entryUriDest);
			assertTrue(copyRet.ok());

			// stat to check the result
			// file should exist in the source bucket
			rs = new RSService(conn, srcBucket);
			StatRet srcStatRet = rs.stat(srcKey);
			assertTrue(srcStatRet.ok()&& expectedHash.equals(srcStatRet.getHash()));

			// file should exist in the dest bucket
			rs = new RSService(conn, destBucket);
			StatRet statRet = rs.stat(destKey);
			assertTrue(statRet.ok() && expectedHash.equals(statRet.getHash())) ;
			
		} finally {
			// delete the file in the source/dest bucket
			rs = new RSService(conn, srcBucket);
			DeleteRet srcDeleteRet = rs.delete(srcKey);
			assertTrue(srcDeleteRet.ok());

			rs = new RSService(conn, destBucket);
			DeleteRet destDeleteRet = rs.delete(destKey);
			assertTrue(destDeleteRet.ok());
		}
	}

	public void testBuckets() throws Exception {
		// just test ret.ok().
		// it is not a good way to compare wheter the result contains all the 
		// existing buckets. Because it's a shared account, other users may also
		// create or drop bucket at the same time.
		BucketsRet ret = rs.buckets();
		assertTrue(ret.ok());
	}
	
}
