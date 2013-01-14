package com.qiniu.qbox.rs;

import java.io.File;
import java.util.Map;

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
	 * func SaveAs(target_key, source_url, opWithParams string) => Bool
	 * 调用相关接口进行云处理并持久化存储处理结构
	 */
	private CallRet saveAs(String targetBucketName, String targetKey, String srcUrl, String opWirthParams) {
		String entryUrl = targetBucketName + ":" + targetKey ;
		String encodedUrl = Client.urlsafeEncode(entryUrl) ;
		String url = srcUrl + "?" + opWirthParams + "/save-as/" + encodedUrl ;
		CallRet callRet = conn.call(url) ;
		return callRet ;
	}
	/**
	 * func ImageMogrifyAs(target_key string, source_img_url string, opts map) => Bool
     * 基于指定URL的原图生成缩略图并以指定的key持久化存储该缩略图
	 * @param key
	 * @param srcImgUrl
	 * @param opts
	 * @return
	 */
	public CallRet imageMogrifySaveAs(String targetBucketName, String targetKey, String srcImgUrl, Map<String, String> opts) {
		String mogrifyParams = Fileop.mkImageMogrifyParams(opts) ;
		return this.saveAs(targetBucketName, targetKey, srcImgUrl, mogrifyParams) ;
	}

	public ImageInfoRet imageInfo(String imgUrl) throws Exception {
		CallRet callRet = conn.call(imgUrl) ;
		return new ImageInfoRet(callRet) ;
	}

	public CallRet imageEXIF(String imgUrl) {
		CallRet callRet = conn.call(imgUrl) ;
		return callRet ;
	}
}
