package com.qiniu;

import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;
import com.qiniu.common.QiniuException;
import com.qiniu.util.Auth;

//https://developer.qiniu.com/kodo/sdk/java#fusion-refresh-urls

public class CdnRefreshDirsDemo {
   public static void main(String args[]) {
       String accessKey = "your access key";
       String secretKey = "your secret key";
       Auth auth = Auth.create(accessKey, secretKey);
       CdnManager c = new CdnManager(auth);
       //待刷新的目录列表，目录必须以 / 结尾
       String[] dirs = new String[]{
               "http://javasdk.qiniudn.com/gopher1/",
               "http://javasdk.qiniudn.com/gopher2/",
               //....
       };
       try {
           //单次方法调用刷新的目录不可以超过10个，另外刷新目录权限需要联系技术支持开通
           CdnResult.RefreshResult result = c.refreshDirs(dirs);
           System.out.println(result.code);
           //获取其他的回复内容
       } catch (QiniuException e) {
           System.err.println(e.response.toString());
       }
   }

}
