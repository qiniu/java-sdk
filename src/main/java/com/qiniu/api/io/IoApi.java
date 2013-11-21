package com.qiniu.api.io;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
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

import java.nio.charset.Charset;

public class IoApi {
	public static final int NO_CRC32 = 0;
	public static final int AUTO_CRC32 = 1;
	public static final int WITH_CRC32 = 2;

	public static PutRet put(String uptoken, String key, String fileName,
			PutExtra extra) {
		File file = new File(fileName);
		return put(uptoken, key, file, extra);
	}

	public static PutRet put(String uptoken, String key, File file,
			PutExtra extra) {
		if (!file.exists() || !file.canRead()) {
			return new PutRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}
		if (extra.checkCrc == AUTO_CRC32) {
			try {
				extra.crc32 = getCRC32(file);
			} catch (Exception e) {
				return new PutRet(new CallRet(400, e));
			}
		}
		FileBody fileBody = new FileBody(file);
		return put(uptoken, key, fileBody, extra);
	}

	private static PutRet put(String uptoken, String key,
			AbstractContentBody fileBody, PutExtra extra) {
		MultipartEntity requestEntity = new MultipartEntity();
		try {
			if (extra.checkCrc != NO_CRC32) {
				if (extra.crc32 == 0) {
					return new PutRet(new CallRet(400, new Exception(
							"no crc32 specified!")));
				}
				requestEntity
						.addPart("crc32", new StringBody(extra.crc32 + ""));
			}
			requestEntity.addPart("token", new StringBody(uptoken));
			requestEntity.addPart("file", fileBody);
			if (!isNullOrEmpty(key)) {
				requestEntity.addPart("key",
						new StringBody(key, Charset.forName("utf-8")));
			}
		} catch (Exception e) {
			return new PutRet(new CallRet(400, e));
		}

		String url = Config.UP_HOST;
		CallRet ret = new Client().callWithMultiPart(url, requestEntity);
		return new PutRet(ret);
	}

	private static boolean isNullOrEmpty(String str) {
		return null == str || str.trim().isEmpty();
	}

	public static PutRet put(String uptoken, String key, InputStream reader,
			PutExtra extra) {
		// inputBody's fileName can not be null or empty. why?
		String fileName = isNullOrEmpty(key) ? "?" : key;
		InputStreamBody inputBody = new InputStreamBody(reader, fileName);
		return put(uptoken, key, inputBody, extra);
	}

	@Deprecated
	public static PutRet Put(String uptoken, String key, InputStream reader,
			PutExtra extra) {
		return put(uptoken, key, reader, extra);
	}

	@Deprecated
	public static PutRet putFile(String uptoken, String key, String fileName,
			PutExtra extra) {
		return put(uptoken, key, fileName, extra);
	}

	private static long getCRC32(File file) throws Exception {
		CRC32 crc32 = new CRC32();
		FileInputStream in = null;
		CheckedInputStream checkedInputStream = null;
		long crc = 0;
		try {
			in = new FileInputStream(file);
			checkedInputStream = new CheckedInputStream(in, crc32);
			byte[] buffer = new byte[1024];
			while (checkedInputStream.read(buffer) != -1) {
			}
			crc = crc32.getValue();
		} finally {
			if (checkedInputStream != null) {
				checkedInputStream.close();
				checkedInputStream = null;
			}
			if (in != null) {
				in.close();
				in = null;
			}
		}
		return crc;
	}

}
