package com.qiniu.api.io;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

public class IoApi {
	
	public static final String UNDEFINED_KEY = "?";
	public static final int NO_CRC32 = 0;
	public static final int AUTO_CRC32 = 1;
	public static final int WITH_CRC32 = 2;
	
	private static PutRet put(String uptoken, String key, File file,
			PutExtra extra) {
		
		if (!file.exists() || !file.canRead()) {
			return new PutRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}
		if (key == null) {
			key = UNDEFINED_KEY;
		}
		
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("token", new StringBody(uptoken));
			FileBody fileBody = new FileBody(file);
			
			requestEntity.addPart("file", fileBody);
			requestEntity.addPart("key", new StringBody(key));
			
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
	
	private static PutRet putStream(String uptoken, String key, InputStream reader,PutExtra extra) {
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			requestEntity.addPart("token", new StringBody(uptoken));
			InputStreamBody inputBody= new InputStreamBody(reader,key);
			requestEntity.addPart("file", inputBody);
			requestEntity.addPart("key", new StringBody(key));
			
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
	
	
	public static PutRet Put(String uptoken,String key,InputStream reader,PutExtra extra)
	{		
		if (key == null) {
			key = UNDEFINED_KEY;
		}
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
