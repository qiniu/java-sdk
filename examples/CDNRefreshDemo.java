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

        //设置要刷新的urls
        String[] urls = new String[] {
                "Url1",
                "Url2"
        };

        try {
            //单次方法调用刷新的链接不可以超过100个
            response = cdnManager.refreshUrls(urls);
            System.out.println(response.toString());
            //获取其他的回复内容
        } catch (QiniuException e) {
            System.err.println(e.response.toString());
        }

        //设置要刷新的dirs
        String[] dirs = new String[]{
                "dir1",
                "dir2"
        };
        try {
            //单次方法调用刷新的目录不可以超过10个，另外刷新目录权限需要联系技术支持开通
            CdnResult.RefreshResult result = c.refreshDirs(dirs);
            System.out.println(result.code);
            //获取其他的回复内容
        } catch (QiniuException e) {
            System.err.println(e.response.toString());
        }

    }
}