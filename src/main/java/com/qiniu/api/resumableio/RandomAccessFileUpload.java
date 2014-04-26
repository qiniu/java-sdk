package com.qiniu.api.resumableio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RandomAccessFileUpload extends SliceUpload {
	protected RandomAccessFile file;
	private final Lock fileUploadLock;

	// / 断点续传记录实例
	private String resumeKey;
	private int currentBlockIdx = 0;

	public RandomAccessFileUpload(File file, String token,
			String key, String mimeType) {
		super(token, key, mimeType);
		try {
			this.contentLength = file.length();
			
			this.file = new RandomAccessFile(file, "r");
			fileUploadLock = new ReentrantLock();
			resumeKey = key;
			if(resumeKey == null){
				resumeKey = file.getAbsolutePath();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	protected boolean hasNext() {
		return contentLength > currentBlockIdx * BLOCK_SIZE;
	}
	

	@Override
	protected UploadBlock buildNextBlockUpload() throws IOException {
		long start = currentBlockIdx * BLOCK_SIZE;
		int len = (int) Math.min(BLOCK_SIZE, contentLength - start);
		
		RandomAccessFileUploadBlock fb = new RandomAccessFileUploadBlock(this, httpClient, host,
				currentBlockIdx, start, len, file, fileUploadLock);
		
		currentBlockIdx++;
		
		return fb;
	}


	@Override
	protected void clean() throws Exception {
		if (file != null) {
			file.close();
		}
	}

}
