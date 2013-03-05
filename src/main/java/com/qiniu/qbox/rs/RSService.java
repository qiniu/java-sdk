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
	
	/**
	 * batchDelete deletes all the files, specified by the parameter entryUris.
	 * 
	 * @param entryUris 
	 * @return BatchCallRet consists a list of CallRet
	 */
	public BatchCallRet batchDelete(List<String> entryUris) {
		BatchCallRet ret = batchOp("delete", entryUris);
		return ret;
	}
	
	/**
	 * batchStat gets the basic information of the files, specified by parameter
	 * entryUris.
	 * 
	 * @param entryUris 
	 * @return
	 */
	public BatchStatRet batchStat(List<String> entryUris) {
		BatchCallRet ret = batchOp("stat", entryUris);
		return new BatchStatRet(ret);
	}
	
	private BatchCallRet batchOp(String cmd, List<String> entryUris) {
		
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<String> iter = entryUris.iterator(); iter.hasNext();) {
			String entryUri = iter.next();
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
	
	
	private BatchCallRet batchOpPairs(String cmd, List<EntryUriPair> entryUriPairs) {
		
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<EntryUriPair> iter = entryUriPairs.iterator(); iter.hasNext();) {
			EntryUriPair e = iter.next();
			String encodedEntryUriSrc = Client.urlsafeEncode(e.src);
			String encodedEntryUriDest = Client.urlsafeEncode(e.dest);
			sbuf.append("op=/").append(cmd).append("/")
				.append(encodedEntryUriSrc).append("/")
				.append(encodedEntryUriDest).append("&");
		}
		
		return batchCall(sbuf);
	}
	
	/**
	 * copys the files from the source bucket to the dest bucket, unlike 
	 * "batchMove", the source files are still available in the source 
	 * bucket.
	 *  
	 * @param entryUriPairs a pair of source/destination entryUri
	 * an entryUri usually looks like bucket:key. 
	 * @return BatchCallRet consists of a list of CallRet
	 */
	public BatchCallRet batchCopy(List<EntryUriPair> entryUriPairs) {
		BatchCallRet ret = this.batchOpPairs("copy", entryUriPairs);
		return ret;
	}
	
	public BatchCallRet batchMove(List<EntryUriPair> entryUriPairs) {
		BatchCallRet ret = this.batchOpPairs("move", entryUriPairs);
		return ret;
	}
	
}
