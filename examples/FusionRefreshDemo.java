package com.examples;

import com.google.gson.Gson;
import com.qiniu.http.Client;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.HashMap;
import java.util.Map;

public class FusionRefreshDemo {

    public static void main(String[] args) throws Exception {
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";
        //要上传的空间
        String bucketname = "Bucket_Name";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        // 构造post请求body
        Gson gson = new Gson();
        Map<String, String[]> m = new HashMap();
        String[] urls = {"http://neko.chentao.qiniuts.com/1.MP4",
                "http://neko.chentao.qiniuts.com/1.mp4"};
        m.put("urls", urls);
        String paraR = gson.toJson(m);
        byte[] bodyByte = paraR.getBytes();
        // 获取签名
        String url = "http://fusion.qiniuapi.com/v2/tune/refresh";
        String accessToken = (String) auth.authorizationV2(url, "POST", bodyByte, "application/json")
                .get("Authorization");
        Client client = new Client();
        StringMap headers = new StringMap();
        headers.put("Authorization", accessToken);
        try {
            com.qiniu.http.Response resp = client.post(url, bodyByte, headers, Client.JsonMime);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
