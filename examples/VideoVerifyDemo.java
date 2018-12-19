package com.examples;

import com.google.gson.Gson;
import com.qiniu.http.Client;
import com.qiniu.http.ProxyConfiguration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.HashMap;
import java.util.Map;

public class VideoVerifyDemo {

    public static void main(String[] args) throws Exception {
        //参考api文档 https://developer.qiniu.com/dora/manual/4258/video-pulp
        //设置好账号的ACCESS_KEY和SECRET_KEY
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";
        //要上传的空间
        String bucketname = "Bucket_Name";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        //aaaa 视频别名标识
        String vedioUrl = "http://argus.atlab.ai/v1/video/aaaa";

        // 构造post请求body
        Gson gson = new Gson();

        // data
        Map<String, Object> data = new HashMap<>();
        data.put("uri", "https://neko.chentao.qiniuts.com/V-pc60fps-en_us-1080p60.mp4");

        // params
        Map<String, Object> params = new HashMap<>();
        params.put("async", false);// 是否异步处理

        Map<String, Object> segment = new HashMap<>();
        segment.put("mode", 0);// 片段处理逻辑,0:每隔固定时间为一片段单位s，默认5s，1：每隔`N`帧为一片段，默认5；由`segment.interval`指定
        segment.put("interval", 5);
        params.put("segment", segment);

        Map<String, Object> vframe = new HashMap<>();
        vframe.put("mode", 0);// 截帧处理逻辑,mode==0:截关键帧,1:每隔固定时间截一帧由`vframe.interval`指定，单位`s`,
        // 默认为`1.0s`,2:每隔`N`帧截一帧，由`vframe.interval`指定，默认为`5`
        vframe.put("interval", 5);
        params.put("vframe", vframe);

        // ops
        Object[] ops = new Object[1];
        Map<String, Object> mapOps = new HashMap<>();
        ops[0] = mapOps;

        mapOps.put("op", "pulp");
        //mapOps.put("hookURL", "http://03b7e229.ngrok.io/callback");// 操作回调地址
        //mapOps.put("hookURL_segment", "http://a.b.c");// 片段回调地址

        Map<String, Object> opsParams = new HashMap<>();

        mapOps.put("params", opsParams);

        Map<String, Object> label = new HashMap<>();
        label.put("label", "2");// 选择类别名，跟具体推理cmd有关
        label.put("select", 2);// 类别选择条件，`1`表示忽略不选；`2`表示只选该
        label.put("score", 0.8);// 类别选择的可信度参数，大于该值才能选择
        Map[] labels = new Map[1];
        labels[0] = label;

        Map<String, Object> terminate = new HashMap<>();
        terminate.put("mode", "1");// 提前退出类型。`1`表示按帧计数；`2`表示按片段计数

        // 该类别的最大个数，达到该阈值则处理过程退出
        Map<String, Object> terminateLabels = new HashMap<>();

        terminateLabels.put("<label>", 2);
        // terminate.put("labels", terminateLabels);//该类别的最大个数，达到该阈值则处理过程退出

        opsParams.put("labels", labels);
        // opsParams.put("terminate", terminate);

        Map<String, Object> para = new HashMap<>();
        para.put("data", data);
        para.put("params", params);
        para.put("ops", ops);

        String paraR = gson.toJson(para);
        byte[] bodyByte = paraR.getBytes();
        System.out.println(paraR);

        // 获取签名
        String accessToken = (String) auth.authorizationV2(vedioUrl, "POST", bodyByte, "application/json")
                .get("Authorization");
        System.out.println(accessToken);

        System.out.println("curl -vX POST http://argus.atlab.ai/v1/video/aaaa -d '" + paraR
                + "' -H 'Content-Type:application/json' -H 'Authorization:" + accessToken + "'");

        Client client =
                new Client(null, false, (ProxyConfiguration) null, 10, 30, 0, 64, 16, 32, 5);
        StringMap headers = new StringMap();
        headers.put("Authorization", accessToken);
        try {
            com.qiniu.http.Response resp = client.post(vedioUrl, bodyByte, headers, Client.JsonMime);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
