
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;

public class DomainListDemo {
    public static void main(String args[]) {
        //设置需要操作的账号的AK和SK
        String ACCESS_KEY = "DWQOcrnPp1ogwgAHBdIK1mI-";
        String SECRET_KEY = "cJFhYuaq7Vo35e1pmXO0aGkJG";
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        Zone z = Zone.zone0();
        Configuration c = new Configuration(z);

        //实例化一个BucketManager对象
        BucketManager bucketManager = new BucketManager(auth, c);

        //要列举文件的空间名
        String bucket = "dorst1";

        try {
            String[] domainLists = bucketManager.domainList(bucket);
            for(String domain : domainLists)
            System.out.print(domain);

        } catch (QiniuException e) {

        }

    }
}
