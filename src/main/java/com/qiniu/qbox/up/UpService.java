package com.qiniu.qbox.up;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base64;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class UpService {
	
	private Client conn;
	
	public UpService(Client conn) {
		this.conn = conn;
	}
	/**
	 * @param blockSize
	 * @param body 	In fact, the content of the body is the first chunk  
	 * @param bodyLength depends on the file size, if the file size >= chunk size(typically 256k) 
	 * 		  bodyLength equals to chunk size, otherwise equals to file size .
	 * @return
	 */
	
	public ResumablePutRet makeBlock(long blockSize, byte[] body, long bodyLength) {
		CallRet ret = this.conn.callWithBinary(Config.getUpHost() + "/mkblk/" + String.valueOf(blockSize), "application/octet-stream", body, bodyLength);
		
		return new ResumablePutRet(ret);
	}

	/**
	 * put the chunk data into the 
	 * @param blockSize
	 * @param ctx
	 * @param offset
	 * @param body
	 * @param bodyLength
	 * @return
	 */
	public ResumablePutRet putBlock(long blockSize, String ctx, long offset, byte[] body, long bodyLength) {
		CallRet ret = this.conn.callWithBinary(Config.getUpHost() + "/bput/" + ctx + "/" + String.valueOf(offset), "application/octet-stream", body, bodyLength);
		
		return new ResumablePutRet(ret);
	}
	
	public CallRet makeFile(String cmd, String entry, long fsize, String params, String callbackParams, 
			String[] checksums) {
		
		if (callbackParams != null && !callbackParams.isEmpty()) {
			params += "/params/" + new String(Base64.encodeBase64(callbackParams.getBytes(), false, false));
		}
		
		String url = Config.getUpHost() + cmd + Client.urlsafeEncodeString(entry.getBytes()) + "/fsize/" + String.valueOf(fsize) + params;
		
		byte[] body = new byte[20 * checksums.length];
		
		for (int i = 0; i < checksums.length; i++) {
			byte[] buf = Base64.decodeBase64(checksums[i]);
			System.arraycopy(buf,  0, body, i * 20, buf.length);
		}
		CallRet ret = this.conn.callWithBinary(url, null, body, body.length);
		return ret;
	}
	
	public static int blockCount(long fsize) {
		return (int)((fsize + Config.getBlockSize() - 1) / Config.getBlockSize());
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
				file.seek(Config.getBlockSize() * blockIndex);
				int readBytes = file.read(body, 0, bodyLength);
				if (readBytes != bodyLength) { // Didn't get expected content.
					return new ResumablePutRet(new CallRet(400, "Read nothing"));
				}
				// make a new block and put the first chunk data
				ret = makeBlock((int)blockSize, body, bodyLength);
				
				if (!ret.ok()) { 				// statusCode == 200
					return ret; 				// Error handling
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
			
			for (int retries = retryTimes; retries > 0; --retries) {
				try {
					file.seek(blockIndex * Config.getBlockSize() + progress.offset);
					int readBytes = file.read(body, 0, bodyLength);
					if (readBytes != bodyLength) { // Didn't get anything
						return new ResumablePutRet(new CallRet(400, "Read nothing"));
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
							break; // Break to while loop.
						}
					} else if (ret.getStatusCode() == 701) {
						// error occurs, We should roll back to the latest block that uploaded successfully,
						// and put the whole block that currently failed from the first chunk again.
						// For convenient, we just fabricate a progress with empty context.
						progress.context = "" ;
						notifier.notify(blockIndex, progress) ;
					
						return ret ;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return new ResumablePutRet(new CallRet(400, e));
				}
			} // end of for
		} // end of while
		
		if (ret == null) {
			ret = new ResumablePutRet(new CallRet(400, (String)null));
		}
		
		return ret;
	}
	
	/**
	 * This function provides the service that allows users to upload a file in a resumable way.
	 * They don't need to worry about the accidents such as PC'power off, network failure etc...
	 * when uploading a file. 
	 * @param file
	 * @param fsize
	 * @param checksums
	 * @param progresses
	 * @param progressNotifier
	 * @param blockProgressNotifier
	 * @return
	 */
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
				long blockSize = Config.getBlockSize();
				if (blockIndex == blockCount - 1) {
					blockSize = fsize - Config.getBlockSize() * blockIndex;
				}
				
				if (progresses[i] == null) {
					progresses[i] = new BlockProgress();
				}
				
				ResumablePutRet ret = resumablePutBlock(file, 
						blockIndex, blockSize, Config.getPutChuncksize(), 
						Config.getPutRetryTimes(), 
						progresses[i], 
						blockProgressNotifier);

				if (!ret.ok()) {
					return ret;
				}

				checksums[i] = ret.getChecksum() ;
				//checksums[i] = ret.getCtx() ;
				// notify the completion of a block
				progressNotifier.notify(i, checksums[i]);
			}
		}
		
		return new ResumablePutRet(new CallRet(200, (String)null));
	}
}
