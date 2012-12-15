package com.qiniu.demo ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.auth.UpTokenClient;
import com.qiniu.qbox.rs.DeleteRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;
import com.qiniu.qbox.up.UpService;

public class ResumablePutDemo {
	
	// 为了测试方便
	public static void delete(String bucketName, String key) {
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucketName);
		DeleteRet deleteRet = null ;
		try {
			deleteRet = rs.delete(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (deleteRet.ok()) {
			System.out.println("Delete ok ! " + deleteRet.hashCode()) ;
		} else {
			System.out.println("Delete failed ! " + deleteRet) ;
		}
	}
	
	// 生成指定大小的临时文件，作为测试之用
	public static String mkTmpFile(long fsize, String fileName) {
		File file = null;
		if (null == fileName || "".equals(fileName)) {
			file = new File("tmp.txt");
		} else {
			file = new File(fileName);
		}

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.setLength(fsize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
				raf = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileName;
	}
	
	public static void main(String[] args) throws Exception {
		Config.init("/home/wangjinlei/QBox.config") ;
		
		String dir = System.getProperty("user.dir");
		String fileName = "tmp.dat";
	//	long fsize = 4 * 1024 * 1024 + 2 * 256 * 1024 + 1024 ;
		long fsize = 256 * 1024 * 2 + 1024 ;
		String completePath = mkTmpFile(fsize, dir + "/" + fileName);
		String inputFile = completePath ;
		System.out.println(inputFile) ;
		String bucketName = "bucketName";
		String key = "RSDemo.class";
		
		// delete the file if exists
		delete(bucketName, key) ;
		
		AuthPolicy policy = new AuthPolicy("bucketName", 3600);
		String token = policy.makeAuthTokenString();
		UpTokenClient upTokenClient = new UpTokenClient(token);
		UpService upClient = new UpService(upTokenClient);
		PutFileRet  putFileRet = null ;
		/*
		 * Here, we provide a nicer method to upload a file in a resumable way. In this case,
		 * you don't need to save the uploading progress manually, Because We've done it 
		 * for you. If you don't like it, you can do it yourself, either.
		 */
		putFileRet = RSClient.resumablePutFile(upClient,
				bucketName, key, "", inputFile, "CustomMeta", "", "");
		
		if (putFileRet.ok()) {
			System.out.println("Resumable put file successfully.");
		} else {
			System.out.println("Resumable put file failed...");
		}
	}
	
}
