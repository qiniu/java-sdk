import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;

public class FetchDemo {
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
	    
	    //文件保存的空间名和文件名
	    String bucket = "yourbucket";
	    String key = "yourkey";
	    
	    //要fetch的url
	    String url = "url";
	    
	    try {
	      //调用fetch方法抓取文件
	      bucketManager.fetch(url, bucket,key);
	    } catch (QiniuException e) {
	      //捕获异常信息
	      Response r = e.response;
	      System.out.println(r.toString());
	    }
	  }
}
