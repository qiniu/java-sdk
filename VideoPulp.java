package com.poger.test;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;


public class VideoPulp {
	
/**
 * created by gebo 2018.6.25
 * @throws QiniuException
 * @throws UnsupportedEncodingException
 */
	@Test
	public void video_pulp() throws QiniuException, UnsupportedEncodingException {
		//创建auth
		Client client = new Client();
		Auth auth = Auth.create("AK","SK");
		//创建body
		String requestBody = getBody("source url",null,null,null,"bucket",null,null,"pulp",null,"0",2,null,null,null,null);
		//创建请求Url
		String Url = "http://argus.atlab.ai/v1/video/99999";
		//创建请求头 包含七牛鉴权
		StringMap headers = auth.authorizationV2(Url,"POST",requestBody.getBytes("UTF-8"),Client.JsonMime);	
		try {
			//发送请求
			Response resp = client.post(Url, requestBody.getBytes("UTF-8"), headers,Client.JsonMime);
			System.out.println(resp.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
/**
 *     获取body
 * @param uri 视频地址
 * @param async 异步处理
 * @param mode 截帧逻辑
 * @param interval 用来设置每隔多长时间截一帧
 * @param bucket 保存截帧图片的Bucket名称
 * @param prefix  截帧图片名称的前缀
 * @param hookURL  视频检测结束后的回调地址
 * @param op 视频检测执行的命令，支持多种视频检测操作。目前，视频鉴黄的命令就是pulp
 * @param singalHookUrl 单个命令的回调地址
 * @param paramLabels 对某个命令返回label进行过滤
 * @param paramSelect 对paramLabels,设置过滤条件
 * @param score  过滤返回label结果的置信度参数
 * @param paramTerminatedMode  视频检测命令提前停止处理的参数
 * @param paramTerminateLabel  视频检测命令返回的label
 * @param paramTerminateMax  设置该类别的最大个数
 * @return
 */
	public String getBody(String uri, Boolean async, Integer mode, Integer interval, String bucket, String prefix, String hookURL, String op,String singalHookUrl,  String paramLabels, Integer paramSelect, String score, Integer paramTerminatedMode, String paramTerminateLabel, Integer paramTerminateMax) {
		//对必填项进行验证
		if (uri == null || "".equalsIgnoreCase(uri.trim()) || op == null || "".equalsIgnoreCase(op.trim())) {
			return null;
		}

		JsonObject body = new JsonObject();
		JsonObject data = new JsonObject();
		data = addvalue(data,"uri", uri);
		
		JsonObject params = new JsonObject();
		
		JsonObject vframe = new JsonObject();
		vframe = addvalue(vframe, "mode", mode);
		vframe = addvalue(vframe, "interval", interval);
		params = addvalue(params,"vframe", vframe);
		
		JsonObject save = new JsonObject();
		save = addvalue(save,"bucket", bucket);
		save = addvalue(save,"prefix", prefix);
		params = addvalue(params,"save", save);
		
		params = addvalue(params,"hookURL", hookURL);

		JsonObject obParams = new JsonObject();
		JsonObject terminate = new JsonObject();
		JsonObject opParamsTerminateLabels = new JsonObject();
		opParamsTerminateLabels = addvalue(opParamsTerminateLabels,"paramTerminateLabel", paramTerminateMax);
		terminate = addvalue(terminate,"mode", paramTerminatedMode);
		terminate = addvalue(terminate,"labels", opParamsTerminateLabels);
		obParams = addvalue(obParams,"terminate", terminate);

		JsonObject opParamsLabelsChild = new JsonObject();
		opParamsLabelsChild= addvalue(opParamsLabelsChild,"label", paramLabels);
		opParamsLabelsChild=addvalue(opParamsLabelsChild,"select", paramSelect);
		opParamsLabelsChild=addvalue(opParamsLabelsChild,"score", score);
		JsonArray opParamsLabels = new JsonArray();
		opParamsLabels.add(opParamsLabelsChild);
		obParams = addvalue(obParams, "labels", opParamsLabels);

		JsonArray  ops = new JsonArray();
		JsonObject opChild = new JsonObject();
		opChild = addvalue(opChild,"op", op);
		opChild = addvalue(opChild,"hookURL", singalHookUrl);
		opChild= addvalue(opChild,"params", obParams);
		ops.add(opChild);
		
		body= addvalue(body,"data", data);
		body= addvalue(body,"params", params);
		body= addvalue(body,"ops", ops);

		
		return body.toString();

	}
	
	/**
	 * json 节点赋值
	 * @param root
	 * @param property
	 * @param value
	 * @return
	 */
	public JsonObject addvalue(JsonObject root,String property,String value) {
		if(value != null){
			root.addProperty(property, value);
		}
		return root;
	}
	
	public JsonObject addvalue(JsonObject root,String property,Integer value) {
		if(value != null){
			root.addProperty(property, value);
		}
		return root;
	}
	
	public JsonObject addvalue(JsonObject root,String property,Boolean value) {
		if(value != null){
			root.addProperty(property, value);
		}
		return root;
	}
	
	public JsonObject addvalue(JsonObject root,String property,JsonObject value) {
		if(value != null){
			root.add(property, value);
		}
		return root;
	}
	
	public JsonObject addvalue(JsonObject root,String property,JsonArray value) {
		if(value != null){
			root.add(property, value);
		}
		return root;
	}
}
