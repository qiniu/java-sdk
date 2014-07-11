package com.qiniu.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Http;
import com.qiniu.api.resumableio.ResumeableIoApi;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class ResumeableioTest  extends TestCase{
	public Mac mac;
	public String bucketName;
	private File file = null;

	private String currentKey;
	private String mimeType = null;

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
		mimeType = null;
		currentKey = UUID.randomUUID().toString() + ".txt";
	}

	public void testPut1MFile() throws Exception {
		file = createFile(1, "_1m_");
		mimeType = "test/type1m";
		uploadFile();
	}

	public void testPut1MStream() throws Exception {
		file = createFile(1, "_1m_");
		uploadStream();
	}

	public void testPut4MFile() throws Exception {
		file = createFile(4, "_4m_");
		uploadFile();
	}

	public void testPut4MStream() throws Exception {
		file = createFile(4, "_4m_");
		mimeType = "test/type4m";
		uploadStream();
	}

	public void testPut3MFile() throws Exception {
		file = createFile(3, "_3m_");
		uploadFile();
	}

	public void testPut3MStream() throws Exception {
		file = createFile(3, "_3m_");
		mimeType = "test/type3m";
		uploadStream();
	}


	private void uploadFile() throws AuthException, JSONException{
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		PutRet ret = ResumeableIoApi.put(file, upToken, currentKey, mimeType);
		assertTrue(ret.ok());
		if(mimeType != null){
			JSONObject jsonObject = new JSONObject(ret.response);
			String mt = (String) jsonObject.get("mimeType");
			assertEquals(mimeType, mt);
		}
	}


	private void uploadStream() throws AuthException, JSONException, FileNotFoundException{
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		FileInputStream fis = new FileInputStream(file);
		PutRet ret = ResumeableIoApi.put(fis, upToken, currentKey, mimeType);
		assertTrue(ret.ok());
		if(mimeType != null){
			JSONObject jsonObject = new JSONObject(ret.response);
			String mt = (String) jsonObject.get("mimeType");
			assertEquals(mimeType, mt);
		}
	}
	
	public void testStream() throws Exception {
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		
		HttpEntity en = getHttpEntity("http://qiniuphotos.qiniudn.com/gogopher.jpg");
		PutRet ret = ResumeableIoApi.put(en.getContent(), upToken, currentKey, en.getContentType().getValue(), en.getContentLength());
		
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
		try{
			if(file != null){
				file.delete();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		RSClient rs = new RSClient(mac);
		CallRet cr = rs.delete(bucketName, currentKey);
	}

	/**
	 * @param fileSize M
	 * @return
	 * @throws IOException
	 */
	private File createFile(int fileSize, String temp) throws IOException {
		FileOutputStream fos = null;
		try{
			long size = 1024 * 1024 * fileSize;
			File f = File.createTempFile("qiniu-java-sdk-resumeable" + temp, ".qiniu");
			fos = new FileOutputStream(f);
			byte [] b = getByte();
			long s = 0;
			while(s < size){
				fos.write(b, 0, b.length);
				s += b.length;
			}
			fos.flush();
			return f;
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] getByte(){
		byte [] b = new byte[1024 * 4];
		b[0] = 'A';
		for(int i=1; i < 1024 * 4 ; i++){
			b[i] = 'b';
		}
		b[1024 * 4 - 2] = '\r';
		b[1024 * 4 - 1] = '\n';
		return b;
	}

}
