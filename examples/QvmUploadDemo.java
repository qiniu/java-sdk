package com.qiniu.storage;

import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 华东 1 区域(杭州)和华北 2 区域(北京)的云主机 通过内网上传资源到七牛同区域的对象存储空间，
 * 并且避免绕行公网带来的网络质量不稳定问题，也可以免去数据在传输过程中被窃取的风险，
 * 参考文档：https://developer.qiniu.com/qvm/manual/4269/qvm-kodo
 */
public class QvmUploadDemo {
    // 获取凭证
    public static void main(String[] args) {
        //设置账号的AK,SK
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";
        //要上传的空间
        String bucketname = "Bucket_Name";
        //上传到七牛后保存的文件名
        String key = "my-java.png";
        //上传文件的路径
        String FilePath = "/.../...";


        //第一种方式: 指定具体的要上传的region
        //指定存储空间所在区域，华北region1，华南region2 ，华东 region0等。具体可以Region类里寻找。
        Configuration configuration = new Configuration(Region.region1());
        //创建上传对象
        UploadManager uploadManager = new UploadManager(configuration);
        //秘钥鉴权
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        //简单上传，使用默认策略，只需要设置上传的空间名就可以了
        String upToken = auth.uploadToken(bucketname);
        Response res = null;
        try {
            //调用上传方法
            res = uploadManager.put(FilePath, key, upToken);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        //打印返回的信息
        System.out.println(res.statusCode);
        System.out.println(res.toString());
    }
}
