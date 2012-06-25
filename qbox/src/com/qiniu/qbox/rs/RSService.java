package com.qiniu.qbox.rs;

import org.apache.commons.codec.binary.Base64;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class RSService {

	private Client conn;
	private String tableName;

	public RSService(Client conn, String tblName) {
		this.conn = conn;
		this.tableName = tblName;
	}

	// func PutAuth() => PutAuthRet
	// 上传授权（生成一个短期有效的可匿名上传URL）
	public PutAuthRet putAuth() throws RSException {
		CallRet callRet = conn.call(Config.IO_HOST + "/put-auth/", 1, 3);
		return new PutAuthRet(callRet);
	}

	/**
	 * func Get(key string, attName string) => (data GetRet, code int, err
	 * Error) 下载授权（生成一个短期有效的可匿名下载URL）
	 */
	public GetRet get(String key, String attName) throws RSException {
		String entryURI = this.tableName + ":" + key;
		String url = Config.RS_HOST + "/get/" + encode(entryURI) + "/attName/"
				+ encode(attName);
		CallRet callRet = conn.call(url, 1, 3);
		return new GetRet(callRet);
	}

	/**
	 * func GetIfNotModified(key string, attName string, base string) => (data
	 * GetRet, code int, err Error)
	 * 下载授权（生成一个短期有效的可匿名下载URL），如果服务端文件没被人修改的话（用于断点续传）
	 */
	public GetRet getIfNotModified(String key, String attName, String base)
			throws RSException {
		String entryURI = this.tableName + ":" + key;
		String url = Config.RS_HOST + "/get/" + encode(entryURI) + "/attName/"
				+ encode(attName) + "/base/" + base;
		CallRet callRet = conn.call(url, 1, 3);
		return new GetRet(callRet);
	}

	/**
	 * func Stat(key string) => (entry Entry, code int, err Error) 取资源属性
	 */
	public StatRet stat(String key) throws RSException {
		String entryURI = this.tableName + ":" + key;
		String url = Config.RS_HOST + "/stat/" + encode(entryURI);
		CallRet callRet = conn.call(url, 1, 3);
		return new StatRet(callRet);
	}

	/**
	 * func Publish(domain string) => (code int, err Error) 将本 Table
	 * 的内容作为静态资源发布。静态资源的url为：http://domain/key
	 */
	public PublishRet publish(String domain) throws RSException {
		String url = Config.RS_HOST + "/publish/" + encode(domain) + "/from/"
				+ this.tableName;
		CallRet callRet = conn.call(url, 1, 3);
		return new PublishRet(callRet);
	}

	/**
	 * func Unpublish(domain string) => (code int, err Error) 取消发布
	 */
	public PublishRet unpublish(String domain) throws RSException {
		String url = Config.RS_HOST + "/unpublish/" + encode(domain);
		CallRet callRet = conn.call(url, 1, 3);
		return new PublishRet(callRet);
	}

	/**
	 * func Delete(key string) => (code int, err Error) 删除资源
	 */
	public DeleteRet delete(String key) throws RSException {
		String entryURI = this.tableName + ":" + key;
		String url = Config.RS_HOST + "/delete/" + encode(entryURI);
		CallRet callRet = conn.call(url, 1, 3);
		return new DeleteRet(callRet);
	}

	/**
	 * func Drop() => (code int, err Error) 删除整个表（慎用！）
	 */
	public DropRet drop() throws RSException {
		String url = Config.RS_HOST + "/drop/" + this.tableName;
		CallRet callRet = conn.call(url, 1, 3);
		return new DropRet(callRet);
	}
	
	private Base64 encoder = new Base64(true);

	protected String encode(String text) {
		return encoder.encodeBase64String(text.getBytes());
	}
}
