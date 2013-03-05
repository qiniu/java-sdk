package com.qiniu.qbox.testing;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.BatchCallRet;
import com.qiniu.qbox.rs.BatchStatRet;
import com.qiniu.qbox.rs.DeleteRet;
import com.qiniu.qbox.rs.DropRet;
import com.qiniu.qbox.rs.EntryUriPair;
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

	public void testBatchStat() throws Exception {
		// upload some file to the buckets
		String key1 = "batch-stat1";
		String key2 = "batch-stat2";
		String entryUri1 = bucketName + ":" + key1;
		String entryUri2 = bucketName + ":" + key2;
		List<String> entryUris = new ArrayList<String>();
		entryUris.add(entryUri1);
		entryUris.add(entryUri2);

		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/testdata/" + key ;

		PutAuthRet putAuthRet = rs.putAuth() ;
		String authorizedUrl = putAuthRet.getUrl() ;
		try {
			// upload file1 with key1
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, bucketName, key1, "", absFilePath, "", "") ;
			assertTrue(putFileRet.ok() && expectedHash.equals(putFileRet.getHash())) ;

			// upload file2 with key2
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet2 = RSClient.putFile(authorizedUrl, bucketName, key2, "", absFilePath, "", "") ;
			assertTrue(putFileRet2.ok() && expectedHash.equals(putFileRet2.getHash())) ;
			// -----------------------------------------------------------------------------------------

			// stat		
			BatchStatRet statRet = rs.batchStat(entryUris);
			assertTrue(statRet.ok());
			List<StatRet> statRetList = statRet.results;
			for (StatRet r : statRetList) {
				assertTrue(r.ok() && r.getHash().equals(expectedHash));
			}
		} finally {
			// delete use batchDelete
			BatchCallRet deleteRet = rs.batchDelete(entryUris);
			assertTrue(deleteRet.ok());
			List<CallRet> callRetList = deleteRet.results;
			for (CallRet r : callRetList) {
				assert(r.ok());
			}
		}
	}

	public void testBatchCopy() throws Exception {
		// upload some files to the source bucket
		String srcKey1 = "src-batch-copy1";
		String srcKey2 = "src-batch-copy2";
		String destKey1 = "dest-batch-copy1";
		String destKey2 = "dest-batch-copy2";
		List<String> entryUris = new ArrayList<String>();
		entryUris.add(srcBucket + ":" + srcKey1);
		entryUris.add(srcBucket + ":" + srcKey2);

		List<String> destEntryUris = new ArrayList<String>();
		destEntryUris.add(destBucket + ":" + destKey1);
		destEntryUris.add(destBucket + ":" + destKey2);

		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/testdata/" + key ;
		try {
			PutAuthRet putAuthRet = rs.putAuth() ;
			String authorizedUrl = putAuthRet.getUrl() ;

			// upload file1 with key1
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, srcBucket, srcKey1, "", absFilePath, "", "") ;
			assertTrue(putFileRet.ok() && expectedHash.equals(putFileRet.getHash())) ;

			// upload file2 with key2
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet2 = RSClient.putFile(authorizedUrl, srcBucket, srcKey2, "", absFilePath, "", "") ;
			assertTrue(putFileRet2.ok() && expectedHash.equals(putFileRet2.getHash())) ;
			// -----------------------------------------------------------------------------------------

			// copy source file to the dest bucket
			EntryUriPair pair1 = new EntryUriPair(srcBucket + ":" + srcKey1, destBucket + ":" + destKey1);
			EntryUriPair pair2 = new EntryUriPair(srcBucket + ":" + srcKey2, destBucket + ":" + destKey2);
			List<EntryUriPair> pairList = new ArrayList<EntryUriPair>();
			pairList.add(pair1);
			pairList.add(pair2);
			BatchCallRet ret= rs.batchCopy(pairList);
			assertTrue(ret.ok());
			for (CallRet r : ret.results) {
				assertTrue(r.ok());
			}
			// -----------------------------------------------------------------------------------------

			// stat checks source files are still availabe
			BatchStatRet statRet = rs.batchStat(entryUris);
			assertTrue(statRet.ok());
			for (StatRet r : statRet.results) {
				assertTrue(r.ok());
			}

			// stat checks dest files are copied successfully
			BatchStatRet statRet2 = rs.batchStat(destEntryUris);
			assertTrue(statRet2.ok());
			for (StatRet r : statRet2.results) {
				assertTrue(r.ok());
			}
		// -----------------------------------------------------------------------------------------
		} finally {
			// delete files from the source bucket
			BatchCallRet srcDeleteRet = rs.batchDelete(entryUris);
			assertTrue(srcDeleteRet.ok());
			for (CallRet r : srcDeleteRet.results) {
				assertTrue(r.ok());
			}
			// delete files from the dest bucket
			BatchCallRet destDeleteRet = rs.batchDelete(destEntryUris);
			assertTrue(destDeleteRet.ok());
			for (CallRet r : destDeleteRet.results) {
				assertTrue(r.ok());
			}
		}
	}

	public void testBatchMove() throws Exception {
		// upload some files to the source bucket
		String srcKey1 = "src-batch-move1";
		String srcKey2 = "src-batch-move2";
		String destKey1 = "dest-batch-move1";
		String destKey2 = "dest-batch-move2";
		List<String> destEntryUris = new ArrayList<String>();
		destEntryUris.add(destBucket + ":" + destKey1);
		destEntryUris.add(destBucket + ":" + destKey2);

		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/testdata/" + key ;

		PutAuthRet putAuthRet = rs.putAuth() ;
		String authorizedUrl = putAuthRet.getUrl() ;

		try {
			// upload file1 with key1
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, srcBucket, srcKey1, "", absFilePath, "", "") ;
			assertTrue(putFileRet.ok() && expectedHash.equals(putFileRet.getHash())) ;

			// upload file2 with key2
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet2 = RSClient.putFile(authorizedUrl, srcBucket, srcKey2, "", absFilePath, "", "") ;
			assertTrue(putFileRet2.ok() && expectedHash.equals(putFileRet2.getHash())) ;
			// -----------------------------------------------------------------------------------------

			// move source files to the dest bucket
			EntryUriPair pair1 = new EntryUriPair(srcBucket + ":" + srcKey1, destBucket + ":" + destKey1);
			EntryUriPair pair2 = new EntryUriPair(srcBucket + ":" + srcKey2, destBucket + ":" + destKey2);
			List<EntryUriPair> pairList = new ArrayList<EntryUriPair>();
			pairList.add(pair1);
			pairList.add(pair2);
			BatchCallRet ret= rs.batchMove(pairList);
			assertTrue(ret.ok());
			for (CallRet r : ret.results) {
				assertTrue(r.ok());
			}
			// -----------------------------------------------------------------------------------------

			// stat checks source files should not be availabe any more
			List<String> entryUris = new ArrayList<String>();
			entryUris.add(srcBucket + ":" + srcKey1);
			entryUris.add(srcBucket + ":" + srcKey2);
			BatchStatRet statRet = rs.batchStat(entryUris);
			assertTrue(statRet.ok()); // confused here! 298?

			// stat checks dest files are avaliable
			BatchStatRet statRet2 = rs.batchStat(destEntryUris);
			assertTrue(statRet2.ok());
			for (StatRet r : statRet2.results) {
				assertTrue(r.ok());
			}
		} finally {
			// delete files from the dest bucket
			BatchCallRet destDeleteRet = rs.batchDelete(destEntryUris);
			assertTrue(destDeleteRet.ok());
			for (CallRet r : destDeleteRet.results) {
				assertTrue(r.ok());
			}
		}
	}

	public void testBatchDelete() throws Exception {
		// upload some file to the buckets
		String key1 = "batch-delete1";
		String key2 = "batch-delete2";
		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/testdata/" + key ;

		PutAuthRet putAuthRet = rs.putAuth() ;
		String authorizedUrl = putAuthRet.getUrl() ;

		try {
			// upload file1 with key1
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet = RSClient.putFile(authorizedUrl, bucketName, key1, "", absFilePath, "", "") ;
			assertTrue(putFileRet.ok() && expectedHash.equals(putFileRet.getHash())) ;

			// upload file2 with key2
			@SuppressWarnings("deprecation")
			PutFileRet putFileRet2 = RSClient.putFile(authorizedUrl, bucketName, key2, "", absFilePath, "", "") ;
			assertTrue(putFileRet2.ok() && expectedHash.equals(putFileRet2.getHash())) ;
		} finally {
			// delete the files from the bucket
			// delete use batchDelete
			String entryUri1 = bucketName + ":" + key1;
			String entryUri2 = bucketName + ":" + key2;
			List<String> entryUris = new ArrayList<String>();
			entryUris.add(entryUri1);
			entryUris.add(entryUri2);

			BatchCallRet deleteRet = rs.batchDelete(entryUris);
			assertTrue(deleteRet.ok());
			List<CallRet> callRetList = deleteRet.results;
			for (CallRet r : callRetList) {
				assert(r.ok());
			}
		}
	}
}