package com.examples;

import com.google.gson.Gson;
import com.qiniu.http.Client;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FetchApiDemo {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(FetchApiDemo.class);

    public static void main(String[] args) throws Exception {
        //参考api文档 https://developer.qiniu.com/kodo/api/4097/asynch-fetch
        //设置好账号的ACCESS_KEY和SECRET_KEY
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";
        //要上传的空间
        String bucketname = "Bucket_Name";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        // 构造post请求body
        Gson gson = new Gson();
        Map<String, String> m = new HashMap();
        m.put("url", "http://chentaodb.qiniuts.com/1.jpg");
        m.put("bucket", bucketname);
        String paraR = gson.toJson(m);
        byte[] bodyByte = paraR.getBytes();
        // 获取签名
        String url = "http://api-z2.qiniu.com/sisyphus/fetch";
        String accessToken = (String) auth.authorizationV2(url, "POST", bodyByte, "application/json")
                .get("Authorization");
        Client client = new Client();
        StringMap headers = new StringMap();
        logger.info(accessToken);
        headers.put("Authorization", accessToken);
        try {
            com.qiniu.http.Response resp = client.post(url, bodyByte, headers, Client.JsonMime);
            logger.info(resp.bodyString());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
