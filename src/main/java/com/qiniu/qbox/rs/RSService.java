package com.qiniu.qbox.rs;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class RSService {

	private Client conn;
	private String bucketName;

	public RSService(Client conn, String bucketName) {
		this.conn = conn;
		this.bucketName = bucketName;
	}

	/**
	 *  func PutAuth() => PutAuthRet
	 *  上传授权（生成一个短期有效的可匿名上传URL）
	 */
	public PutAuthRet putAuth() throws Exception {
		CallRet callRet = conn.call(Config.IO_HOST + "/put-auth/");
		return new PutAuthRet(callRet);
	}

	/**
	 * func Put(key, mimeType, entity, customMeta string)
	 * 上传一个文件
	 * @throws Exception 
	 */
	public PutFileRet put(
		String key, String mimeType, AbstractHttpEntity entity, String customMeta) throws Exception {

		String entryURI = this.bucketName + ":" + key;
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}
		String url = Config.IO_HOST + "/rs-put/" + Client.urlsafeEncode(entryURI) +
					"/mimeType/" + Client.urlsafeEncode(mimeType);
		if (customMeta != null && !customMeta.isEmpty()) {
			url += "/meta/" + Client.urlsafeEncode(customMeta);
		}
		CallRet callRet = conn.callWithBinary(url, entity);
		return new PutFileRet(callRet);
	}

	/**
	 * func PutFile(key, mimeType, localFile, customMeta string)
	 * 上传一个文件
	 * @throws Exception
	 */
	public PutFileRet putFile(
		String key, String mimeType, String localFile, String customMeta) throws Exception {

		File file = new File(localFile);
		FileEntity entity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);
		return put(key, mimeType, entity, customMeta);
	}

	/**
	 * func Get(key string, attName string) => (data GetRet, code int, err Error)
	 * 下载授权（生成一个短期有效的可匿名下载URL）
	 */
	public GetRet get(String key, String attName) throws Exception {
		String entryURI = this.bucketName + ":" + key;
		String url = Config.RS_HOST + "/get/" + Client.urlsafeEncode(entryURI) + "/attName/"
				+ Client.urlsafeEncode(attName);
		CallRet callRet = conn.call(url);
		return new GetRet(callRet);
	}

	public GetRet getWithExpires(String key, String attName, long expires) 
			throws Exception {
	    String entryURI = this.bucketName + ":" + key;
		String url = Config.RS_HOST + "/get/" + Client.urlsafeEncode(entryURI)
				+ "/attName/" + Client.urlsafeEncode(attName) + "/expires/"
				+ String.valueOf(expires);
	    CallRet callRet = conn.call(url);
	    return new GetRet(callRet);
	}
	
	/**
	 * func GetIfNotModified(key string, attName string, base string) => (data GetRet, code int, err Error)
	 * 下载授权（生成一个短期有效的可匿名下载URL），如果服务端文件没被人修改的话（用于断点续传）
	 */
	public GetRet getIfNotModified(String key, String attName, String base) throws Exception {
		String entryURI = this.bucketName + ":" + key;
		String url = Config.RS_HOST + "/get/" + Client.urlsafeEncode(entryURI) + "/attName/"
				+ Client.urlsafeEncode(attName) + "/base/" + base;
		CallRet callRet = conn.call(url);
		return new GetRet(callRet);
	}

	/**
	 * func Stat(key string) => (entry Entry, code int, err Error)
	 * 取资源属性
	 */
	public StatRet stat(String key) throws Exception {
		String entryURI = this.bucketName + ":" + key;
		String url = Config.RS_HOST + "/stat/" + Client.urlsafeEncode(entryURI);
		CallRet callRet = conn.call(url);
		return new StatRet(callRet);
	}

	/**
	 * func Publish(domain string) => (code int, err Error)
	 * 将本 Table 的内容作为静态资源发布。静态资源的url为：http://domain/key
	 */
	public PublishRet publish(String domain) throws Exception {
		String url = Config.RS_HOST + "/publish/" + Client.urlsafeEncode(domain) + "/from/"
				+ this.bucketName;
		CallRet callRet = conn.call(url);
		return new PublishRet(callRet);
	}

	/**
	 * func Unpublish(domain string) => (code int, err Error)
	 * 取消发布
	 */
	public PublishRet unpublish(String domain) throws Exception {
		String url = Config.RS_HOST + "/unpublish/" + Client.urlsafeEncode(domain);
		CallRet callRet = conn.call(url);
		return new PublishRet(callRet);
	}

	/**
	 * func Delete(key string) => (code int, err Error)
	 * 删除资源
	 */
	public DeleteRet delete(String key) throws Exception {
		String entryURI = this.bucketName + ":" + key;
		String url = Config.RS_HOST + "/delete/" + Client.urlsafeEncode(entryURI);
		CallRet callRet = conn.call(url);
		return new DeleteRet(callRet);
	}

	/**
	 * func Drop() => (code int, err Error)
	 * 删除整个表（慎用！）
	 */
	public DropRet drop() throws Exception {
		String url = Config.RS_HOST + "/drop/" + this.bucketName;
		CallRet callRet = conn.call(url);
		return new DropRet(callRet);
	}
	
	/**
	 * func Mkbucket(bucketname string) => Bool
     * 创建一个资源表
	 */
	public CallRet mkBucket(String newBucketName) throws Exception {
		String url = Config.RS_HOST + "/mkbucket/" + newBucketName ;
		CallRet callRet = conn.call(url) ;
		return callRet ;
	}
	
	public BatchCallRet batchDelete(List<String> keys) {
		BatchCallRet ret = batchOp("delete", keys);
		return ret;
	}
	
	public BatchStatRet batchStat(List<String> keys) {
		BatchCallRet ret = batchOp("stat", keys);
		return new BatchStatRet(ret);
	}
	
	private BatchCallRet batchOp(String cmd, List<String> keys) {
		
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			String entryUri = this.bucketName + ":" + iter.next();
			String encodedEntryUri = Client.urlsafeEncode(entryUri);
			sbuf.append("op=/").append(cmd).append("/")
				.append(encodedEntryUri).append("&");
		}
		
		return batchCall(sbuf);
	}
	
	private BatchCallRet batchCall(StringBuilder body) {
		// remove the last &
		body.deleteCharAt(body.length() - 1);
		
		String url = Config.RS_HOST + "/batch";
		CallRet callRet = this.conn.callWithBinary(url,
				"application/x-www-form-urlencoded",
				body.toString().getBytes(), body.length());
	
		return new BatchCallRet(callRet);
	}
	
	private BatchCallRet batchOp(String cmd, String srcBucket, List<String> srcKeys,
			String destBucket, List<String> destKeys) {
		
		if (srcKeys.size() != destKeys.size()) {
			throw new RuntimeException("The length of src and dest keys are not matched!");
		}
		
		StringBuilder sbuf = new StringBuilder();
		for (int i = 0; i < srcKeys.size(); i++) {
			String srcEntryUri = srcBucket + ":" + srcKeys.get(i);
			String destEntryUri = destBucket + ":" + destKeys.get(i);
			String encodedEntryUriSrc = Client.urlsafeEncode(srcEntryUri);
			String encodedEntryUriDest = Client.urlsafeEncode(destEntryUri);
			sbuf.append("op=/").append(cmd).append("/")
				.append(encodedEntryUriSrc).append("/")
				.append(encodedEntryUriDest).append("&");
		}
		
		return batchCall(sbuf);
	}
	
	/**
	 * Copys the files specifed by the source bucket name and source keys
	 * to the dest bucket and use dest keys to identify them. Unlike "batchMove",
	 * the source keys are still available in the source bucket.
	 * 
	 * @param srcBucket the source bucket name
	 * @param srcKeys the keys in the source bucket
	 * @param destBucket the dest bucket name
	 * @param destKeys the new key names in the dest bucket
	 * @return BatchCallRet contains a list of CallRet
	 */
	public BatchCallRet batchCopy(String srcBucket, List<String> srcKeys, 
			String destBucket, List<String> destKeys) {
		BatchCallRet ret = batchOp("copy", srcBucket, srcKeys, destBucket, destKeys);
		return ret;
	}
	
	/**
	 * Moves the files specifed by the source bucket name and source keys
	 * to the dest bucket and use dest keys to identify them. Notice that
	 * the source bucket will not have the source keys any more.
	 * 
	 * @param srcBucket the source bucket name
	 * @param srcKeys the keys in the source bucket
	 * @param destBucket the dest bucket name
	 * @param destKeys the new key names in the dest bucket
	 * @return BatchCallRet contains a list of CallRet
	 */
	public BatchCallRet batchMove(String srcBucket, List<String> srcKeys, 
			String destBucket, List<String> destKeys) {
		BatchCallRet ret = batchOp("move", srcBucket, srcKeys, destBucket, destKeys);
		return ret;
	}
}
