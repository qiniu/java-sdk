package com.qiniu.qbox.testing;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCase {
	
	public static Test suite() {
		TestSuite suite = new ActiveTestSuite();
		suite.addTestSuite(FileopTest.class);
		suite.addTestSuite(RsTest.class);
		suite.addTestSuite(UpTest.class);
		return suite;
	} 
	
}
