package com.qiniu.demo;

import java.util.HashMap;
import java.util.Map;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.FileOp;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;

public class FileOpDemo {
	
	
	public static void main(String[] args) throws Exception {
		
		// First of all, get the config information through the specified file.
		Config.init("QBox.config") ;
		
		DigestAuthClient conn = new DigestAuthClient();
		String bucketName = "testPhotos";
		String key = "/home/wangjinlei/dao.jpg";

		String path = RSDemo.class.getClassLoader().getResource("").getPath();
		System.out.println("Test to put local file: " + path + key);

		RSService rs = new RSService(conn, bucketName);
		PutAuthRet putAuthRet = rs.putAuth();
		String putUrl = putAuthRet.getUrl();
		System.out.println("Put URL: " + putUrl);

		Map<String, String> callbackParams = new HashMap<String, String>();
		callbackParams.put("key", key);
		
		// delete the image, just for convenient when you're testing
		CallRet delRet = rs.delete(key) ;
		if (delRet.ok()) {
			System.out.println("Delete successfully.") ;
		} else {
			System.out.println("Delete failed " + delRet) ;
		}
		
		
		PutFileRet putFileRet = RSClient.putFile(putAuthRet.getUrl(),
				bucketName, key, "", key, "CustomData", callbackParams);
		if (!putFileRet.ok()) {
			System.out.println("Failed to put file " + path + key + ": " + putFileRet);
			return;
		} else {
			System.out.println("Put file " + path + key + " successfully." ) ;
		}
		
		GetRet getRet = rs.get(key, key);
		System.out.println(getRet) ;
		String imgDownloadUrl = getRet.getUrl() ;
		System.out.println("Image Download Url : " + imgDownloadUrl) ;
		
		FileOp fp = new FileOp() ;
		// get the image info from the specified url
		CallRet imgRet = rs.imageInfo(fp.getImageInfoURL(imgDownloadUrl)) ;
		System.out.println("ImageInfo " + imgRet) ;
		
		// get the exif info from the specified image url
		CallRet imgExRet = rs.imageEXIF(fp.getImageEXIFURL(imgDownloadUrl)) ;
		System.out.println("ImageExif  : " + imgExRet) ;
		
		
		Map<String, String> opts = new HashMap<String, String>() ;
		opts.put("thumbnail", "!120x120r") ;
		opts.put("gravity", "center") ;
		opts.put("crop", "!120x120a0a0") ;
		opts.put("quality", "85") ;
		opts.put("rotate", "45") ;
		opts.put("format", "jpg") ;
		opts.put("auto_orient", "True") ;
		// get image mogrify preview url
		String mogrifyPreviewUrl = fp.getImageMogrifyPreviewURL(imgDownloadUrl, opts) ;
		System.out.println("ImageMogrifyPreviewUrl : " + mogrifyPreviewUrl) ;
		
		CallRet imgSaveAsRet = rs.imageMogrifySaveAs(key, imgDownloadUrl, opts) ;
		if (imgSaveAsRet.ok()) {
			System.out.println("OK : " + imgSaveAsRet) ;
		} else {
			System.out.println("Fail : " + imgSaveAsRet) ;
		}
	}
}
