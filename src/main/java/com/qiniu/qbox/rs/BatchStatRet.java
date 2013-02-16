package com.qiniu.qbox.rs;

import java.util.List;

import com.qiniu.qbox.auth.CallRet;

public class BatchStatRet extends CallRet {

	public List<StatRet> statRetList;
	
	public BatchStatRet(CallRet ret) {
		
		super(ret);
		if (ret.ok() && ret.getResponse() != null) {
			try {
				unmarshal(ret.getResponse());
			} catch (Exception e) {
				this.exception = e;
			}
		}
	}

	private void unmarshal(String response) {
		
	}
}
