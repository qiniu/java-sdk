import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;

public class BucketManagerDemo {

  public static void main(String args[]){
    //设置需要操作的账号的AK和SK
    String ACCESS_KEY = "Access_Key";
    String SECRET_KEY = "Secret_Key";
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    ///////////////////////指定上传的Zone的信息//////////////////
    //第一种方式: 指定具体的要上传的zone
    //注：该具体指定的方式和以下自动识别的方式选择其一即可
    //要上传的空间(bucket)的存储区域为华东时
    // Zone z = Zone.zone0();
    //要上传的空间(bucket)的存储区域为华北时
    // Zone z = Zone.zone1();
    //要上传的空间(bucket)的存储区域为华南时
    // Zone z = Zone.zone2();

    //第二种方式: 自动识别要上传的空间(bucket)的存储区域是华东、华北、华南。
    Zone z = Zone.autoZone();
    Configuration c = new Configuration(z);

    //实例化一个BucketManager对象
    BucketManager bucketManager = new BucketManager(auth,c);
    //要测试的空间和key，并且这个key在你空间中存在
    String bucket = "Bucket_Name";
    String key = "Bucket_key";
    try {
      //调用stat()方法获取文件的信息
      FileInfo info = bucketManager.stat(bucket, key);
      System.out.println(info.hash);
      System.out.println(info.key);
    } catch (QiniuException e) {
      //捕获异常信息
      Response r = e.response;
      System.out.println(r.toString());
    }
  }
}