package com.code4docs;

import com.qiniu.api.fop.ImageView;
import com.qiniu.api.net.CallRet;

public class FopImageView {

	public static void main(String[] args) {
		String url = "http://domain/key";
		ImageView iv = new ImageView();
		iv.mode = 1 ;
		iv.width = 100 ;
		iv.height = 200 ;
		iv.quality = 1 ;
		iv.format = "jpg" ;
		CallRet ret = iv.call(url);
	}
}
