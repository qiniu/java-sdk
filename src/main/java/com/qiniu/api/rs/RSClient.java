package com.qiniu.api.rs;

import java.util.Iterator;
import java.util.List;

import com.qiniu.api.auth.DigestAuthClient;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;
import com.qiniu.api.net.EncodeUtils;

/**
 * The <code>RSClient</code> only used for server side.
 * 
 */
public class RSClient {
	
	private Client conn;
	
	public RSClient(Mac mac) {
		this.conn = new DigestAuthClient(mac);
	}
	
	
	/**
	 * Gets the basic information of the file with the specified key in the
	 * bucket
	 * 
	 * @param bucket
	 *            the bucket name
	 * @param key
	 *            the file's key
	 * @return {@StatRet}
	 * 
	 */
	public Entry stat(String bucket, String key) {
		String entryURI = bucket + ":" + key;
		String url = Config.RS_HOST + "/stat/"
				+ EncodeUtils.urlsafeEncode(entryURI);
		CallRet ret = this.conn.call(url);
		return new Entry(ret);
	}

	/**
	 * Deletes the file with the specified key in the bucket.
	 * 
	 * @param bucket
	 *            target bucket
	 * @param key
	 *            deleted key
	 * @return a general response
	 */
	public CallRet delete(String bucket, String key) {
		String entryURI = bucket + ":" + key;
		String url = Config.RS_HOST + "/delete/"
				+ EncodeUtils.urlsafeEncode(entryURI);
		CallRet ret = this.conn.call(url);
		return ret;
	}

	/**
	 * Moves a file from the source bucket to the dest bucket, and the source
	 * file will be removed from the source bucket.
	 * 
	 * @param bucketSrc
	 *            the source bucket name
	 * @param keySrc
	 *            the source key in the source bucket
	 * @param bucketDest
	 *            the dest bucket name
	 * @param keyDest
	 *            the dest key in the dest bucket
	 * @return a general response
	 * 
	 */
	public CallRet move(String bucketSrc, String keySrc, String bucketDest,
			String keyDest) {
		String entryURISrc = bucketSrc + ":" + keySrc;
		String entryURIDest = bucketDest + ":" + keyDest;
		return execute("move", entryURISrc, entryURIDest);
	}

	/**
	 * Copies a source file from the source bucket to the dest bucket, unlike
	 * method "move" the source file is still available in the source bucket.
	 * 
	 * @param bucketSrc
	 *            the source bucket name
	 * @param keySrc
	 *            the source key in the source bucket
	 * @param bucketDest
	 *            the dest bucket name
	 * @param keyDest
	 *            the dest file key
	 * @return a general response
	 */
	public CallRet copy(String bucketSrc, String keySrc, String bucketDest,
			String keyDest) {
		String entryURISrc = bucketSrc + ":" + keySrc;
		String entryURIDest = bucketDest + ":" + keyDest;
		return execute("copy", entryURISrc, entryURIDest);
	}

	private CallRet execute(String cmd, String entryURISrc, String entryURIDest) {
		String encodedSrc = EncodeUtils.urlsafeEncode(entryURISrc);
		String encodedDest = EncodeUtils.urlsafeEncode(entryURIDest);
		String url = Config.RS_HOST + "/" + cmd + "/" + encodedSrc + "/"
				+ encodedDest;
		CallRet callRet = this.conn.call(url);
		return callRet;
	}

	/**
	 * Gets the basic information of the file in a batch way.
	 * 
	 * @param entries
	 *            a list of {@code EntryPath}
	 * @return a list of {@code StatRet}
	 */
	public BatchStatRet batchStat(List<EntryPath> entries) {
		BatchCallRet ret = batchOp("stat", entries);
		return new BatchStatRet(ret);
	}

	/**
	 * Deletes the files specified by the entries
	 */
	public BatchCallRet batchDelete(List<EntryPath> entries) {
		BatchCallRet ret = batchOp("delete", entries);
		return ret;
	}

	private BatchCallRet batchOp(String cmd, List<EntryPath> entries) {
		StringBuilder sbuf = new StringBuilder();
		
		for (Iterator<EntryPath> iter = entries.iterator(); iter.hasNext();) {
			EntryPath entryPath = iter.next();
			
			String entryURI = entryPath.bucket + ":" + entryPath.key;
			String encodedEntryURI = EncodeUtils.urlsafeEncode(entryURI);
			
			sbuf.append("op=/").append(cmd).append("/").append(encodedEntryURI)
					.append("&");
		}

		return batchCall(sbuf);
	}

	private BatchCallRet batchCall(StringBuilder body) {
		// remove the last &
		body.deleteCharAt(body.length() - 1);

		String url = Config.RS_HOST + "/batch";
		CallRet callRet = this.conn.callWithBinary(url,
				"application/x-www-form-urlencoded",
				body.toString().getBytes());

		return new BatchCallRet(callRet);
	}

	public BatchCallRet batchMove(List<EntryPathPair> entries) {
		BatchCallRet ret = this.batchOpPairs("move", entries);
		return ret;
	}

	public BatchCallRet batchCopy(List<EntryPathPair> entries) {
		BatchCallRet ret = this.batchOpPairs("copy", entries);
		return ret;
	}

	private BatchCallRet batchOpPairs(String cmd, List<EntryPathPair> entries) {
		StringBuilder sbuf = new StringBuilder();
		
		for (Iterator<EntryPathPair> iter = entries.iterator(); iter.hasNext();) {
			EntryPathPair e = iter.next();
			String entryURISrc = e.src.bucket + ":" + e.src.key;
			String entryURIDest = e.dest.bucket + ":" + e.dest.key;

			String encodedEntryURISrc = EncodeUtils.urlsafeEncode(entryURISrc);
			String encodedEntryURIDest = EncodeUtils.urlsafeEncode(entryURIDest);
			
			sbuf.append("op=/").append(cmd).append("/")
					.append(encodedEntryURISrc).append("/")
					.append(encodedEntryURIDest).append("&");
		}

		return batchCall(sbuf);
	}
	
}
