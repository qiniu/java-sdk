package com.qiniu.qbox;

import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSService;

public class QboxService {
	private DigestAuthClient conn = new DigestAuthClient() ;
	private String bucketName = "bucketName" ;
	private RSService rs = new RSService(conn, bucketName) ;
	private String localFile = "/home/wangjinlei/dao.jpg" ;
	
	static {
		Config.init("/home/wangjinlei/Qbox.config") ;
	}
	
	
	
	public boolean testPutAuth() throws Exception {
		PutAuthRet ret = rs.putAuth() ;
		return ret.ok() ;
	}
	
	
	public boolean testPutFile() throws Exception {
		String key = "QboxService.class" ;
		PutFileRet ret = rs.putFile(key, "", localFile, "") ;
		return ret.ok() ;
	}
	
}
