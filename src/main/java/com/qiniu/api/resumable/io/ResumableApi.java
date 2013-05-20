package com.qiniu.api.resumable.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.qiniu.api.auth.UptokenClient;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.PutFileRet;

public class ResumableApi {
	
	private static final int DEFAULT_RETRY_TIMES = 3;
	private static final long DEFAULT_CHUNK_SIZE = 256 * 1024; // 256KB
	
	/**
	 * 方法 putFile 来实现断点续传操作，通常需要用户自己实现文件上传进度的持久化工作
	 * 
	 * @param uptoken 由业务服务器颁发的上传凭证，@see (com.qiniu.api.rs.PutPolicy)
	 * @param localFile 所要上传的文件
	 * @param key 上传文件的唯一标识
	 * @param extra 其余参数(see com.qiniu.api.resumable.io.PutExtra)
	 * @return 
	 * 		@see PutFileRet
	 */
	public static PutFileRet putFile(String uptoken, RandomAccessFile localFile, String key, PutExtra extra) {
		UpApi up = new UpApi(new UptokenClient(uptoken));
		
		long fsize = 0;
		try {
			fsize = localFile.length();
		} catch (IOException e) {
			return new PutFileRet(new CallRet(400, e.getMessage()));
		}
		
		fillExtra(extra, fsize);
			
		ResumablePutRet resumablePutRet = up.resumablePut(localFile, fsize, extra);
		
		if (!resumablePutRet.ok()) {
			return new PutFileRet(resumablePutRet);
		}

		CallRet callRet = up.makeFile(resumablePutRet.host, "/rs-mkfile/", key, fsize, extra);
		return new PutFileRet(callRet);
	}

	private static void fillExtra(PutExtra extra, long fsize) {
		int blockCnt = UpApi.blockCount(fsize);
		
		if (extra.progresses == null) {
			extra.progresses = new BlockProgress[blockCnt];
		}
		
		if (extra.checksums == null) {
			extra.checksums = new String[blockCnt];
		}
		
		if (extra.blockProgressNotifier == null) {
			extra.blockProgressNotifier = new BlockProgressNotifier() {
				@Override
				public void notify(int blockIndex, BlockProgress progress) {
					// default empty implementation
				}
			};
		}

		if (extra.progressNotifier == null) {
			extra.progressNotifier = new ProgressNotifier() {
				@Override
				public void notify(int blockIndex, String checksum) {
					// default empty implementation
				}
			};
		}

		if (extra.chunkSize == 0) {
			extra.chunkSize = DEFAULT_CHUNK_SIZE;
		}
		
		if (extra.retryTimes == 0) {
			extra.retryTimes = DEFAULT_RETRY_TIMES;
		}
		
		if (extra.mimeType == null || extra.mimeType.isEmpty()) {
			extra.mimeType = "application/octet-stream";
		}
	}
}
