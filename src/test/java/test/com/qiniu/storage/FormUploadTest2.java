package test.com.qiniu.storage;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.StringMap;

import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import static org.junit.Assert.*;

public class FormUploadTest2 {

	UploadManager uploadManager = new UploadManager(new Configuration());
	
	/**
	 * 测试传入inputStream的表单上传
	 * 检测reqid是否为Null
	 * 检测状态码是否为200
	 */
	@Test
	public void testFormUploadWithInputStream() {
		
		String token = TestConfig.testAuth.uploadToken(TestConfig.testBucket_z0, TestConfig.testBucket_z0, 3600, null);
		System.out.println(token);
		
		try {
			InputStream inputStream = new FileInputStream(TempFile.createFile(11));
			Response response = uploadManager.putWithForm(inputStream, TestConfig.testBucket_z0, token);
			System.out.println(response.reqId);
			System.out.println(response.statusCode);
			System.out.println(response.bodyString());
			assertNotNull(response.reqId);
			assertEquals(200, response.statusCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 测试传入inputStream的表单上传
	 * 检测reqid是否为Null
	 * 检测状态码是否为614
	 */
	@Test
	public void testFormUploadWithInputStreamWithPolicy() {
		
		StringMap putPolicy = new StringMap();
		putPolicy.put("insertOnly", 1);
		String token = TestConfig.testAuth.uploadToken(TestConfig.testBucket_z0, TestConfig.testBucket_z0, 3600, putPolicy);
		System.out.println(token);
		
		try {
			InputStream inputStream = new FileInputStream(TempFile.createFile(11));
			uploadManager.putWithForm(inputStream, TestConfig.testBucket_z0, token);
		} catch (Exception e) {
			if (e instanceof QiniuException) {
				QiniuException ex = (QiniuException) e;
				System.out.println(ex.response.reqId);
				System.out.println(ex.response.statusCode);
				assertNotNull(ex.response.reqId);
				assertEquals(614, ex.response.statusCode);
			}
		}
	}
}
