package com.qiniu.testing;

import java.io.File;

import junit.framework.TestCase;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.fop.ExifRet;
import com.qiniu.api.fop.ImageExif;
import com.qiniu.api.fop.ImageInfo;
import com.qiniu.api.fop.ImageInfoRet;
import com.qiniu.api.fop.ImageView;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class FileopTest extends TestCase {

	public String key = "FileopTest-key";

	public String bucketName;

	public String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String domain = "http://junitbucket.qiniudn.com";
	
	public Mac mac;

	@Override
	public void setUp() throws Exception {
		// get the global config, may be should use static init block
		// to prevent multiple invocation. To do!
		{
			Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
			Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
			Config.RS_HOST = System.getenv("QINIU_RS_HOST");
			this.bucketName = System.getenv("QINIU_TEST_BUCKET");
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}
		// check the config
		{
			assertNotNull(Config.ACCESS_KEY);
			assertNotNull(Config.SECRET_KEY);
			assertNotNull(Config.RS_HOST);
			assertNotNull(bucketName);
		}
		// upload an image
		{
			String uptoken = "";
			try {
				uptoken = new PutPolicy(bucketName).token(mac);
			} catch (AuthException ignore) {
			}
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();
			
			PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash.equals(ret.getHash()));
		}
	}

	public void testImageInfo() throws Exception {
		String url = domain + "/" + key;
		ImageInfoRet ret = ImageInfo.call(url);
		assertTrue(ret.ok());
	}

	public void testImageExif() throws Exception {
		String url = domain + "/" + key;
		ExifRet ret = ImageExif.call(url);
		// logo.png has no exif
		assertTrue(!ret.ok());
	}

	public void testImageView() throws Exception {
		String url = domain + "/" + key;
		{
			ImageView iv = new ImageView();
			iv.mode = 1;
			iv.height = 100;
			CallRet ret = iv.call(url);
			assertTrue(ret.ok());
		}
		{
			ImageView iv = new ImageView();
			iv.mode = 2;
			iv.width = 199;
			CallRet ret = iv.call(url);
			assertTrue(ret.ok());
		}
		{
			ImageView iv = new ImageView();
			iv.mode = 1 ;
			iv.width = 100 ;
			iv.height = 200 ;
			iv.quality = 1 ;
			iv.format = "jpg" ;
			CallRet ret = iv.call(url);
			assertTrue(ret.ok());
		}
	}

	public void testImageViewMakeRequest() {
		String testUrl = "http://iovip.qbox.me/file/xyz==";
		ImageView imgView = new ImageView();
		// case 1
		{
			imgView.mode = 2;
			String url = imgView.makeRequest(testUrl);
			assertEquals(testUrl + "?imageView/2", url);
		}
		// case 2
		{
			imgView.height = 200;
			String url = imgView.makeRequest(testUrl);
			assertEquals(testUrl + "?imageView/2/h/200", url);
		}
		// case 3
		{
			String url = imgView.makeRequest(testUrl);
			assertEquals(testUrl + "?imageView/2/h/200", url);
		}
	}

	@Override
	public void tearDown() {
		// delete the metadata from rs
		// confirms it exists.
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(bucketName, key);
			assertTrue(sr.ok());
			assertTrue(expectedHash.equals(sr.getHash()));
		}

		// deletes it from rs
		{
			RSClient rs = new RSClient(mac);
			CallRet cr = rs.delete(bucketName, key);
			assertTrue(cr.ok());
		}

		// confirms that it's deleted
		{
			RSClient rs = new RSClient(mac);
			Entry sr = rs.stat(bucketName, key);
			assertTrue(!sr.ok());
		}
	}
}