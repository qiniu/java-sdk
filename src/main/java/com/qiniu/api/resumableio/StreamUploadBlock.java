package com.qiniu.api.resumableio;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;

public class StreamUploadBlock extends UploadBlock {
	private StreamSliceUpload.ByteRef buffer;

	StreamUploadBlock(SliceUpload sliceUpload, HttpClient httpClient,
			String host, int blockIdx, long offset, int len,
			StreamSliceUpload.ByteRef br) {
		super(sliceUpload, httpClient, host, blockIdx, offset, len);
		this.buffer = br;
	}

	@Override
	protected HttpEntity buildHttpEntity(int start, int len) {
		byte[] data = copy2New(start, len);
		ByteArrayEntity bae = new ByteArrayEntity(data);
		bae.setContentType("application/octet-stream");
		return bae;
	}

	@Override
	protected void clean() {
		if (buffer != null) {
			buffer.clean();
		}
		buffer = null;
	}

	private byte[] copy2New(int start, int len) {
		byte[] b = new byte[len];
		System.arraycopy(this.buffer.getBuf(), start, b, 0, len);
		return b;
	}

	@Override
	protected long buildCrc32(int start, int len) {
		byte[] data = copy2New(start, len);
		return Util.crc32(data);
	}

}
