
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

        //第一种方式: 指定具体的要上传的region
        //指定存储空间所在区域，华北region1，华南region2 ，华东 region0等。具体可以Region类里寻找。
        //Region region = Region.region1();

        //第一种方式: 使用自动方式
        Region autoRegion = Region.autoRegion();
        Configuration c = new Configuration(autoRegion);

        Region region = new Region.Builder().autoRegion(Configuration.defaultUcHost);
        Configuration c = new Configuration(region);

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
