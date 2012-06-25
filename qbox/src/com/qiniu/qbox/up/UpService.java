package com.qiniu.qbox.up;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.rs.PutFileRet;

public class UpService {
	
	private Client conn;
	
	public UpService(Client conn) {
		this.conn = conn;
	}
	
	public ResumablePutRet makeBlock(long blockSize, byte[] body, long bodyLength) {
		CallRet ret = this.conn.callWithBinary(Config.UP_HOST + "/mkblk/" + String.valueOf(blockSize), "application/octet-stream", body, bodyLength);
		
		return new ResumablePutRet(ret);
	}

	public ResumablePutRet putBlock(long blockSize, String ctx, long offset, byte[] body, long bodyLength) {
		CallRet ret = this.conn.callWithBinary(Config.UP_HOST + "/bput/" + ctx + "/" + String.valueOf(offset), "application/octet-stream", body, bodyLength);
		
		return new ResumablePutRet(ret);
	}
	
	public PutFileRet makeFile(String cmd, String entry, long fsize, String params, String callbackParams, String[] checksums) {
		Base64 encoder = new Base64(true);
		BASE64Decoder decoder = new BASE64Decoder();
		
		if (callbackParams != null && !callbackParams.isEmpty()) {
			params += "/params/" + new String(Base64.encodeBase64(callbackParams.getBytes(), false, false));
		}
		
		String url = Config.UP_HOST + cmd + encoder.encodeBase64String(entry.getBytes()) + "/fsize/" + String.valueOf(fsize) + params;
		
		byte[] body = new byte[20 * checksums.length];
		
		for (int i = 0; i < checksums.length; i++) {
			byte[] buf = encoder.decodeBase64(checksums[i]);
			System.arraycopy(buf,  0, body, i * 20, buf.length);
		}
		
		CallRet ret = this.conn.callWithBinary(url, null, body, body.length);
		
		return new PutFileRet(ret);
	}
	
	public static int blockCount(long fsize) {
		return (int)((fsize + UpService.BLOCK_SIZE - 1) / UpService.BLOCK_SIZE);
	}

	/**
	 * Put a single block.
	 * 
	 * @param in
	 * @param blockIndex
	 * @param blockSize
	 * @param chunkSize
	 * @param retries If fails, how many times should it retry to upload?
	 * @param progress
	 * @param notifier
	 */
	public ResumablePutRet resumablePutBlock(RandomAccessFile file, 
			int blockIndex, long blockSize, long chunkSize, 
			int retryTimes,
			BlockProgress progress, BlockProgressNotifier notifier) {
		
		ResumablePutRet ret = null;

		if (progress.context == null || progress.context.isEmpty()) { // This block has never been uploaded.
			int bodyLength = (int)((blockSize > chunkSize) ? chunkSize : blockSize); // Smaller one.
			
			byte[] body = new byte[bodyLength];
			
			try {
				int readBytes = file.read(body, 0, bodyLength);
				if (readBytes == -1) { // Didn't get anything
					return new ResumablePutRet(new CallRet(400, "Read nothing"));
				}
				
				ret = makeBlock((int)blockSize, body, bodyLength);
				if (!ret.ok()) {
					// Error handling
					return ret;
				}
				
				CRC32 crc32 = new CRC32();
				crc32.update(body, 0, bodyLength);
				
				if (ret.getCrc32() != crc32.getValue()) {
					// Something wrong!
					ret.statusCode = 400;
					return ret;
				}
				
				progress.context = ret.getCtx();
				progress.offset = bodyLength;
				progress.restSize = blockSize - bodyLength;
				
				notifier.notify(blockIndex, progress);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (progress.offset + progress.restSize != blockSize) {
			// Invalid arg.
			return new ResumablePutRet(new CallRet(400, "Invalid arg. File length does not match"));
		}
		
		while (progress.restSize > 0) {
			int bodyLength = (int)((chunkSize < progress.restSize) ? chunkSize : progress.restSize);
			
			byte[] body = new byte[bodyLength];
			int retries = retryTimes;
			
			try {
				int readBytes = file.read(body, 0, bodyLength);
				if (readBytes == -1) { // Didn't get anything
					// TODO: Something wrong!
				}
				
				ret = putBlock(blockSize, progress.context, progress.offset, body, bodyLength);
				if (ret.ok()) {
					
					CRC32 crc32 = new CRC32();
					crc32.update(body, 0, bodyLength);
					
					if (ret.getCrc32() == crc32.getValue()) {
	
						progress.context = ret.getCtx();
						progress.offset += bodyLength;
						progress.restSize -= bodyLength;
						
						notifier.notify(blockIndex, progress);
					
						continue;
					}
				}
				
				// TODO: Retry if fails.
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (ret == null) {
			ret = new ResumablePutRet(new CallRet(400, (String)null));
		}
		return ret;
	}
	
	public static final int BLOCK_SIZE = 1024 * 1024 * 4;
	public static final int CHUNK_SIZE = 1024 * 256;
	public static final int RETRY_TIMES = 3;
	
	public ResumablePutRet resumablePut(RandomAccessFile file, long fsize,
			String[] checksums, BlockProgress[] progresses, 
			ProgressNotifier progressNotifier, BlockProgressNotifier blockProgressNotifier) {
		
		int blockCount = blockCount(fsize);
		
		if (checksums.length != blockCount || progresses.length != blockCount) {
			return new ResumablePutRet(new CallRet(400, "Invalid arg. Unexpected block count."));
		}
		
		for (int i = 0; i < blockCount; i++) {
			if (checksums[i] == null || checksums[i].isEmpty()) {
				int blockIndex = i;
				long blockSize = BLOCK_SIZE;
				if (blockIndex == blockCount - 1) {
					blockSize = fsize - BLOCK_SIZE * blockIndex;
				}
				
				if (progresses[i] == null) {
					progresses[i] = new BlockProgress();
				}
				
				ResumablePutRet ret = resumablePutBlock(file, 
						blockIndex, blockSize, CHUNK_SIZE, 
						RETRY_TIMES, 
						progresses[i], 
						blockProgressNotifier);
				
				if (!ret.ok()) {
					return ret;
				}
				
				checksums[i] = ret.getChecksum();
				
				progressNotifier.notify(i, checksums[i]);
			}
		}
		
		return new ResumablePutRet(new CallRet(200, (String)null));
	}
}
