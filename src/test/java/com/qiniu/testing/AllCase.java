package com.qiniu.testing;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCase {
	
	public static Test suite() {
		TestSuite suite = new ActiveTestSuite();
		
		suite.addTestSuite(FileopTest.class);
		
		suite.addTestSuite(IOTest.class);
		
		suite.addTestSuite(RSStatTest.class);
		suite.addTestSuite(CopyTest.class);
		suite.addTestSuite(MoveTest.class);
		
		suite.addTestSuite(BatchStatTest.class);
		suite.addTestSuite(BatchCopyTest.class);
		suite.addTestSuite(BatchMoveTest.class);
		
		suite.addTestSuite(RSFTest.class);
		
		suite.addTestSuite(ResumeableioTest.class);

		return suite;
	} 
	
}
