package com.qiniu.testing;


import junit.framework.TestCase;

import com.qiniu.api.net.EncodeUtils;

public class UtilTest extends TestCase {

	// just upload an image in testdata.
	public void test() throws Exception {
		String expectString = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+}{:?><-=,./;'[]";
		String encodedString = "MTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVp-IUAjJCVeJiooKV8rfXs6Pz48LT0sLi87J1td";
		byte[] rawBytes = EncodeUtils.urlsafeBase64Decode(encodedString);
		String decoded = new String(rawBytes);
		assertTrue(expectString.equals(decoded));
	}

	@Override
	public void tearDown() {
		// do nothing here.
	}
}
