package com.qiniu.qbox.testing;

import junit.framework.TestCase;

import com.qiniu.qbox.Config;

public class TestEnv extends TestCase {
	public static String accessKey ;
	public static String secretKey ;
	public static String bucketName ;
	public static String upHost ;
	public static String rsHost ;
	public static String ioHost ;
	
	static {
		Config.ACCESS_KEY = System.getenv("QINIU_ACCESS_KEY") ;
		Config.SECRET_KEY = System.getenv("QINIU_SECRET_KEY") ;
		Config.UP_HOST = System.getenv("QINIU_UP_HOST") ;
		Config.IO_HOST = System.getenv("QINIU_IO_HOST") ;
		Config.RS_HOST = System.getenv("QINIU_RS_HOST") ;
		bucketName = System.getenv("QINIU_TEST_BUCKET") ;
	
System.out.println("accessKey : " + Config.ACCESS_KEY) ;
System.out.println("bucketName : " + bucketName) ;
		assertNotNull(Config.ACCESS_KEY) ;
		assertNotNull(Config.SECRET_KEY) ;
		assertNotNull(Config.UP_HOST) ;
		assertNotNull(Config.RS_HOST) ;
		assertNotNull(Config.IO_HOST) ;
		assertNotNull(bucketName) ;
	}
}
