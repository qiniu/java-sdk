package com.qiniu.sms;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

public class SmsManager {
	
	private final Auth auth;
	
	private Configuration configuration;
		
	private final Client client;
	
	private final String Host = "10.200.20.22:22230";
	
	public SmsManager(Auth auth) {
        this.auth = auth;
        client = new Client();
    }
	
	
	 /**
     * 发送短信
     * @param templateId  模板Id，必填
     * @param mobiles     手机号码数组，必填
     * @param parameter   参数,必填
     */
	public Response sendMessage(String templateId,String[] mobiles,Map<String,String> parameters) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/message", Host);
		Gson gson = new Gson();
		Map bodyMap = new HashMap();
		bodyMap.put("template_id",templateId);
		bodyMap.put("mobiles",mobiles);
		bodyMap.put("parameters",parameters);
		return post(requestUrl,gson.toJson(bodyMap).getBytes());
	}
	
	/**
     * 查询签名
     * @param auditStatus 审核状态,非必填
     *         			  取值范围为: "passed"(通过), "rejected"(未通过), "reviewing"(审核中)。
     * @param page        页码。默认为 1,非必填
     * @param pageSize	  分页大小。默认为 20,非必填
     */
	public Response describeSignature(String auditStatus,int page,int pageSize) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/signature", Host);
		//TODO add queries to url
		return get(requestUrl);
	}
	
	/**
     * 创建签名
     * @param signature  签名，必填
     * @param source     签名来源，申请签名时必须指定签名来源。必填
     *                   取值范围为：
	 *					 enterprises_and_institutions 企事业单位的全称或简称
	 *					 website 工信部备案网站的全称或简称
	 *					 app APP应用的全称或简称
	 *					 public_number_or_small_program 公众号或小程序的全称或简称
	 *					 store_name 电商平台店铺名的全称或简称
	 *					 trade_name 商标名的全称或简称
     * @param pics	     签名对应的资质证明图片进行 base64 编码格式转换后的字符串,非必填
     */
	public Response createSignature(String signature,String source,String[] pics) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/signature", Host);
		StringMap bodyMap = new StringMap();
		bodyMap.put("signature",signature);
		bodyMap.put("source",source);
		bodyMap.put("pics",pics);
		return post(requestUrl,Json.encode(bodyMap).getBytes());
	}
	
	/**
     * 编辑签名  审核不通过的情况下才可以重新编辑签名，已经审核通过的签名无法重新编辑。
     * @param signatureId  签名Id, 必填
     * @param signature    签名,必填
     */
	public Response modifySignature(String signatureId,String signature) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/signature/%s", Host,signatureId);
		StringMap bodyMap = new StringMap();
		bodyMap.put("signature",signature);
		return put(requestUrl,Json.encode(bodyMap).getBytes());
	}
	
	/**
     * 删除签名  审核不通过的情况下才可以重新编辑签名，已经审核通过的签名无法重新编辑。
     * @param signatureId  签名Id, 必填
     */
	public Response deleteSignature(String signatureId) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/signature/%s", Host,signatureId);
		return delete(requestUrl);
	}
	
	/**
     * 查询模板
     * @param auditStatus 审核状态, 非必填
     * 					  取值范围为: "passed"(通过), "rejected"(未通过), "reviewing"(审核中)。
     * @param page        页码。默认为 1,非必填
     * @param pageSize	  分页大小。默认为 20,非必填
     */
	public Response describeTemplate(String auditStatus,int page,int pageSize) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/template", Host);
		StringMap queryMap = new StringMap();
		queryMap.putNotEmpty("audit_status",auditStatus);
		queryMap.putNotNull("page", page);
		queryMap.putNotNull("page_size", pageSize);
		//TODO add queries to url
		return get(requestUrl);
	}

	/**
     * 创建模板
     * @param name          模板名称,必填
     * @param template      模板内容,必填
     * @param type	        模板类型,必填
     * 						取值范围为: notification (通知类短信), verification (验证码短信), marketing (营销类短信)。
     * @param description	申请理由简述,必填
     * @param signatureId	已经审核通过的签名,必填
     */
	public Response createTemplate (String name,String template,String type, String description,String signatureId) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/template", Host);
		StringMap bodyMap = new StringMap();
		bodyMap.put("name",name);
		bodyMap.put("template",template);
		bodyMap.put("type",type);
		bodyMap.put("description",description);
		bodyMap.put("signature_id",signatureId);
		return post(requestUrl,Json.encode(bodyMap).getBytes());
	}
	
	/**
     * 编辑模板  审核不通过的情况下才可以重新编辑模板，已经审核通过的模板无法重新编辑。
     * @param templateId    模板Id,必填
     * @param name          模板名称,必填
     * @param template      模板内容,必填
     * @param description	申请理由简述,必填
     * @param signatureId	已经审核通过的签名,必填
     */
	public Response modifyTemplate(String templateId,String name,String template,String description,String signatureId) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/template/%s", Host,templateId);
		StringMap bodyMap = new StringMap();
		bodyMap.put("name",name);
		bodyMap.put("template",template);
		bodyMap.put("description",description);
		bodyMap.put("signature_id",signatureId);
		return put(requestUrl,Json.encode(bodyMap).getBytes());
	}

	/**
     * 删除模板
     * @param templateId  模板Id, 必填
     */
	public Response deleteTemplate(String templateId) throws QiniuException{
		String requestUrl = String.format("http://%s/v1/template/%s", Host,templateId);
		return delete(requestUrl);
	}
	
	/*
     * 相关请求的方法列表
     * */
	private Response get(String url) throws QiniuException {
        StringMap headers = composeHeader(url,MethodType.GET.toString(),null,Client.FormMime);
        return client.get(url, headers);
    }
	
	private Response post(String url,byte[] body) throws QiniuException{
		StringMap headers = composeHeader(url, MethodType.POST.toString(),body, Client.JsonMime);
		return client.post(url, body, headers, Client.JsonMime);
	}
	
	private Response put(String url,byte[] body) throws QiniuException {
        StringMap headers = composeHeader(url,MethodType.PUT.toString(),body,Client.JsonMime);
        return client.put(url, body, headers, Client.JsonMime);
    }
	
	private Response delete(String url) throws QiniuException {
        StringMap headers = composeHeader(url,MethodType.DELETE.toString(),null,Client.DefaultMime);
        return client.delete(url, headers);
    }
	
	private StringMap composeHeader(String url, String method, byte[] body, String contentType) {
		StringMap headers = auth.authorizationV2(url,method,body,contentType);
		headers.put("Content-Type", contentType);
		return headers;
	}

}
