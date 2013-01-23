package com.qiniu.qbox.testing;

import java.util.Map;

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
		Map<String, String> envMap = System.getenv() ;
		
		System.out.println("accessKey : " + envMap.get("QINIU_ACCESS_KEY")) ;
		System.out.println("secretkey : " + envMap.get("QINIU_SECRET_KEY")) ;
		System.out.println("uphost : " + envMap.get("QINIU_UP_HOST")) ;
		System.out.println("rshost : " + envMap.get("QINIU_RS_HOST")) ;
		System.out.println("bucketName : " + envMap.get("QINIU_TEST_BUCKET")) ;
		
		Config.ACCESS_KEY =  envMap.get("QINIU_ACCESS_KEY");
		Config.SECRET_KEY =  envMap.get("QINIU_SECRET_KEY");
		Config.UP_HOST = envMap.get("QINIU_UP_HOST") ;
		Config.RS_HOST = envMap.get("QINIU_RS_HOST") ;
		Config.IO_HOST = envMap.get("QINIU_IO_HOST") ;
		bucketName = envMap.get("QINIU_TEST_BUCKET") ;
/*System.out.println("accessKey : " + System.getenv("QINIU_ACCESS_KEY")) ;
System.out.println("secretkey : " + System.getenv("QINIU_SECRET_KEY")) ;
System.out.println("uphost : " + System.getenv("QINIU_UP_HOST")) ;
System.out.println("rshost : " + System.getenv("QINIU_RS_HOST")) ;
System.out.println("bucketName : " + System.getenv("QINIU_TEST_BUCKET")) ;*/
		assertNotNull(Config.ACCESS_KEY) ;
		assertNotNull(Config.SECRET_KEY) ;
		assertNotNull(Config.UP_HOST) ;
		assertNotNull(Config.RS_HOST) ;
		assertNotNull(Config.IO_HOST) ;
		assertNotNull(bucketName) ;
	}
}
