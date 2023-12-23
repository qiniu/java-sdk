import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;

public class BucketManagerDemo {

    public static void main(String args[]) {
        //设置需要操作的账号的AK和SK
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        //第一种方式: 指定具体的要上传的region
        //指定存储空间所在区域，华北region1，华南region2 ，华东 region0等。具体可以Region类里寻找。
        //Region region = Region.region1();

        //第一种方式: 使用自动方式
        Region autoRegion = Region.autoRegion();
        Configuration c = new Configuration(autoRegion);

        //实例化一个BucketManager对象
        BucketManager bucketManager = new BucketManager(auth, c);
        //要测试的空间和key，并且这个key在你空间中存在
        String bucket = "Bucket_Name";
        String key = "Bucket_key";
        //将文件从文件key 复制到文件key2。 可以在不同bucket复制
        String key2 = "yourjavakey";
        try {
            //调用copy方法移动文件
            bucketManager.copy(bucket, key, bucket, key2);
        } catch (QiniuException e) {
            //捕获异常信息
            Response r = e.response;
            System.out.println(r.toString());
        }
    }