package com.qiniu.api.resumableio;

import java.io.IOException;
import java.io.InputStream;

public class StreamSliceUpload extends SliceUpload{
    protected InputStream is;
    private int currentBlockIdx = 0;
    
    public StreamSliceUpload(InputStream is,
			String token, String key, String mimeType) {
		this(is, token, key, mimeType, -1);
	}

	/**
	 * @param is
	 * @param authorizer
	 * @param key
	 * @param mimeType
	 * @param totalLength 长度未知可传 -1；
	 */
	public StreamSliceUpload(InputStream is,
			String token, String key, String mimeType, long totalLength) {
		super(token, key, mimeType);
		this.contentLength = totalLength;
		this.is = is;
	}


	@Override
	protected boolean hasNext() {
		try {
			// 部分流 is.available() == 0，此时通过设定的内容长度判断，
			return is != null && (is.available() > 0 || currentBlockIdx * BLOCK_SIZE < contentLength);
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	protected UploadBlock buildNextBlockUpload() throws IOException {
		long left = is.available();
		left = left > 0 ? left : (contentLength - currentBlockIdx * BLOCK_SIZE);
		long start = currentBlockIdx * BLOCK_SIZE;
		int len = (int) Math.min(BLOCK_SIZE, left);

		 ByteRef br = new ByteRef();
        byte[] b = new byte[len];
        is.read(b, 0, len);
        br.setBuf(b);
        StreamUploadBlock bu = new StreamUploadBlock(this, httpClient, host, currentBlockIdx, start, len, br);
        
        currentBlockIdx++;
        return bu;
	}
	
    protected UploadBlock buildBlockUpload(int blockIdx, long start, int len) throws IOException {
        ByteRef br = new ByteRef();
        byte[] b = new byte[len];
        is.read(b, 0, len);
        br.setBuf(b);
        StreamUploadBlock bu = new StreamUploadBlock(this, httpClient, host, blockIdx, start, len, br);
        return bu;
    }

    @Override
    protected void clean() throws Exception {
        if(is != null){
           is.close();
        }
    }


	protected class ByteRef {
		private byte[] buf;

		public byte[] getBuf() {
			return buf;
		}

		protected void setBuf(byte[] buf) {
			this.buf = buf;
		}

		public void clean() {
			this.buf = null;
		}

		public boolean isEmpty() {
			return buf == null;
		}
	}


}
