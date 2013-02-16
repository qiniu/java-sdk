package com.qiniu.qbox.rs;

import java.io.File;

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
	
	public CallRet move(String src, String dest) {
		return execute("move", src, dest);
	}

	public CallRet copy(String src, String dest) {
		return execute("copy", src, dest);
	}

	private CallRet execute(String cmd, String src, String dest) {
		String encodedSrc = Client.urlsafeEncode(src);
		String encodedDest = Client.urlsafeEncode(dest);
		String url = Config.RS_HOST + "/" + cmd + "/" + encodedSrc + "/"
				+ encodedDest;
		CallRet callRet = conn.call(url);
		return callRet;
	}

	public BucketsRet buckets() {
		String url = Config.RS_HOST + "/buckets";
		CallRet ret = conn.call(url);
		return new BucketsRet(ret);
	}

	public static void main(String[] args) {
		
		try {
			String bucketName = "wjl-test1";
			String bucketName2 = "wjl-test2";
			Config.ACCESS_KEY = "ttXNqIvhrYu04B_dWM6GwSpcXOZJvGoYFdznAWnz";
			Config.SECRET_KEY = "rX-7Omdag0BIBEtOyuGQXzx4pmTUTeLxoPEw6G8d";
			DigestAuthClient conn = new DigestAuthClient();
			RSService rs = new RSService(conn, "");
			String key = "mm";
DropRet dr = rs.drop();
System.out.println("dr : " + dr.response + " : " + dr.statusCode);
			String src = bucketName + ":" + key;
			String dest = bucketName2 + ":" + key;

			//CallRet ret = rs.move(src, dest);
			 CallRet ret = rs.copy(dest, src);
			System.out.println("move resposne : " + ret.response);
			System.out.println("move statuscode : " + ret.statusCode);
			System.out.println("move exception : " + ret.getException());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
