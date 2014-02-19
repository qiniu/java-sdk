package com.qiniu.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import junit.framework.TestCase;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class IOTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	public final String key = "IOTest-key";
	
	public final String key2 = "IOTest-Stream-key";
	
	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";
	public final String expectedHash2 = "Fp9UwOPl9G3HmZsVFkJrMtdwSMp8";

	public String bucketName;
	
	public Mac mac;
	@Override
	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		Config.RS_HOST = System.getenv("QINIU_RS_HOST");
		bucketName = System.getenv("QINIU_TEST_BUCKET");

		assertNotNull(Config.ACCESS_KEY);
		assertNotNull(Config.SECRET_KEY);
		assertNotNull(Config.RS_HOST);
		assertNotNull(bucketName);
		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
	}

	// just upload an image in testdata.
	public void testPut() throws Exception {
		// must start with "x:"
		String xname = "x:test_name";
		String xvalue = "test_value";
		String name = "nonxtest_name";
		String value = "nonxtest_value";
		
		PutPolicy putPolicy = new PutPolicy(bucketName);
		putPolicy.returnBody = "{\"hash\":\"$(etag)\",\"key\":\"$(key)\",\"fsize\":\"$(fsize)\",\""+xname+"\":\"$("+xname+")\",\""+name+"\":\"$("+name+")\"}";
		String uptoken = putPolicy.token(mac);
		
		String dir = System.getProperty("user.dir");
		String localFile = dir + "/testdata/" + "logo.png";

		PutExtra extra = new PutExtra();
		Map<String, String> params = new HashMap<String,String>();
		params.put(xname, xvalue);
		params.put(name, value);
		extra.params = params;
		
		PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
		assertTrue(ret.ok());
		assertTrue(expectedHash.equals(ret.getHash()));
		
		JSONObject jsonObject = new JSONObject(ret.response);
		assertEquals(xvalue, getJsonValue(jsonObject, xname));
		assertEquals(null, getJsonValue(jsonObject, name));
		
		//test stream upload
		{
			String str="Hello,Qiniu";
			ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
			ret = IoApi.Put(uptoken, key2, stream, extra);
			assertTrue(ret.ok());
			assertTrue(expectedHash2.equals(ret.getHash()));

			jsonObject = new JSONObject(ret.response);
			assertEquals(xvalue, getJsonValue(jsonObject, xname));
			assertEquals(null, getJsonValue(jsonObject, name));
		}
	}
	
	private String getJsonValue(JSONObject jsonObject, String name){
		try{
			String value = jsonObject.getString(name);
			// 针对使用returnBody情况
			if("null".equalsIgnoreCase(value)){
				return null;
			}
			return value;
		}catch(Exception e){
			return null;
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
