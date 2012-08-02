package com.qiniu.qbox.wm;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;

public class WMService {
	private Client conn;
	
	public WMService(Client conn) {
		this.conn = conn;
	}
	
	public CallRet get(String customer) {
		String url = Config.WM_HOST + "/get";
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("customer", customer));
		CallRet callRet = conn.call(url, nvps);	
		nvps.add(new BasicNameValuePair("customer", customer));
		return callRet;
	}
	
	public CallRet set (String customer, WMTemplate tpl) {
		String url = Config.WM_HOST + "/set";
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("text", tpl.getText()));
		nvps.add(new BasicNameValuePair("dx", ((Integer)tpl.getDx()).toString()));
		nvps.add(new BasicNameValuePair("dy", ((Integer)tpl.getDy()).toString()));
		if (!(customer == "" || customer == null)) {
			nvps.add(new BasicNameValuePair("customer", customer));
		}
		if (tpl.getFont() != null) {
			nvps.add(new BasicNameValuePair("font", tpl.getFont()));
		}
		if (tpl.getPointSize() != 0) {
			nvps.add(new BasicNameValuePair("fontsize", ((Integer)tpl.getPointSize()).toString()));
		}
		if (tpl.getFill() != null) {
			nvps.add(new BasicNameValuePair("fill", tpl.getFill()));
		}
		if (tpl.getBucket() != null) {
			nvps.add(new BasicNameValuePair("bucket", tpl.getBucket()));
		}
		if (tpl.getDissolve() != null) {
			nvps.add(new BasicNameValuePair("dissolve", tpl.getDissolve()));
		}
		if (tpl.getGravity() != null) {
			nvps.add(new BasicNameValuePair("gravity", tpl.getGravity()));
		}
		return conn.call(url, nvps);
	}
}













