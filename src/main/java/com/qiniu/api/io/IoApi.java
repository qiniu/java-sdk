package com.qiniu.api.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.rs.PutPolicy;

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

	public static PutRet putFile(String uptoken, String key, File file, PutExtra extra) {
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
	
	public static void main(String[] args) throws Exception {
		/*File file = new File("/home/wangjinlei/mm.jpg");
		System.out.println(getCRC32(file));*/

		Config.ACCESS_KEY = "Ilt-hivILN2qJ0npYd8rEwPBLUjcl0zMvmF6XAMS";
		Config.SECRET_KEY = "g_HKK_hHAsatWAbqY7yCkfA_0xIAtu3xWwi8DUpH";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		PutPolicy putPolicy = new PutPolicy("wangjinlei");
		putPolicy.expires = 3600;
		String uptoken = putPolicy.token(mac);
		
		String key="wjl.mm.jpg";
		File file = new File("/home/wangjinlei/mm.jpg");
		PutExtra extra = new PutExtra();
		extra.checkCrc = 0;
		PutRet ret = IoApi.put(uptoken, key, file, extra);
		System.out.println(ret.ok());
		System.out.println(ret.getHash());
		System.out.println(ret);
	}
}
