package com.qiniu.testing;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rsf.ListItem;
import com.qiniu.api.rsf.ListPrefixRet;
import com.qiniu.api.rsf.RSFClient;
import com.qiniu.api.rsf.RSFEofException;

public class RSFTest extends TestCase {

	// because all the testcase concurrently executes
	// so the key should be different.
	public final String key = "_javasdk_RSFTest-key";

	public final String expectedHash = "FmDZwqadA4-ib_15hYfQpb7UXUYR";

	public String bucketName;
	
	public Mac mac;
	@Override
	public void setUp() {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY");
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY");
		Config.RS_HOST = System.getenv("QINIU_RS_HOST");
		bucketName = System.getenv("QINIU_TEST_BUCKET");

		assertNotNull(Config.ACCESS_KEY);
		assertNotNull(Config.SECRET_KEY);
		assertNotNull(Config.RS_HOST);
		assertNotNull(bucketName);
		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
	}

	// just upload an image in testdata.
	public void testRSF() throws Exception {
		{
			String uptoken = new PutPolicy(bucketName).token(mac);
			String dir = System.getProperty("user.dir");
			String localFile = dir + "/testdata/" + "logo.png";

			PutExtra extra = new PutExtra();

			// upload 3 files
			for (int i = 0; i < 3; i++) {
				PutRet ret = IoApi.putFile(uptoken, key + "_" + i,localFile, extra);
				assertTrue(ret.ok());
				assertTrue(expectedHash.equals(ret.getHash()));
			}
			
		}
		// we don't checkout the result of how may items are in the buckets.
		// not very convient, it's better, although.
		{
			RSFClient client = new RSFClient(mac);
			String marker = "";
			
			List<ListItem> all = new ArrayList<ListItem>();
			ListPrefixRet ret = null;
			while (true) {
				ret = client.listPrifix(bucketName, "_javasdk", marker, 10);
				marker = ret.marker;
				all.addAll(ret.results);
				if (!ret.ok()) {
					// no more items or error occurs
					break;
				}
			}
			if (ret.exception.getClass() != RSFEofException.class) {
				// error handler
			} 
			
			assertTrue(all.size() >= 3);
		}
	}

	@Override
	public void tearDown() {
		// do nothing here.
	}
}