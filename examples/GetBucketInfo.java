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
    
    //指定区域，可以用 Zone.autoZone() 自动获取
    Zone z = Zone.zone0();
    Configuration c = new Configuration(z);
    
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
