package com.qiniu.api.resumableio;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import com.qiniu.api.config.Config;


public abstract class UploadBlock  {
    public static int CHUNK_SIZE = 1024 * 256;
    public static int FIRST_CHUNK = 1024 * 256;
    public static int triedTimes = 3;

    protected HttpClient httpClient;
    protected String orginHost;

    /// 块编号
    protected int blockIdx;
    /// 此块开始的位置
    protected long offset;
    /// 此块的长度
    protected int length;
    protected SliceUpload sliceUpload;
    
    public UploadBlock(SliceUpload sliceUpload, HttpClient httpClient,
            String host, int blockIdx, long offset, int len){
    	this.sliceUpload = sliceUpload;
		this.httpClient = httpClient;
		this.orginHost = host;
		this.blockIdx = blockIdx;
        this.offset = offset;
        this.length = len;
    }

    public ChunkUploadCallRet execute() throws Exception {
        int flen = Math.min(length, FIRST_CHUNK);
        ChunkUploadCallRet ret = uploadMkblk(flen, 0);
        checkChunkCallRet(ret);
        if (length > FIRST_CHUNK) {
            int count = (length - FIRST_CHUNK + CHUNK_SIZE - 1) / CHUNK_SIZE;
            for(int i = 0; i < count; i++) {
                int start = CHUNK_SIZE * i + FIRST_CHUNK;
                int len = Math.min(length - start, CHUNK_SIZE);
                ret = uploadChunk(ret, start, len, 0);
                checkChunkCallRet(ret);
            }
        }
        clean();
        return ret;
    }

    private  void checkChunkCallRet(ChunkUploadCallRet ret) throws Exception{
        if(ret == null || !ret.ok()){
            clean();
            if(ret.exception != null){
            	throw ret.exception;
            }
            throw new Exception();
        }
    }

    private ChunkUploadCallRet uploadMkblk(int len, int time) {
        String url = getMkblkUrl();
        return upload(url, 0, len, time);
    }

    private ChunkUploadCallRet uploadChunk(ChunkUploadCallRet ret, int start, int len, int time) {
            String url = getBlkUrl(ret);
            return upload(url, start, len, time);
    }

    private ChunkUploadCallRet upload(String url, int start,int len, int time)  {
    	try {    
	    	HttpPost post = Util.buildUpPost(url, sliceUpload.token);
            post.setEntity(buildHttpEntity(start, len));
            HttpResponse response = httpClient.execute(post);
            ChunkUploadCallRet ret = new ChunkUploadCallRet(Util.handleResult(response));
            
            return checkAndRetryUpload(url, start, len, time, ret);
		} catch (Exception e) {
			return new ChunkUploadCallRet(Config.ERROR_CODE, e);
		}
    }

	private ChunkUploadCallRet checkAndRetryUpload(String url, int start,
			int len, int time, ChunkUploadCallRet ret) {
		if(!ret.ok()){
			// 406	上传的数据 CRC32 校验错。； 701	上传数据块校验出错。； 服务端失败
			if((ret.statusCode == 406 || ret.statusCode == 701 || ret.statusCode / 100 == 5) && time < triedTimes){
				return upload(url, start, len, time + 1);
			}else{
				return ret;
			}
		}
		else{
			long crc32 = buildCrc32(start, len);
			// 上传的数据 CRC32 校验错。
			if(ret.getCrc32() != crc32){
				if(time < triedTimes){
					return upload(url, start, len, time + 1);
		    	}else{
		    		// 406	上传的数据 CRC32 校验错。
		    		return new ChunkUploadCallRet(406, "inner block's crc32 do not match.");
		    	}
			}else{
				sliceUpload.addSuccessLength(len);
			    return ret;
			}
		}
	}
    

    private String getMkblkUrl() {
        String url = orginHost + "/mkblk/" + length;
        return url;
    }

    private String getBlkUrl(ChunkUploadCallRet ret) {
        String url = ret.getHost() + "/bput/" + ret.getCtx() + "/" + ret.getOffset();
        return url;
    }
    

    protected abstract HttpEntity buildHttpEntity(int start, int len);
    
    protected abstract long buildCrc32(int start, int len);

    protected abstract void clean();

}
