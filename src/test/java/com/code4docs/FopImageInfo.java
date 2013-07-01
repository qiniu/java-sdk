package com.code4docs;
import com.qiniu.api.fop.ImageInfo;
import com.qiniu.api.fop.ImageInfoRet;

public class FopImageInfo {

	public static void main(String[] args) {
		String url = "<domain>" + "/" + "<key>";
		ImageInfoRet ret = ImageInfo.call(url);
	}
}