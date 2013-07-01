package com.code4docs;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.RSClient;

public class Copy {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient client = new RSClient(mac);
		client.delete("<bucketName>", "<key>");
	}
}
