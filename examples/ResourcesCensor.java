package com.examples;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.HashMap;
import java.util.Map;

public class ResourcesCensor {
    //设置好账号的ACCESS_KEY和SECRET_KEY
    private static final String ACCESS_KEY = "填写你们自己的ak";
    private static final String SECRET_KEY = "填写你们自己的sk";
    private final Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
    private final Client client = new Client();

    public static void main(String args[]) {
        ResourcesCensor resourcesCensor = new ResourcesCensor();
        String result;
        try {
            result = resourcesCensor.ImageCensor();
            //result = resourcesCensor.VideoCensor();
            System.out.printf(result);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //参考api文档 https://developer.qiniu.com/dora/manual/4252/image-review
    //图片审核
    public String ImageCensor() throws QiniuException {
        // 构造post请求body
        Gson gson = new Gson();

        Map<String, Object> uri = new HashMap<>();
        uri.put("uri", "http://oayjpradp.bkt.clouddn.com/Audrey_Hepburn.jpg");

        Map<String, Object> scenes = new HashMap<>();
        //pulp 黄  terror 恐  politician 敏感人物
        String[] types = {"pulp", "terror", "politician", "ads"};
        scenes.put("scenes", types);

        Map params = new HashMap();
        params.put("data", uri);
        params.put("params", scenes);

        String paraR = gson.toJson(params);
        byte[] bodyByte = paraR.getBytes();

        // 接口请求地址
        String url = "http://ai.qiniuapi.com/v3/image/censor";

        return post(url, bodyByte);
    }

    //参考api文档 https://developer.qiniu.com/dora/manual/4258/video-pulp
    //视频审核
    public String VideoCensor() throws QiniuException {
        // 构造post请求body
        Gson gson = new Gson();

        Map bodyData = new HashMap();

        Map<String, Object> uri = new HashMap<>();
        uri.put("uri", "https://mars-assets.qnssl.com/scene.mp4");

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> scenes = new HashMap<>();
        //pulp 黄  terror 恐  politician 敏感人物
        String[] types = {"pulp", "terror", "politician"};

        Map<String, Object> cut_param = new HashMap<>();
        cut_param.put("interval_msecs", 500);

        params.put("scenes", types);
        params.put("cut_param", cut_param);

        bodyData.put("data", uri);
        bodyData.put("params", params);
        String paraR = gson.toJson(bodyData);
        byte[] bodyByte = paraR.getBytes();

        // 接口请求地址
        String url = "http://ai.qiniuapi.com/v3/video/censor";
        return post(url, bodyByte);
    }

    private String post(String url, byte[] body) throws QiniuException {
        String accessToken = (String) auth.authorizationV2(url, "POST", body, "application/json")
                .get("Authorization");

        StringMap headers = new StringMap();
        headers.put("Authorization", accessToken);
        
        com.qiniu.http.Response resp = client.post(url, body, headers, Client.JsonMime);
        return resp.bodyString();
    }

}
