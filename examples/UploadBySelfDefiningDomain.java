import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;


public class UploadBySelfDefiningDomain {

    private static final String ACCESS_KEY = "填写你们的AK";
    private static final String SECRET_KEY = "填写你们的SK";
    private static final String BUCKET = "存储空间";
    private static final Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    //自定义上传域名，华东z0，华北z1，华南z2
    //上传主要修改 srcup 和 accup
    public static Region regionHD() {
        return (new Region.Builder()).
                srcUpHost("upload-z0.qiniup.com").
                accUpHost("upload-z0.qiniup.com").
                iovipHost("iovip-z0.qbox.me").
                rsHost("rs-z0.qbox.me").
                rsfHost("rsf-z0.qbox.me").
                apiHost("api-z0.qiniu.com").build();
    }

    public static void main(String args[]) throws Exception {
        upload();
    }

    public static void upload() throws QiniuException {
        Configuration cfg = new Configuration(regionHD());
        //是否指定https上传，默认true
        //cfg.useHttpsDomains=false;
        UploadManager uploadManager = new UploadManager(cfg);
        StringMap policy = new StringMap();
        String upToken = auth.uploadToken(BUCKET, null, 3600, policy);
        String localFilePath = "/Users/mini/Downloads/qiniu_test.jpg";
        Response response = uploadManager.put(localFilePath, "qiniu_test.jpg", upToken);
        //解析上传成功的结果
        DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        System.out.println(putRet.key);
    }

}
