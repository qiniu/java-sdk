package com.examples.client;

import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;

/**
 * 此demo为您展示了如何从客户端向七牛云存储上传一个本地文件。
 * 
 */
public class PutFileDemo {

	public static void main(String[] args) throws Exception {
		// 从业务服务端得到上传凭证
		String uptoken = "<Get it from the biz server!>";
		
		// 注：此处的key可以类比数据中表中的主键，此处用上传文件的文件名。
		String key = "logo.png";
		String dir = System.getProperty("user.dir");
		
		// 本地文件的绝对路径
		String localFile = dir + "/testdata/" + key;

		// 可选的上传选项，具体说明请参见使用手册。
		PutExtra extra = new PutExtra();
		
		// 上传文件
		PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);

		if (ret.ok()) {
			System.out.println("Successfully upload the file.");
		} else {
			System.out.println("opps, error : " + ret);
			return;
		}
	}
}
