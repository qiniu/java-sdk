package com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.util.Auth;

//https://developer.qiniu.com/kodo/sdk/java#fusion-refresh-urls
public class CdnRefreshDemo {
    public static void main(String args[]) {
        String accessKey = "your access key";
        String secretKey = "your secret key";
        Auth auth = Auth.create(accessKey, secretKey);
        CdnManager c = new CdnManager(auth);
        //待刷新的链接列表
        String[] urls = new String[]{
                "http://javasdk.qiniudn.com/gopher1.jpg",
                "http://javasdk.qiniudn.com/gopher2.jpg",
                //....
        };
        try {
            //单次方法调用刷新的链接不可以超过100个
            CdnResult.RefreshResult result = c.refreshUrls(urls);
            System.out.println(result.code);
            //获取其他的回复内容
        } catch (QiniuException e) {
            System.err.println(e.response.toString());
        }
    }

}
