package com.qiniu.qbox.testing;

import junit.framework.TestCase;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.fileop.ImageExif;
import com.qiniu.qbox.fileop.ImageInfo;
import com.qiniu.qbox.fileop.ImageMogrify;
import com.qiniu.qbox.fileop.ImageView;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;

public class FileopTest extends TestCase {
	
	//public void setUp() {
	static {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY") ;
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY") ;
		Config.UP_HOST = System.getenv("QINIU_UP_HOST") ;
		Config.IO_HOST = System.getenv("QINIU_IO_HOST") ;
		Config.RS_HOST = System.getenv("QINIU_RS_HOST") ;
		String bucket = System.getenv("QINIU_TEST_BUCKET") ;
		assertNotNull(Config.ACCESS_KEY) ;
		assertNotNull(Config.SECRET_KEY) ;
		assertNotNull(Config.UP_HOST) ;
		assertNotNull(Config.RS_HOST) ;
		assertNotNull(Config.IO_HOST) ;
		assertNotNull(bucket) ;
	}
	
	public void testUploadWithToken() throws Exception {
		String key = "upload.jpg" ;
		String bucketName = System.getenv("QINIU_TEST_BUCKET") ;
		String expectHash = "FnM8Lt3Mk6yCcYPaosvnAOwWZqyM" ;
		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/res/" + key ;
		
		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();
		PutFileRet putRet = RSClient.putFileWithToken(token, bucketName, key, absFilePath, "", "", "", "") ;
		
		GetRet getRet = this.rsGet() ;
		String hash = getRet.getHash() ;
		assertTrue(putRet.ok() && (expectHash.equals(hash))) ;
	}
	
	public GetRet rsGet() throws Exception {
		String key = "upload.jpg" ;
		String bucketName = System.getenv("QINIU_TEST_BUCKET") ;
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucketName);
		GetRet getRet = rs.get(key, key);
		return getRet ;
	}
	
	public void testImageInfo() throws Exception {
		GetRet getRet = this.rsGet() ;
		assertTrue(getRet.ok()) ;
		String url = getRet.getUrl() ;
		CallRet callRet = ImageInfo.call(url) ;
		assertTrue("ImageInfo " + url + " failed!", callRet.ok()) ;
	}
	
	public void testImageExif() throws Exception {
		GetRet getRet = this.rsGet() ;
		assertTrue(getRet.ok()) ;
		String url = getRet.getUrl() ;
		CallRet callRet = ImageExif.call(url) ;
		assertTrue("ImageExif " + url + " failed!", callRet.ok()) ;
	}
	
	public void testImageView() throws Exception {
		GetRet getRet = this.rsGet() ;
		assertTrue(getRet.ok()) ;
		String url = getRet.getUrl() ;
		ImageView imgView = new ImageView() ;
		imgView.mode = 1 ;
		imgView.width = 100 ;
		imgView.height = 200 ;
		imgView.quality = 1 ;
		imgView.format = "jpg" ;
		imgView.sharpen = 100 ;
		CallRet imgViewRet = imgView.call(url) ;
		assertTrue("ImageView " + url + " failed!", imgViewRet.ok()) ;
	}
	
	public void testImageMogrify() throws Exception {
		GetRet getRet = this.rsGet() ;
		assertTrue(getRet.ok()) ;
		String url = getRet.getUrl() ;
		
		ImageMogrify imgMogr = new ImageMogrify() ;
		imgMogr.thumbnail = "!120x120r" ;
		imgMogr.gravity = "center" ;
		imgMogr.crop = "!120x120a0a0" ;
		imgMogr.quality = 85 ;
		imgMogr.rotate = 45 ;
		imgMogr.format = "jpg" ;
		imgMogr.autoOrient = true ;
		CallRet imgMogrRet = imgMogr.call(url) ;
		assertTrue("ImageMogr " + url + " failed!", imgMogrRet.ok()) ;
	}
	
	public void testImageViewMakeRequest() {
		String testUrl = "http://iovip.qbox.me/file/xyz==" ;
		ImageView imgView = new ImageView() ;
		imgView.mode = 2 ;
		String url = imgView.makeRequest(testUrl) ;
		assertEquals(testUrl + "?imageView/2", url) ;
		
		imgView.height = 200 ;
		url = imgView.makeRequest(testUrl) ;
		assertEquals(testUrl + "?imageView/2/h/200", url) ;
		
		imgView.sharpen = 10 ;
		url = imgView.makeRequest(testUrl) ;
		assertEquals(testUrl + "?imageView/2/h/200/sharpen/10", url) ;
	}
	
	public void testImageMogrifyMakeRequest() {
		String testUrl = "http://iovip.qbox.me/file/xyz==" ;
		ImageMogrify imgMogr = new ImageMogrify() ;
		imgMogr.format = "jpg" ;
		String url = imgMogr.makeRequest(testUrl) ;
		assertEquals(testUrl+"?imageMogr/format/jpg", url) ;
		
		imgMogr.autoOrient = true ;
		url = imgMogr.makeRequest(testUrl) ;
		assertEquals(testUrl+"?imageMogr/format/jpg/auto-orient", url) ;
	}
	
}
