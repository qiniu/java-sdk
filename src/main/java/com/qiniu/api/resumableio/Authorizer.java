package com.qiniu.api.resumableio;

public class Authorizer {
	private String uploadToken;
	private static Authorizer authorizer = new Authorizer();
	
	public static Authorizer  getInstance(){
		return authorizer;
	}
	
	private Authorizer(){
		
	}
	
	/* 
	 * 上传只包含文件相关,不包含资源处理.将上传和资源处理分开，可修改该方法
	 */
	public synchronized void buildNewUploadToken() {

	}
	
	public synchronized void setUploadToken(String token) {
		this.uploadToken = token;
	}

	public String getUploadToken() {
		if(uploadToken == null){
			buildNewUploadToken();
		}
		return uploadToken;
	}

}
