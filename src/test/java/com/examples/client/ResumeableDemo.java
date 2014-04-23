package com.examples.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

import org.json.JSONException;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.Http;
import com.qiniu.api.resumableio.RandomAccessFileUpload;
import com.qiniu.api.resumableio.SliceUpload;
import com.qiniu.api.resumableio.StreamSliceUpload;
import com.qiniu.api.rs.PutPolicy;

public class ResumeableDemo {
	private static Mac mac;
	private String uuid;
	private String bucket;
	private String file;

	// @Before
	public void setUp() throws Exception {
		Config.ACCESS_KEY = "acmK";
		Config.SECRET_KEY = "OpAp";
		bucket = "aaa6";
		file = "D:\\Downloads\\qrsb.tar.gz";

		// / 更改以上信息

		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		uuid = UUID.randomUUID().toString();
	}

	// 可直接使用UpApi封装的代码上传。 com.qiniu.api.resumableio.UpApi

	// @Test
	public void testFileNoResume() throws AuthException, JSONException {
		String key = uuid + "____" + file;
		PutPolicy putPolicy = new PutPolicy(bucket);
		String token = putPolicy.token(mac);
		RandomAccessFileUpload up = new RandomAccessFileUpload(new File(file),
				token, key, null);
		up.httpClient = Http.getClient();

		handle(up);
		long s = System.currentTimeMillis();
		PutRet ret = up.execute();
		long e = System.currentTimeMillis();
		System.out
				.println((e - s + 1)
						+ "  "
						+ ((up.getCurrentUploadLength() / 1024) / ((e - s + 1) / 1000)));
		System.out.println(ret);
		System.out.println(ret.response);
		System.out.println(ret.statusCode);
	}

	// @Test
	public void testFileFileResume() throws AuthException, JSONException {
		String key = null;// uuid + "____" + file;
		PutPolicy putPolicy = new PutPolicy(bucket);
		String token = putPolicy.token(mac);

		RandomAccessFileUpload up = new RandomAccessFileUpload(new File(file),
				token, key, null);
		up.httpClient = Http.getClient();

		handle(up);

		long s = System.currentTimeMillis();
		PutRet ret = up.execute();
		long e = System.currentTimeMillis();
		System.out
				.println((e - s + 1)
						+ "  "
						+ ((up.getCurrentUploadLength() / 1024) / ((e - s + 1) / 1000)));

		System.out.println(ret);
		System.out.println(ret.response);
		System.out.println(ret.statusCode);
		System.out.println((e - s + 1) + "ms,  "
				+ ((up.getCurrentUploadLength() / 1024) / ((e - s + 1) / 1000))
				+ "KB/s");
	}

	// @Test
	public void testStream() throws AuthException, JSONException,
			FileNotFoundException {
		String key = uuid + "____" + file;
		PutPolicy putPolicy = new PutPolicy(bucket);
		String token = putPolicy.token(mac);

		File f = new File(file);

		StreamSliceUpload up = new StreamSliceUpload(new FileInputStream(f),
				token, key, null, f.length());
		up.httpClient = Http.getClient();

		handle(up);

		long s = System.currentTimeMillis();
		PutRet ret = up.execute();
		long e = System.currentTimeMillis();
		System.out
				.println((e - s + 1)
						+ "  "
						+ ((up.getCurrentUploadLength() / 1024) / ((e - s + 1) / 1000)));

		System.out.println(ret);
		System.out.println(ret.response);
		System.out.println(ret.statusCode);
		System.out.println((e - s + 1) + "ms,  "
				+ ((up.getCurrentUploadLength() / 1024) / ((e - s + 1) / 1000))
				+ "KB/s");
	}

	private void handle(final SliceUpload up) {
		Thread t = new Thread() {
			public void run() {
				long s = System.currentTimeMillis();
				while (true) {
					try {
						String msg = up.getContentLength() + " :  "
								+ up.getCurrentUploadLength() + " -- "
								+ up.getLastUploadLength();
						if (up.getContentLength() > 0) {
							msg += "  :  "
									+ (up.getCurrentUploadLength() + up
											.getLastUploadLength()) * 100
									/ up.getContentLength() + "%";
						}
						System.out.println(msg);
						long e = System.currentTimeMillis();
						System.out.println(Thread.currentThread().getId()
								+ "  "
								+ (e - s + 1)
								+ "ms,  "
								+ ((up.getCurrentUploadLength() / 1024) / ((e
										- s + 1) / 1000)) + "KB/s");
						Thread.sleep(1 * 1000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		t.setDaemon(true);
		t.start();

	}

}
