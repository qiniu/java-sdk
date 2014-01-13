package com.qiniu.api.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

public class IoApi {
	
	public static final String UNDEFINED_KEY = null;
	public static final int NO_CRC32 = 0;
	public static final int AUTO_CRC32 = 1;
	public static final int WITH_CRC32 = 2;
	
	private static PutRet put(String uptoken, String key, File file,
			PutExtra extra) {
		
		if (!file.exists() || !file.canRead()) {
			return new PutRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("token", new StringBody(uptoken));
			AbstractContentBody fileBody = buildFileBody(file, extra);
			requestEntity.addPart("file", fileBody);
			setKey(requestEntity, key);
			if (extra.checkCrc != NO_CRC32) {
				if (extra.crc32 == 0) {
					return new PutRet(new CallRet(400, new Exception("no crc32 specified!")));
				}
				requestEntity.addPart("crc32", new StringBody(extra.crc32 + ""));
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return new PutRet(new CallRet(400, e));
		}

		String url = Config.UP_HOST;
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutRet(ret);
	}
	
	private static FileBody buildFileBody(File file,PutExtra extra){
		if(extra.mimeType != null){
			return new FileBody(file, extra.mimeType);
		}else{
			return new FileBody(file);
		}
	}
	
	private static void setKey(MultipartEntity requestEntity, String key) throws UnsupportedEncodingException{
		if(key != null){
			requestEntity.addPart("key", new StringBody(key,Charset.forName("utf-8")));
		}
	}
	
	private static PutRet putStream(String uptoken, String key, InputStream reader,PutExtra extra) {
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("token", new StringBody(uptoken));
			AbstractContentBody inputBody = buildInputStreamBody(reader, extra, key);
			requestEntity.addPart("file", inputBody);
			setKey(requestEntity, key);
			if (extra.checkCrc != NO_CRC32) {
				if (extra.crc32 == 0) {
					return new PutRet(new CallRet(400, new Exception("no crc32 specified!")));
				}
				requestEntity.addPart("crc32", new StringBody(extra.crc32 + ""));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new PutRet(new CallRet(400, e));
		}

		String url = Config.UP_HOST;
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutRet(ret);
	}
	
	private static InputStreamBody buildInputStreamBody(InputStream reader,PutExtra extra, String key){
		if(extra.mimeType != null){
			return new InputStreamBody(reader, extra.mimeType, key);
		}else{
			return new InputStreamBody(reader, key);
		}
	}
	
	
	public static PutRet Put(String uptoken,String key,InputStream reader,PutExtra extra)
	{		
		PutRet ret = putStream(uptoken,key,reader,extra);
		return ret;
	}
	
	
	public static PutRet putFile(String uptoken, String key, String fileName, PutExtra extra) {
		File file=new File(fileName);
		if (extra.checkCrc == AUTO_CRC32) {
			try {
				extra.crc32 = getCRC32(file);
			} catch (Exception e) {
				return new PutRet(new CallRet(400, e));
			}
		}
		return put(uptoken, key, file, extra);
	}
	
	private static long getCRC32(File file) throws Exception {
		CRC32 crc32 = new CRC32();
		FileInputStream in = null;
		CheckedInputStream checkedInputStream = null;
		long crc = 0;
		try {
			in = new FileInputStream(file);
			checkedInputStream = new CheckedInputStream(in, crc32);
			while (checkedInputStream.read() != -1) {
			}
			crc = crc32.getValue();
		} finally {
			if (in != null) {
				in.close();
				in = null;
			}
			if (checkedInputStream != null) {
				checkedInputStream.close();
				checkedInputStream = null;
			}
		}
		return crc;
	}
}
