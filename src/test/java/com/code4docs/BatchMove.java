package com.code4docs;

import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchCallRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.EntryPathPair;
import com.qiniu.api.rs.RSClient;

public class BatchMove {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient rs = new RSClient(mac);
		List<EntryPathPair> entries = new ArrayList<EntryPathPair>();
		
		EntryPathPair pair1 = new EntryPathPair();
		
		EntryPath src = new EntryPath();
		src.bucket = "<srcBucket>";
		src.key = "<key1>";
		
		EntryPath dest = new EntryPath();
		dest.bucket = "<destBucket>";
		dest.key = "<key1>";
		
		pair1.src = src;
		pair1.dest = dest;
		
		EntryPathPair pair2 = new EntryPathPair();
		
		EntryPath src2 = new EntryPath();
		src2.bucket = "<srcBucket>";
		src2.key =  "<key2>";
		
		EntryPath dest2 = new EntryPath();
		dest2.bucket = "<destBucket>";
		dest2.key = "<key2>";
		
		pair2.src = src2;
		pair2.dest = dest2;
		
		entries.add(pair1);
		entries.add(pair2);
		
		BatchCallRet ret = rs.batchMove(entries);
	}
}
