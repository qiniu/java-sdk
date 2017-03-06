import com.qiniu.common.QiniuException;
import com.qiniu.util.Auth;
import com.qiniu.cdn.CdnManager;
import com.qiniu.cdn.CdnResult;

public class CdnRefreshDemo {
    public static void main(String args[]) {
        //设置需要操作的账号的AK和SK
        String ACCESS_KEY = "Access_Key";
        String SECRET_KEY = "Secret_Key";

        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        //实例化一个CDNManger对象
        CdnManager cdnManager = new CdnManager(auth);

        CdnResult.RefreshResult response = null;

        //设置要刷新的url
        String[] urls = new String[] {"Url"};

        try {
            response = cdnManager.refreshUrls(urls);
            System.out.println(response.toString());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }
}