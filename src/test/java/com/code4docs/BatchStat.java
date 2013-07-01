package com.code4docs;

import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchStatRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.RSClient;

public class BatchStat {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		
		RSClient rs = new RSClient(mac);
		List<EntryPath> entries = new ArrayList<EntryPath>();

		EntryPath e1 = new EntryPath();
		e1.bucket = "<bucketName>";
		e1.key = "<key1>";
		entries.add(e1);

		EntryPath e2 = new EntryPath();
		e2.bucket = "<bucketName>";
		e2.key = "<key2>";
		entries.add(e2);

		BatchStatRet bsRet = rs.batchStat(entries);
	}
}
