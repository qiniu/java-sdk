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
	public String key = "upload.jpg";
	public String bucketName = "junit_fileop";
	public static String url ;
	
	public void setUp() {
		Config.ACCESS_KEY = "ttXNqIvhrYu04B_dWM6GwSpcXOZJvGoYFdznAWnz" ;
		Config.SECRET_KEY = "rX-7Omdag0BIBEtOyuGQXzx4pmTUTeLxoPEw6G8d" ;
	}
	
	public void testUploadWithToken() throws Exception {
		String dir = System.getProperty("user.dir") ;
		String absFilePath = dir + "/res/" + key ;
		
		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();
		PutFileRet putRet = RSClient.putFileWithToken(token, bucketName, key, absFilePath, "", "", "", "") ;
		assertTrue(putRet.ok()) ;
	}
	
	public void testRsGet() throws Exception {
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucketName);
		GetRet getRet = rs.get(key, key);
		url = getRet.getUrl();
		assertTrue(getRet.ok()) ;
	}
	
	public void testImageInfo() {
		CallRet callRet = ImageInfo.call(url) ;
		assertTrue("ImageInfo " + url + " failed!", callRet.ok()) ;
	}
	
	public void testImageExif() {
		CallRet callRet = ImageExif.call(url) ;
		assertTrue("ImageExif " + url + " failed!", callRet.ok()) ;
	}
	
	public void testImageView() {
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
	
	public void testImageMogrify() {
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
