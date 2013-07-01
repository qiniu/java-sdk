package com.code4docs;
import com.qiniu.api.fop.ExifRet;
import com.qiniu.api.fop.ImageExif;

public class FopImageExif {

	public static void main(String[] args) {
		String url = "<domain>" + "/" + "<key>";
		ExifRet ret = ImageExif.call(url);
	}
}