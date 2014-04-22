package com.qiniu.api.resumableio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.qiniu.api.config.Config;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.EncodeUtils;

public abstract class SliceUpload {
	// 七牛服务器要求固定为4M
	protected static final int BLOCK_SIZE = 1024 * 1024 * 4;

	/** 错误后尝试次数 */
	public static int triedTimes = 3;

	public String host = Config.UP_HOST;
	public HttpClient httpClient;

	protected String token;

	protected String key;
	protected String mimeType;

	protected long contentLength = -1;
	protected long lastUploadLength = 0;
	protected long currentUploadLength = 0;

	public SliceUpload(String token, String key,
			String mimeType) {
		this.token = token;
		this.key = key;
		this.mimeType = mimeType;
	}

	/**
	 * 执行上传
	 * @return
	 */
	public PutRet execute() {
		List<ChunkUploadCallRet> rets = sliceAndUpload();
		CallRet ret = mkfile(rets);
		doClean();
		return new PutRet(ret);
	}

	protected List<ChunkUploadCallRet> sliceAndUpload() {
		List<ChunkUploadCallRet> rets = new ArrayList<ChunkUploadCallRet>();
		while(hasNext()) {
			ChunkUploadCallRet ret = nextUploadBlock();
			rets.add(ret);
		}
		return rets;
	}

	protected ChunkUploadCallRet nextUploadBlock() {
		try {
			UploadBlock upload = buildNextBlockUpload();
			ChunkUploadCallRet ret = upload.execute();
			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract boolean hasNext();
	
	protected abstract  UploadBlock buildNextBlockUpload() throws IOException;

	protected CallRet mkfile(List<ChunkUploadCallRet> rets) {
		String ctx = mkCtx(rets);
		return mkfile(ctx, 0);
	}

	protected String mkCtx(List<ChunkUploadCallRet> rets) {
		StringBuffer sb = new StringBuffer();
		for (ChunkUploadCallRet ret : rets) {
			sb.append(",").append(ret.getCtx());
		}
		return sb.substring(1);
	}

	private CallRet mkfile(String ctx, int time) {
		try {
			String url = buildMkfileUrl();
			HttpPost post = Util.buildUpPost(url, token);
			post.setEntity(new StringEntity(ctx));
			HttpResponse response = httpClient.execute(post);
			CallRet ret = Util.handleResult(response);
			// 406 上传的数据 CRC32 校验错。； 701 上传数据块校验出错。； 服务端失败
			if ((ret.statusCode == 406 || ret.statusCode == 701 || ret.statusCode / 100 == 5)
					&& time < triedTimes) {
				return mkfile(ctx, time + 1);
			}
			return ret;
		} catch (Exception e) {
			// 网络异常等，到最后一步，重试是值得的。
			if (time < triedTimes) {
				return mkfile(ctx, time + 1);
			}
			throw new RuntimeException(e);
		}
	}

	private String buildMkfileUrl() {
		String url = host + "/mkfile/" + (currentUploadLength + lastUploadLength);
		if (null != key) {
			url += "/key/" + EncodeUtils.urlsafeEncode(key);
		}
		if (null != mimeType && !(mimeType.trim().length() == 0)) {
			url += "/mimeType/" + EncodeUtils.urlsafeEncode(mimeType);
		}
		return url;
	}

	private void doClean(){
		try{
			clean();
		}catch(Exception e){
			
		}
	}

	protected abstract void clean() throws Exception;

	protected void addSuccessLength(long size) {
		currentUploadLength += size;
	}

	/**
	 * @return 上传资源总大小
	 * 流上传时，可能返回 -1或0
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * @return 上次上传的文件大小。
	 * 在文件断点续传中用到。其它上传返回 0；
	 */
	public long getLastUploadLength() {
		return lastUploadLength;
	}

	/**
	 * @return 本次已上传的文件大小
	 */
	public long getCurrentUploadLength() {
		return currentUploadLength;
	}

}
