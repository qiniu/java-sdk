package com.qiniu.qbox.rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.ResumablePutRet;
import com.qiniu.qbox.up.UpService;

public class RSClient {

	private static class ResumableNotifier implements ProgressNotifier,
			BlockProgressNotifier {
		private PrintStream os;

		public ResumableNotifier(String progressFile) throws Exception {
			OutputStream out = new FileOutputStream(progressFile, true);
			this.os = new PrintStream(out, true);
		}

		public void notify(int blockIndex, String checksum) {

			try {
				HashMap<String, Object> doc = new HashMap<String, Object>();
				doc.put("block", blockIndex);
				doc.put("checksum", checksum);
				String json = JSONObject.valueToString(doc);
				// write to file
				os.println(json);
			} catch (Exception e) {
				// nothing to do;
				e.printStackTrace();
			}
		}

		public void notify(int blockIndex, BlockProgress progress) {

			try {
				HashMap<String, Object> doc = new HashMap<String, Object>();
				doc.put("block", blockIndex);

				Map<String, String> map = new HashMap<String, String>();
				map.put("context", progress.context);
				map.put("offset", progress.offset + "");
				map.put("restSize", progress.restSize + "");
				doc.put("progress", map);

				String json = JSONObject.valueToString(doc);
				os.println(json);
			} catch (Exception e) {
				// nothing to do;
				e.printStackTrace();
			}
		}
	}

	public static PutFileRet putFile(String url, String bucketName, String key,
			String mimeType, String localFile, String customMeta,
			Object callbackParams1) throws Exception {

		File file = new File(localFile);
		if (!file.exists() || !file.canRead()) {
			return new PutFileRet(new CallRet(400, new Exception(
					"File does not exist or not readable.")));
		}

		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}

		String entryURI = bucketName + ":" + key;
		String action = "/rs-put/"
				+ Base64.encodeBase64String(entryURI.getBytes()) + "/mimeType/"
				+ Base64.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			action += "/meta/"
					+ Base64.encodeBase64String(customMeta.getBytes());
		}

		MultipartEntity requestEntity = new MultipartEntity();
		requestEntity.addPart("action", new StringBody(action));

		FileBody fileBody = new FileBody(new File(localFile));
		requestEntity.addPart("file", fileBody);

		if (callbackParams1 != null) {
			String callbackParams = Client.encodeParams(callbackParams1);
			if (callbackParams != null) {
				requestEntity.addPart("params", new StringBody(callbackParams));
			}
		}
		HttpPost postMethod = new HttpPost(url);
		postMethod.setEntity(requestEntity);

		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(postMethod);
			return handleResult(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new PutFileRet(new CallRet(400, e));
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	private static PutFileRet handleResult(HttpResponse response) {
		if (response == null || response.getStatusLine() == null) {
			return new PutFileRet(new CallRet(400, "No response"));
		}

		try {
			String responseBody = EntityUtils.toString(response.getEntity());

			StatusLine status = response.getStatusLine();
			int statusCode = (status == null) ? 400 : status.getStatusCode();

			return new PutFileRet(new CallRet(statusCode, responseBody));
		} catch (Exception e) {
			e.printStackTrace();
			return new PutFileRet(new CallRet(400, e));
		}
	}

	public static PutFileRet resumablePutFile(UpService c, String[] checksums,
			BlockProgress[] progresses, ProgressNotifier progressNotifier,
			BlockProgressNotifier blockProgressNotifier, String bucketName,
			String key, String mimeType, RandomAccessFile f, long fsize,
			String customMeta, String callbackParams) {

		ResumablePutRet ret = c.resumablePut(f, fsize, checksums, progresses,
				progressNotifier, blockProgressNotifier);

		if (!ret.ok()) {
			return new PutFileRet(ret);
		}

		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream"; 
		}

		String params = "/mimeType/"
				+ Base64.encodeBase64String(mimeType.getBytes());
		if (customMeta != null && !customMeta.isEmpty()) {
			params += "/meta/"
					+ Base64.encodeBase64String(customMeta.getBytes());
		}

		String entryUri = bucketName + ":" + key;
		CallRet callRet = c.makeFile("/rs-mkfile/", entryUri, fsize, params,
				callbackParams, checksums);

		return new PutFileRet(callRet);
	}

	/**
	 * Allow users uploading a local file to the server in a resumable way.
	 * @param c
	 * @param bucketName
	 * 		  The bucket name
	 * @param key
	 *        The key
	 * @param mimeType
	 * 	      mimeType
	 * @param inputFile
	 *        local file that will be uploaded
	 * @param customMeta
	 * @param callBackParams
	 * @param progressFile 保存上传进度的文件，可以根据需要自己指定其存储位置
	 * @return
	 */
	public static PutFileRet resumablePutFile(UpService c, String bucketName,
			String key, String mimeType, String inputFile, String customMeta,
			String callBackParams, String progressFile) {
		RandomAccessFile f = null;
		PutFileRet putFileRet = null;
		try {
			f = new RandomAccessFile(inputFile, "r");

			long fsize = f.length();
			int blockCount = UpService.blockCount(fsize);
			if (progressFile == null || "".equals(progressFile)) {
				progressFile = inputFile + ".progress" + fsize;
			}
			String[] checksums = new String[(int) blockCount];
			BlockProgress[] progresses = new BlockProgress[(int) blockCount];
			
			readProgress(progressFile, checksums, progresses, blockCount);
			ResumableNotifier notif = new ResumableNotifier(progressFile);

			putFileRet = RSClient.resumablePutFile(c, checksums, progresses,
					(ProgressNotifier) notif, (BlockProgressNotifier) notif,
					bucketName, key, mimeType, f, fsize, customMeta, "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (putFileRet.ok()) {
			System.out.println("Successfully put file resumably: " + putFileRet.getHash());
		} else {
			System.out.println("Failed to put file resumably: " + putFileRet);
		}
		return putFileRet;
	}

	private static void readProgress(String file, String[] checksums,
			BlockProgress[] progresses, int blockCount) throws Exception {
		File fi = new File(file);
		if (!fi.exists()) {
			return;
		}
		FileReader f = new FileReader(file);
		BufferedReader is = new BufferedReader(f);

		for (;;) {
			String line = is.readLine();
			if (line == null) // has no content any more
				break;

			JSONObject o = new JSONObject(line);
			Object block = o.get("block");
			if (block == null) { // invalid content
				// error ...
				break;
			}
			int blockIdx = (Integer) block;
			if (blockIdx < 0 || blockIdx >= blockCount) { // invalid blockIndex
				// error ...
				break;
			}

			Object checksum = null;
			if (o.has("checksum")) {
				checksum = o.get("checksum");
			}

			// each block has a checksum value
			if (checksum != null) {
				checksums[blockIdx] = (String) checksum;
				continue;
			}

			JSONObject progress = null;
			if (o.has("progress")) {
				progress = (JSONObject) o.get("progress");
			}

			if (progress != null) {
				BlockProgress bp = new BlockProgress();
				bp.context = progress.getString("context");
				bp.offset = progress.getInt("offset");
				bp.restSize = progress.getInt("restSize");
				progresses[blockIdx] = bp;
				continue;
			}
			break; // error ...
		}

		if (is != null) {
			is.close();
			is = null;
		}
	}
}
