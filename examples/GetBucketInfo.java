import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.storage.model.BucketInfo;

public class GetBucketInfo {
    
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
    
    // 空间名
    String bucket = "BUCKET";
    
    public static void main(String[] args) {
        new GetBucketInfo().getBucketInfo();
    }
    
    public void getBucketInfo() {
        try {
            BucketInfo bucketInfo = bucketManager.getBucketInfo(bucket);
            // 输出空间私有性
            System.out.println(bucketInfo.getPrivate());
            // 输出空间所述区域
            System.out.println(bucketInfo.getZone());
            
           // 其他参数详见 https://github.com/qiniu/java-sdk/blob/master/src/main/java/com/qiniu/storage/model/BucketInfo.java
            
        } catch (Exception e) {
            //
        }
    }
}
