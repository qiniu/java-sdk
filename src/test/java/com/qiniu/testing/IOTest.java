package com.qiniu.testing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.Http;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class IOTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	private String key = "java-IOTest-key";

	private String key2 = "java-IOTest-Stream-key";

	private final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";
	private final String expectedHash2 = "Fp9UwOPl9G3HmZsVFkJrMtdwSMp8";

	private String bucketName;

	private Mac mac;
	@Override
	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		bucketName = System.getenv("QINIU_TEST_BUCKET");

		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);

		key = UUID.randomUUID().toString();
		key2 = UUID.randomUUID().toString();
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
	
	public void testNoLengthStream() throws Exception {
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		
		HttpEntity en = getHttpEntity("http://qiniuphotos.qiniudn.com/gogopher.jpg");
		
		class MyInputStream extends InputStream{
			InputStream in;
			MyInputStream(InputStream is){
				this.in = is;
			}
			
			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				return in.read();
			}
			
			 public int available() throws IOException {
				 throw new IOException();
			 }
			 
			 public void close() throws IOException {
				 in.close();
			 }
			
		}
		

		
		PutRet ret = IoApi.Put(upToken, key, new MyInputStream(en.getContent()), null);
		
		assertTrue(ret.ok());
	}
	
	public void testSetLengthStream() throws Exception {
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		
		HttpEntity en = getHttpEntity("http://qiniuphotos.qiniudn.com/gogopher.jpg");
		PutExtra extra = new PutExtra();
		extra.mimeType = en.getContentType().getValue();
		PutRet ret = IoApi.Put(upToken, key, en.getContent(), extra, en.getContentLength());
		
		assertTrue(ret.ok());
	}

	private HttpEntity getHttpEntity(String url) throws ClientProtocolException, IOException{
		HttpClient client = Http.getClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse res = client.execute(httpget);
		return res.getEntity();
	}


	@Override
	public void tearDown() {
		RSClient rs = new RSClient(mac);
		try{
			rs.delete(bucketName, key2);
		}catch(Exception e){

		}

		try{
			rs.delete(bucketName, key);
		}catch(Exception e){

		}
	}
}
