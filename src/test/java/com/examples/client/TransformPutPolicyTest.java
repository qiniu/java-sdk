package com.examples.client;

import static org.junit.Assert.*;

import java.util.UUID;

import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.EncodeUtils;
import com.qiniu.api.rs.PutPolicy;

public class TransformPutPolicyTest {
	private static Mac mac;
	private String bucket;
	private String uuid;
	private String file;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.ACCESS_KEY = "acmKu7Hie1OQ3t31bAovR6JORFX72MMpTicc2xje";
		Config.SECRET_KEY = "OpApZ68pnm1sGVl9i07IPOvjsmt3WMGEGAlN8Wsu";
	
		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
	}

	@Before
	public void setUp() throws Exception {
		bucket = "liubin";
		uuid = UUID.randomUUID().toString();
		file = "/home/xc/Downloads/test1.mov";
	}


	@Test
	public void test() throws AuthException, JSONException {
		PutPolicy putPolicy = new PutPolicy(bucket);
		
		String w = "http://liubin-pub.u.qiniudn.com/w.jpg";
		String wurl = EncodeUtils.urlsafeEncode(w);
		String fop = "vwatermark/1/image/" + wurl + "/format/mp4";
		fop = "/avthumb/mp4";
		putPolicy.transform = fop;
		putPolicy.fopTimeout = 60 * 2;
		
		String uptoken = putPolicy.token(mac);
		
		String key = "test___" + uuid + "__" + file;
		PutExtra extra = new PutExtra();
		PutRet ret = IoApi.putFile(uptoken, key, file, extra);
		assertTrue(ret.ok());
		assertEquals(ret.getKey(), key);
		System.out.println(ret);
	}

}
