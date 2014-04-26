package com.qiniu.api.resumableio;

import java.io.File;
import java.io.InputStream;

import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.Http;

public class ResumeableIoApi {

	public static RandomAccessFileUpload upload(File file, String upToken) {
		return upload(file, upToken, null, null);
	}

	public static RandomAccessFileUpload upload(File file, String upToken,
			String key) {
		return upload(file, upToken, key, null);
	}


	/**
	 * @param file
	 * @param upToken
	 * @param key
	 * @param mimeType
	 * @param resumeClass 
	 * @return
	 * 
	 * 获得upload后执行execute方法，返回 CallRet
	 * upload。execute()
	 * 
	 */
	public static RandomAccessFileUpload upload(File file, String upToken,
			String key, String mimeType) {
		RandomAccessFileUpload upload = new RandomAccessFileUpload(file, upToken, key, mimeType);

		upload.httpClient = Http.getClient();

		return upload;
	}
	
	
	public static StreamSliceUpload upload(InputStream is, String upToken) {
		return upload(is, upToken, null, null);
	}
	
	
	public static StreamSliceUpload upload(InputStream is, String upToken,
			String key) {
		return upload(is, upToken, key, null);
	}
	
	
	public static StreamSliceUpload upload(InputStream is, String upToken,
			String key, String mimeType) {
		return upload(is, upToken, key, mimeType, -1);
	}
	
	public static StreamSliceUpload upload(InputStream is, String upToken, long streamLength) {
		return upload(is, upToken, null, null, streamLength);
	}
	
	
	public static StreamSliceUpload upload(InputStream is, String upToken,
			String key, long streamLength) {
		return upload(is, upToken, key, null, streamLength);
	}
	
	/**
	 * @param is
	 * @param upToken
	 * @param key
	 * @param mimeType
	 * @param streamLength
	 * @return
	 * 
	 * 获得upload后执行execute方法，返回 CallRet
	 * upload。execute()
	 */
	public static StreamSliceUpload upload(InputStream is, String upToken,
			String key, String mimeType, long streamLength) {
		StreamSliceUpload upload = new StreamSliceUpload(is, upToken, key, mimeType, streamLength);

		upload.httpClient = Http.getClient();

		return upload;
	}
	
	/**
	 * @param file 具体文件，不能为文件夹
	 * @param upToken
	 * @param key
	 * @param mimeType
	 * @return
	 */
	public static PutRet put(File file, String upToken,
			String key, String mimeType){
		return upload(file, upToken, key, mimeType).execute();
	}
	
	public static PutRet put(File file, String upToken,
			String key){
		return put(file, upToken, key, null);
	}
	
	/**
	 * @param is
	 * @param upToken
	 * @param key
	 * @param mimeType
	 * @param streamLength 若不知具体大小，可传 -1
	 * @return
	 */
	public static PutRet put(InputStream is, String upToken,
			String key, String mimeType, long streamLength){
		return upload(is, upToken, key, mimeType, streamLength).execute();
	}
	
	public static PutRet put(InputStream is, String upToken,
			String key, String mimeType){
		return upload(is, upToken, key, mimeType).execute();
	}
	
	public static PutRet put(InputStream is, String upToken,
			String key){
		return upload(is, upToken, key, null).execute();
	}

}
