import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;


public class UploadBySelfDefiningParam {

    private static final String ACCESS_KEY = "设置好你们自己的AK";
    private static final String SECRET_KEY = "设置好你们自己的SK";
    private static final Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    private Configuration cfg;
    private UploadManager uploadManager;
    private Region region;
    private String bucket;

    {
        bucket = "设置你们自己的上传空间名称";
        //指定存储空间所在区域，华北region1，华南region2 ，华东 region0
        region = Region.region1();
        //初始化cfg实例，可以指定上传区域，也可以创建无参实例 , cfg = new Configuration();
        cfg = new Configuration(region);
        //是否指定https上传，默认true
        //cfg.useHttpsDomains=false;
        //构建 uploadManager 实例
        uploadManager = new UploadManager(cfg);
    }

    public static void main(String args[]) throws Exception {
        UploadBySelfDefiningParam up = new UploadBySelfDefiningParam();
        up.upload();
    }

    public void upload() throws QiniuException {
        //设置上传后的文件名称
        String key = "qiniu_test.jpg";
        //上传策略
        StringMap policy = new StringMap();
        //自定义上传后返回内容，返回自定义参数，需要设置 x:参数名称
        policy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fname\":\"$(x:fname)\",\"age\",$(x:age)}");
        //生成上传token
        String upToken = auth.uploadToken(bucket, key, 3600, policy);

        //上传自定义参数，自定义参数名称需要以 x:开头
        StringMap params = new StringMap();
        params.put("x:fname","123.jpg");
        params.put("x:age",20);
        String localFilePath = "/Users/mini/Downloads/qiniu_test.jpg";

        Response response = uploadManager.put(localFilePath, key, upToken,params,null,false);
        //输出返回结果
        System.out.println(response.bodyString());
    }

}
