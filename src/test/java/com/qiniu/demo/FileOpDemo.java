package com.qiniu.demo;

import java.util.HashMap;
import java.util.Map;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.FileOp;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.ImageInfoRet;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;

public class FileOpDemo {
	
	
	public static void main(String[] args) throws Exception {
		
		// First of all, get the config information through the specified file.
		Config.init("/home/wangjinlei/QBox.config") ;
		
		DigestAuthClient conn = new DigestAuthClient();
		String bucketName = "testPhotos";
		String key = "/home/wangjinlei/dg.jpg";
		//String key = "dao.jpg" ;
		String path = FileOpDemo.class.getClassLoader().getResource("").getPath();
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
		ImageInfoRet imgInfoRet = rs.imageInfo(fp.getImageInfoURL(imgDownloadUrl)) ;
		System.out.println("Resulst of imageInfo() : ") ;
		System.out.println("**    format : " + imgInfoRet.getFormat()) ;
		System.out.println("**    width  : " + imgInfoRet.getWidth()) ;
		System.out.println("**    height : " + imgInfoRet.getHeight()) ;
		System.out.println("**colorModel : " + imgInfoRet.getColorMode()) ;
		
		// get the exif info from the specified image url
		CallRet imgExRet = rs.imageEXIF(fp.getImageEXIFURL(imgDownloadUrl)) ;
		System.out.println("Result of imageEXIF()  : ") ;
		System.out.println("**    ok : " + imgExRet.ok()) ;
		System.out.println("**       : " + imgExRet.getResponse()) ;
		
		// get image preview url
		String imgPreviewUrl = fp.getImagePreviewURL(imgDownloadUrl, 1) ;
		System.out.println("imgPreviewUrl : " + imgPreviewUrl) ;
		
		// get image view url 
		Map<String, String> imgViewOpts = new HashMap<String, String>() ;
		imgViewOpts.put("mode", "1") ;
		imgViewOpts.put("w", "100") ;
		imgViewOpts.put("h", "200") ;
		imgViewOpts.put("q", "1") ;
		imgViewOpts.put("format", "jpg") ;
		imgViewOpts.put("sharpen", "100") ;
		String imgViewUrl = fp.getImageViewURL(imgDownloadUrl, imgViewOpts) ;
		System.out.println("image view url : " + imgViewUrl) ;
		
		
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
		
		
		
		CallRet imgSaveAsRet = rs.imageMogrifySaveAs("testTarget", key, imgDownloadUrl, opts) ;
		if (imgSaveAsRet.ok()) {
			System.out.println("OK : " + imgSaveAsRet) ;
		} else {
			System.out.println("Fail : " + imgSaveAsRet) ;
		}
	}
}
