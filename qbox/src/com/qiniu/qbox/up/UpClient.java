package com.qiniu.qbox.up;

import java.io.RandomAccessFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.rs.PutFileRet;

public class UpClient {
	
	private static Base64 encoder = new Base64(true);

	public static PutFileRet resumablePutFile(UpService c, String[] checksums, BlockProgress[] progresses, 
			ProgressNotifier progressNotifier, BlockProgressNotifier blockProgressNotifier,
			String entryUri, String mimeType, RandomAccessFile f, long fsize, String customMeta, String callbackParams) {
		
		ResumablePutRet ret = c.resumablePut(f, fsize, checksums, progresses, progressNotifier, blockProgressNotifier);
		if (!ret.ok()) {
			return new PutFileRet(ret);
		}
		
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}
		
		String params = "/mimeType/" + encoder.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			params += "/meta/" + encoder.encodeBase64String(customMeta.getBytes());
		}
		
		PutFileRet putFileRet = c.makeFile("/rs-mkfile/", entryUri, fsize, params, callbackParams, checksums);
		
		return putFileRet;
	}

	private static PutFileRet handleResult(HttpResponse response) {
		
		if (response == null || response.getStatusLine() == null) {
			return new PutFileRet(new CallRet(400, "No response"));
		}
		
		try {
			String responseBody = EntityUtils.toString(response.getEntity());
			
			StatusLine status = response.getStatusLine();
			int statusCode = (status == null) ? 400 : status.getStatusCode();
			
			return new PutFileRet(new CallRet(statusCode, responseBody));
		} catch (Exception e) {
			e.printStackTrace();
			return new PutFileRet(new CallRet(400, e));
		}
	}
}
