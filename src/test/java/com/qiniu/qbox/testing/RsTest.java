package com.qiniu.qbox.testing;

import junit.framework.TestCase;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
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
	
}

