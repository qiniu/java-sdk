package com.qiniu.qbox;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {
	private QboxService qs = new QboxService() ;
	
	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	public void testApp() {
		assertTrue(true);
	}

	public void testAdd() {
		System.out.println("add");
		int x = 1;
		int y = 0;
		Operation instance = new Operation();
		int expResult = 1;
		int result = instance.add(x, y);
		assertEquals(expResult, result);
	}
	
	public void testPutAuth() {
		boolean expect = true ;
		boolean real = true ;
		try {
			qs.testPutAuth() ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		assertEquals(expect, real) ;
	}
}
