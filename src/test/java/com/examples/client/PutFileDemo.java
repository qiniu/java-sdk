package com.examples.client;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;

/**
 * 此demo为您展示了如何从客户端向七牛云存储上传一个本地文件，以及如何通过相应的 url来查看。
 * 
 */
public class PutFileDemo {
	// 从业务服务端得到上传凭证
	private static final String UPTOKEN = "";
	
	// 上述bucket所绑定的域名
	private static final String DOMAIN = "http://xxxx.qiniudn.com";

	public static void main(String[] args) throws Exception {
		// 注：此处的key可以类比数据中表中的主键，此处用上传文件的文件名。
		String key = "logo.png";
		String dir = System.getProperty("user.dir");
		
		// 本地文件的绝对路径
		String localFile = dir + "/testdata/" + key;

		// 可选的上传选项，具体说明请参见使用手册。
		PutExtra extra = new PutExtra();
		// 设置上传文件的 mime type
		extra.mimeType = "image/jpeg";
		// 设置上传文件的目标 bucket
		
		
		// 上传文件
		PutRet ret = IoApi.putFile(UPTOKEN, key, new File(localFile), extra);

		if (ret.ok()) {
			System.out.println("Successfully upload the file.");
		} else {
			System.out.println("opps, error : " + ret);
			return;
		}

		// 构造可访问上传文件的url
		String lookUrl = DOMAIN + "/" + key;
		System.out.println("Waiting... for the browser.");

		// 在系统默认的浏览器中访问已上传的文件
		Desktop dp = Desktop.getDesktop();
		dp.browse(new URI(lookUrl));
	}
}
