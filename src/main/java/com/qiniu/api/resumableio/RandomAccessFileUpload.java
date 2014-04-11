package com.qiniu.api.resumableio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.qiniu.api.net.CallRet;
import com.qiniu.api.resumableio.resume.Block;
import com.qiniu.api.resumableio.resume.NonResume;
import com.qiniu.api.resumableio.resume.Resumable;

public class RandomAccessFileUpload extends SliceUpload {
	protected RandomAccessFile file;
	private final Lock fileUploadLock;

	public Class<? extends Resumable> resumeClass;
	// / 断点续传记录实例
	private Resumable resume;
	private String resumeKey;
	private int currentBlockIdx = 0;
	private int blockCount;

	public RandomAccessFileUpload(File file, Authorizer authorizer,
			String key, String mimeType) {
		super(authorizer, key, mimeType);
		try {
			this.contentLength = file.length();
			this.blockCount = (int) ((contentLength + BLOCK_SIZE - 1) / BLOCK_SIZE);
			
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
	protected List<ChunkUploadCallRet> sliceAndUpload() {
		initReusme();
		return super.sliceAndUpload();
	}
	
	@Override
	protected CallRet mkfile(List<ChunkUploadCallRet> rets) {
		CallRet ret = super.mkfile(rets);
		cleanResume();
		return ret;
	}
	

	@Override
	protected boolean hasNext() {
		return contentLength > currentBlockIdx * BLOCK_SIZE;
	}

	@Override
	protected UploadBlock buildNextBlockUpload() throws IOException {
		long start = currentBlockIdx * BLOCK_SIZE;
		int len = (int) Math.min(BLOCK_SIZE, contentLength - start);
		
		return new RandomAccessFileUploadBlock(this, httpClient, host,
				currentBlockIdx, start, len, file, fileUploadLock);
	}


	@Override
	protected ChunkUploadCallRet nextUploadBlock() {
		ChunkUploadCallRet ret = null;
		if (!resume.isBlockDone(currentBlockIdx)) {
			ret = super.nextUploadBlock();
			setResume(ret);
			saveResume();
		}
		currentBlockIdx++;
		return ret;
	}
	
	@Override
	protected String mkCtx(List<ChunkUploadCallRet> rets) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< this.blockCount; i++){
			Block b = resume.getBlock(i);
			sb.append(",").append(b.getCtx());
		}
		String ctx = sb.substring(1);
        return ctx;
	}


	/**
	 * 当指定到断点记录类型为null,或指定的断点记录创建失败时, 使用默认的断点记录类型: 不保存断点记录
	 */
	private void initReusme() {
		
		try {
			if (resumeClass != null) {
				resume = resumeClass.getConstructor(int.class, String.class)
						.newInstance(blockCount, resumeKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 默认为不保存断点记录
		if (resume == null) {
			resume = new NonResume(blockCount, resumeKey);
		}
		try {
			resume.load();
			lastUploadLength = resume.doneCount() * BLOCK_SIZE;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void cleanResume() {
		try {
			if (resume != null) {
				resume.clean();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setResume(ChunkUploadCallRet ret) {
		try {
			if (resume != null && !resume.isBlockDone(ret.getBlockIdx())) {
				Block block = new Block(ret.getBlockIdx(), ret.getCtx(), true);
				resume.set(block);
			}
		} catch (Exception e) {
		}
	}

	private void saveResume() {
		try {
			if (resume != null) {
				resume.save();
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void clean() throws Exception {
		if (file != null) {
			file.close();
		}
	}

}
