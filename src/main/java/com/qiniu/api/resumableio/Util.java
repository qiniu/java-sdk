package com.qiniu.api.resumableio;

import java.util.zip.CRC32;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.Client;

public class Util {
    public static HttpPost buildUpPost(String url, String token) {
        HttpPost post = Client.newPost(url);
        post.setHeader("Authorization", "UpToken " + token);
        return post;
    }

    public static CallRet handleResult(HttpResponse response) {
        try {
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            String responseBody = EntityUtils.toString(
                    response.getEntity(), "utf-8");
            return new CallRet(statusCode, responseBody);
        } catch (Exception e) {
            CallRet ret = new CallRet(Config.ERROR_CODE, "can not load response.");
            ret.exception = e;
            return ret;
        }
    }

    public static long crc32(byte[] data){
    	CRC32 crc32 = new CRC32();
    	crc32.update(data);
		return crc32.getValue();
    }

}
