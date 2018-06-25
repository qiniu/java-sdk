package com.poger.test;

import java.io.UnsupportedEncodingException;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VideoPulp {
	
/**
 * created by gebo 2018.6.25
 * @throws QiniuException
 * @throws UnsupportedEncodingException
 */
	
	public void video_pulp() throws QiniuException, UnsupportedEncodingException {
		//创建auth
		Client client = new Client();
		Auth auth = Auth.create("AK","SK");
		//创建body
		String requestBody = getBody("Source Url",null,null,null,"bucket",null,null,"pulp",null,"0",2,null,null,null,null);
		//创建请求Url
		String Url = "http://argus.atlab.ai/v1/video/{Vid}";
		//创建请求头 包含七牛鉴权
		StringMap headers = auth.authorizationV2(Url,"POST",requestBody.getBytes("UTF-8"),Client.JsonMime);	
		try {
			//发送请求
			Response resp = client.post(Url, requestBody, headers);
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
		JSONObject body = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject params = new JSONObject();
		JSONObject vframe = new JSONObject();
		JSONObject save = new JSONObject();
		JSONArray ops = new JSONArray();
		JSONObject opChild = new JSONObject();
		JSONObject obParams = new JSONObject();
		JSONArray opParamsLabels = new JSONArray();
		JSONObject opParamsLabelsChild = new JSONObject();
		JSONObject terminate = new JSONObject();
		JSONObject opParamsTerminateLabels = new JSONObject();
		data = putvalue(data, "uri", uri);
		
		
		vframe = putvalue(vframe, "mode", mode);
		vframe = putvalue(vframe, "interval", interval);
		params = putvalue(params, "vframe", vframe);
		
		
		save = putvalue(save, "bucket", bucket);
		save = putvalue(save, "prefix", prefix);
		params = putvalue(params, "save", save);
		params = putvalue(params, "hookURL", hookURL);
		
		
		
		opParamsTerminateLabels = putvalue(opParamsTerminateLabels, paramTerminateLabel, paramTerminateMax);
		terminate = putvalue(terminate, "mode", paramTerminatedMode);
		terminate = putvalue(terminate, "labels", opParamsTerminateLabels);
		obParams = putvalue(obParams, "terminate", terminate);
		
		
		opParamsLabelsChild = putvalue(opParamsLabelsChild, "label", paramLabels);
		opParamsLabelsChild = putvalue(opParamsLabelsChild, "select", paramSelect);
		opParamsLabelsChild = putvalue(opParamsLabelsChild, "score", score);
		opParamsLabels.add(opParamsLabelsChild);
		
		obParams = putvalue(obParams, "labels", opParamsLabels);
		
		opChild = putvalue(opChild, "op", op);
		
		opChild = putvalue(opChild, "hookURL", singalHookUrl);
		
		opChild = putvalue(opChild, "params", obParams);
		
		ops.add(opChild);
		
		
		body = putvalue(body, "data", data);
		body = putvalue(body, "params", params);
		body = putvalue(body, "ops", ops);
		
		return body.toString();

	}
	
	/**
	 * json 节点赋值
	 * @param root
	 * @param property
	 * @param value
	 * @return
	 */
	public JSONObject putvalue(JSONObject root,String property,Object value) {
		if(value != null){
			root.put(property, value);
		}
		return root;
	}
}
