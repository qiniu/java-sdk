import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Recorder;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;

import java.io.IOException;

public class UploadDemo {

    //设置好账号的ACCESS_KEY和SECRET_KEY
    String ACCESS_KEY = "Access_Key";
    String SECRET_KEY = "Secret_Key";
    //要上传的空间
    String bucketname = "Bucket_Name";
    //上传到七牛后保存的文件名
    String key = "my-java.png";
    //上传文件的路径
    String filePath = "/.../...";

    //密钥配置
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    //第一种方式: 指定具体的要上传的region
    //指定存储空间所在区域，华北region1，华南region2 ，华东 region0等。具体可以Region类里寻找。
    //Region region = Region.region1();

    //第一种方式: 使用自动方式
    Region autoRegion = Region.autoRegion();
    Configuration c = new Configuration(autoRegion);


    //创建上传对象
    UploadManager uploadManager = new UploadManager(c);

    // 覆盖上传
    public String getUpToken() {
        return auth.uploadToken(bucketname);
    }

    public void upload() throws IOException {
        //设置断点记录文件保存在指定文件夹或的File对象
        String recordPath = "/.../...";
        //实例化recorder对象
        Recorder recorder = new FileRecorder(recordPath);
        //实例化上传对象，并且传入一个recorder对象
        UploadManager uploadManager = new UploadManager(recorder);

        try {
            //调用put方法上传
            Response res = uploadManager.put("path/file", "key", getUpToken());
            //打印返回的信息
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时打印的异常的信息
            System.out.println(r.toString());
            try {
                //响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new UploadDemo().upload();
    }

}