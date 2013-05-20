package com.qiniu.api.resumable.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base64;

import com.qiniu.api.auth.UptokenClient;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.EncodeUtils;

public class UpApi {

	private static final int BLOCK_SIZE = 4 * 1024 * 1024; // 4MB
	private static final int INVALID_CTX = 701;

	private UptokenClient conn;

	public UpApi(UptokenClient conn) {
		this.conn = conn;
	}

	public ResumablePutRet makeBlock(String upHost, long blockSize, byte[] body) {
		CallRet ret = this.conn.callWithBinary(
				upHost + "/mkblk/" + String.valueOf(blockSize),
				"application/octet-stream", body);
		ResumablePutRet putRet = new ResumablePutRet(ret);
		return putRet;
	}

	public ResumablePutRet putBlock(String upHost, long blockSize, String ctx,
			long offset, byte[] body) {
		CallRet ret = this.conn.callWithBinary(upHost + "/bput/" + ctx + "/"
				+ String.valueOf(offset), "application/octet-stream", body);

		return new ResumablePutRet(ret);
	}

	public CallRet makeFile(String upHost, String cmd, String key, long fsize,
			PutExtra extra) {
		String entryURI = extra.bucketName + ":" + key;
		String params = "/mimeType/" + Base64.encodeBase64String(extra.mimeType.getBytes());

		if (extra.customMeta != null && !extra.customMeta.isEmpty()) {
			params += "/meta/" + Base64.encodeBase64String(extra.customMeta.getBytes());
		}

		if (extra.callbackParams != null && !extra.callbackParams.isEmpty()) {
			params += "/params/" + new String(Base64.encodeBase64(
							extra.callbackParams.getBytes(), false, false));
		}
		String url = upHost + cmd
				+ EncodeUtils.urlsafeEncodeString(entryURI.getBytes())
				+ "/fsize/" + String.valueOf(fsize) + params;

		StringBuilder body = new StringBuilder();
		for (int i = 0; i < extra.progresses.length; i++) {
			body.append(extra.progresses[i].context);
			body.append(",");
		}
		// remove the last ","
		if (body.length() > 0) {
			body.deleteCharAt(body.length() - 1);
		}

		CallRet ret = this.conn.callWithBinary(url, "text/plain", body
				.toString().getBytes());

		return ret;
	}

	public static int blockCount(long fsize) {
		return (int) ((fsize + BLOCK_SIZE - 1) / BLOCK_SIZE);
	}

	public ResumablePutRet resumablePut(RandomAccessFile file, long fsize,
			PutExtra extra) {

		String upHost = Config.UP_HOST;
		// get upHost from the progress file.
		if (extra.progresses[0] != null) {
		    upHost = extra.progresses[0].host;
		}
		ResumablePutRet ret = null;
		int blockCount = blockCount(fsize);

		if (extra.checksums.length != blockCount || extra.progresses.length != blockCount) {
			return new ResumablePutRet(new CallRet(400, "Invalid arg. Unexpected block count."));
		}

		for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
			if (extra.checksums[blockIndex] == null || extra.checksums[blockIndex].isEmpty()) {
				
				long blockSize = BLOCK_SIZE;
				if (blockIndex == blockCount - 1) {
					blockSize = fsize - BLOCK_SIZE * blockIndex;
				}

				if (extra.progresses[blockIndex] == null) {
					extra.progresses[blockIndex] = new BlockProgress();
				}

				ret = resumablePutBlock(upHost, file, blockIndex, blockSize,
						extra.progresses[blockIndex], extra);
				// just use the host returned by the first block's mkblock operation
				if (blockIndex == 0) {
					upHost = ret.host;
				}
				if (!ret.ok()) {
					return ret;
				}

				extra.checksums[blockIndex] = ret.checksum;
				extra.progressNotifier.notify(blockIndex, extra.checksums[blockIndex]);
			}
		}
		// all the blocks have been uploaded successfully.
		ret = new ResumablePutRet(new CallRet(200, (String) null));
		ret.host = upHost;
		return ret;
	}

	private boolean checkCrc32(byte[] content, long targetCrc) {
		CRC32 crc32 = new CRC32();
		crc32.update(content, 0, content.length);

		return crc32.getValue() == targetCrc;
	}

	private void saveProgress(BlockProgress progress, String context,
			long offset, long restSize, String upHost) {
		progress.context = context;
		progress.offset = offset;
		progress.restSize = restSize;
		progress.host = upHost;
	}
	
	private long getBodyLength(long currentSize, long chunkSize) {
		if (currentSize > chunkSize) {
			return chunkSize;
		} else {
			return currentSize;
		}
	}
	
	public ResumablePutRet resumablePutBlock(String upHost,
			RandomAccessFile file, int blockIndex, long blockSize,
			BlockProgress progress, PutExtra extra) {

		ResumablePutRet ret = null;
		// This block has never been uploaded.
		if (progress.context == null || progress.context.isEmpty()) {
			int bodyLength = (int)getBodyLength(blockSize, extra.chunkSize);
			byte[] body = new byte[bodyLength];

			try {
				file.seek(BLOCK_SIZE * blockIndex);
				int readBytes = file.read(body, 0, bodyLength);
				if (readBytes != bodyLength) { // Didn't get expected content.
					return new ResumablePutRet(new CallRet(400, "Read nothing"));
				}

				ret = makeBlock(upHost, (int) blockSize, body);
				if (blockIndex == 0) {
					upHost = ret.host;
				}
				
				if (!ret.ok()) {
					return ret;
				}

				if (!checkCrc32(body, ret.crc32)) {
					ret.statusCode = 400;
					return ret;
				}

				saveProgress(progress, ret.ctx, bodyLength, blockSize-bodyLength, upHost);
				extra.blockProgressNotifier.notify(blockIndex, progress);
			} catch (IOException e) {
				e.printStackTrace();
				return new ResumablePutRet(new CallRet(400, e));
			}
		} else if (progress.offset + progress.restSize != blockSize) {
			return new ResumablePutRet(new CallRet(400, "Invalid arg. File length does not match"));
		}

		while (progress.restSize > 0) {
			int bodyLength = (int)getBodyLength(progress.restSize, extra.chunkSize);

			byte[] body = new byte[bodyLength];

			for (int retries = extra.retryTimes; retries > 0; --retries) {
				try {
					file.seek(blockIndex * BLOCK_SIZE + progress.offset);
					int readBytes = file.read(body, 0, bodyLength);
					if (readBytes != bodyLength) {
						return new ResumablePutRet(new CallRet(400, "Read nothing"));
					}

					ret = putBlock(upHost, blockSize, progress.context,
							progress.offset, body);
					
					if (ret.ok()) {
						if (!checkCrc32(body, ret.crc32)) {
							ret.statusCode = 400;
							return ret;
						}
						
						saveProgress(progress, ret.ctx, progress.offset+bodyLength, progress.restSize-bodyLength, upHost);
						
						extra.blockProgressNotifier.notify(blockIndex,progress);
						break; // Break to while loop.
					} else if (ret.getStatusCode() == INVALID_CTX) {
						// invalid context
						progress.context = "";
						extra.blockProgressNotifier.notify(blockIndex, progress);
						return ret;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return new ResumablePutRet(new CallRet(400, e));
				}
			}
		}
		
		if (ret == null) {
			ret = new ResumablePutRet(new CallRet(400, (String) null));
		}
		return ret;
	}
	
}