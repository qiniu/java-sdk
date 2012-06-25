package com.qiniu.qbox.auth;

import org.json.JSONException;
import org.json.JSONStringer;

public class AuthPolicy {
	public String scope;
	public String callbackUrl;
	public String returnUrl;
	public long deadline;
	
	public AuthPolicy(String scope, String callbackUrl, String returnUrl, long deadline) {
		this.scope = scope;
		this.callbackUrl = callbackUrl;
		this.returnUrl = returnUrl;
		this.deadline = deadline;
	}
	
	public String marshal() throws JSONException {

		JSONStringer stringer = new JSONStringer();
		
		stringer.object();
		stringer.key("scope").value(this.scope);
		stringer.key("callbackUrl").value(this.callbackUrl);
		stringer.key("returnUrl").value(this.returnUrl);
		stringer.key("deadline").value(this.deadline);
		stringer.endObject();

		return stringer.toString();
	}
}
