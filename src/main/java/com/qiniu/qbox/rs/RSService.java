package com.qiniu.qbox.rs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.auth.DigestAuthClient;

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
	
	public CallRet batchDelete(List<String> keys) {
		
		return batchUnaryOp("delete", keys);
	}
	
	public CallRet batchStat(List<String> keys) {
		
		return batchUnaryOp("stat", keys);
	}
	
	private CallRet batchUnaryOp(String cmd, List<String> keys) {
		
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			String entryUri = this.bucketName + ":" + iter.next();
			String encodedUri = Client.urlsafeEncode(entryUri);
			sbuf.append("op=/" + cmd + "/").append(encodedUri).append("&");
		}
		// remove the last &
		sbuf.deleteCharAt(sbuf.length() - 1);
		String url = Config.RS_HOST + "/batch";
		CallRet callRet = this.conn.callWithBinary(url,
				"application/x-www-form-urlencoded",
				sbuf.toString().getBytes(), sbuf.length());

		return callRet;
	}
	
	public CallRet batchCopy(List<SrcDestPair> pairs) {
		return batchBinaryOp("copy", pairs);
	}
	
	public CallRet batchMove(List<SrcDestPair> pairs) {
		return batchBinaryOp("move", pairs);
	}
	
	private CallRet batchBinaryOp(String cmd, List<SrcDestPair> pairs) {
		
		StringBuilder sbuf = new StringBuilder();
		for (Iterator<SrcDestPair> iter = pairs.iterator(); iter.hasNext();) {
			SrcDestPair pair = iter.next();
			String encodedSrc = Client.urlsafeEncode(pair.srcEntryUri);
			String encodedDest = Client.urlsafeEncode(pair.destEntryUri);
			sbuf.append("op=/" + cmd + "/").append(encodedSrc)
					.append("/" + encodedDest + "&");
		}
		// remove the last &, either.
		sbuf.deleteCharAt(sbuf.length() - 1);
		
		String url = Config.RS_HOST + "/batch";
		CallRet callRet = this.conn.callWithBinary(url,
				"application/x-www-form-urlencoded",
				sbuf.toString().getBytes(), sbuf.length());

		return callRet;
	}
	
	public static void main(String[] args) {
		Config.ACCESS_KEY = "ttXNqIvhrYu04B_dWM6GwSpcXOZJvGoYFdznAWnz";
		Config.SECRET_KEY = "rX-7Omdag0BIBEtOyuGQXzx4pmTUTeLxoPEw6G8d";
		
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, "batch");
		List<String> keys = new ArrayList<String>();
		keys.add("mm");
		keys.add("ww");
/*		CallRet ret = rs.batchDelete(keys);
		System.out.println("batch delete : " + ret);
*/		
		CallRet statRet = rs.batchStat(keys);
		System.out.println("batch stat : " + statRet);
	}
}
