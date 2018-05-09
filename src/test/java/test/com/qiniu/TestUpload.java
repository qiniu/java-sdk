package test.com.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.sun.org.apache.regexp.internal.RE;
import org.junit.Test;
import org.junit.runner.JUnitCommandLineParseResult;

import java.net.InetAddress;

/**
 * Created by jemy on 21/01/2018.
 */
public class TestUpload {


    @Test
    public void testUploadWithSaveKey() {
        String ak = "0jHFupMTJiWhntX7P7GPlE8PRImaTN39s58KKEVc";
        String sk = "nYKM7FrpI7V-xUpqnGMfW_iVLwa3bF5yQQJajJ0n";
        String bucket = "if-pbl";
        StringMap policy = new StringMap();
        //上传策略这样指定saveKey，客户端key设置为null
        //policy.put("saveKey", "hello/world/$(etag)");

        Auth auth = Auth.create(ak, sk);
        String uptoken = auth.uploadToken(bucket, null, 3600, policy);
        UploadManager manager = new UploadManager(new Configuration());
        try {
            for (int i = 1; i < 2000; i++) {
                String key = String.format("folder%d/qiniu.png", i);
                Response response = manager.put("/Users/jemy/Documents/qiniu.png", key, uptoken);
                System.out.println(response.bodyString());
            }
        } catch (QiniuException ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void testList() {
        String ak = "0jHFupMTJiWhntX7P7GPlE8PRImaTN39s58KKEVc";
        String sk = "nYKM7FrpI7V-xUpqnGMfW_iVLwa3bF5yQQJajJ0n";
        String bucket = "if-pbl";
        Auth auth = Auth.create(ak, sk);
        BucketManager bucketManager = new BucketManager(auth, new Configuration());
        String prefix = "";
        try {
            FileListing listRet = bucketManager.listFiles(bucket, prefix, null, 1000, "/");
            for (FileInfo item : listRet.items) {
                System.out.println(item.key);
            }

            if (listRet.commonPrefixes != null) {
                for (String p : listRet.commonPrefixes) {
                    System.out.println(p);
                }
            }
        } catch (QiniuException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testCreateBucket() {
        String ak = "0jHFupMTJiWhntX7P7GPlE8PRImaTN39s58KKEVc";
        String sk = "nYKM7FrpI7V-xUpqnGMfW_iVLwa3bF5yQQJajJ0n";
        String bucket = "if-as0";
        Auth auth = Auth.create(ak, sk);
        BucketManager bucketManager = new BucketManager(auth, new Configuration());
        String region = "as0";
        try {
            bucketManager.createBucket(bucket, region);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testNic() throws Exception {
        InetAddress i = InetAddress.getLocalHost();
        System.out.println(i.getCanonicalHostName());
    }

    @Test
    public void testCreateUptoken() {
        String ak = "anEC5u_72gw1kZPSy3Dsq1lo_DPXyvuPDaj4ePkN";
        String sk = "Ybuk4unN4XL267Jsr8LRFPiaJzNOGLxqZSc8heL3";
        Auth auth = Auth.create(ak, sk);
        String bucket = "sdk-na0";
        String key = "a.jpg";
        long expires = 3153600000L;
        StringMap policy = new StringMap();
        policy.put("fsizeMin", new Integer(800000));
        policy.put("fsizeLimit", new Integer(1000000));
        String uptoken = auth.uploadToken(bucket, key, expires, policy);
        System.out.println(uptoken);

        UploadManager manager = new UploadManager(new Configuration());
        try {
            Response response = manager.put("/Users/jemy/Documents/qiniu.png", key, uptoken);
            System.out.println(response.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
