import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;

import java.io.IOException;

public class UploadDemo {

    //设置好账号的ACCESS_KEY和SECRET_KEY
    String ACCESS_KEY = "Access_Key";
    String SECRET_KEY = "Secret_Key";
    //要上传的空间
    String bucketname = "bucketname";
    //上传到七牛后保存的路径及文件名
    String key = "dirName/dirName/test.png";
    //上传文件的路径[本地文件路径]
    String filePath = "T:\\download\\chrome\\test.png";

    String bucketname_DOMAIN = "https://www.test.com/";

    //密钥配置
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    Region region = new Region.Builder().autoRegion(Configuration.defaultUcHost);
    Configuration c = new Configuration(region);

    //创建上传对象
    UploadManager uploadManager = new UploadManager(c);

    //创建CDN管理对象
    CdnManager cdnManager = new CdnManager(auth);

    // 覆盖上传
    public String getUpToken() {
        //<bucket>:<key>，表示只允许用户上传指定key的文件。在这种格式下文件默认允许“修改”，已存在同名资源则会被本次覆盖。
        //如果希望只能上传指定key的文件，并且不允许修改，那么可以将下面的 insertOnly 属性值设为 1。
        return auth.uploadToken(bucketname, key);

//        return auth.uploadToken(bucketname, key, 3600, new StringMap().put("insertOnly", 1));
    }

    public void upload() throws IOException {
        try {
            //调用put方法上传，这里指定的key和上传策略中的key要一致
            Response res = uploadManager.put(filePath, key, getUpToken());
            //打印返回的信息
            System.out.println(res.bodyString());

            String url = bucketname_DOMAIN+key;
            String[] urls = new String[]{url};

            //如果是覆盖之前旧的，刷新一下CDN，能更快得到新的文件。
            RefreshResult result = cdnManager.refreshUrls(urls);
            System.out.println(result.code);
        } catch (QiniuException e) {
            System.out.println("upload error:"+e.getMessage());
            Response r = e.response;
            if(r != null && r.bodyString() != null){
                System.out.println(r.bodyString());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new QiNiuUtil().upload();
    }
}
