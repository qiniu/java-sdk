package com.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import java.util.UUID;

public class Main {
    public static void main(String[] args) throws QiniuException {
        // 构造一个带指定的zone对象的配置类
        Configuration configuration = new Configuration(Zone.q_zone0());
        String filename = UUID.randomUUID() + ".txt";
        System.out.println(filename);
        // 上传工具
        UploadManager uploadManager = new UploadManager(configuration);
        Auth auth = Auth.create("lHQPfJY6OwTlFq2bmuVHck-9L5B-Nwj994BUb8DT", "rJrPO_uMpU-r2uhYUapDEWbpPEB_XPWjZTTlklly");
        String upToken = auth.uploadToken("hktry", filename, 3600, null);
        // String upToken =auth.uploadToken("hktry",null,s);
        System.out.println(upToken);
        // result:XNfRj8ywAvRmhHd39-_BI9BkBZP-vxfe6H5IoENJ:RxF8DU0tagRFvMNKMopuHRNABBo=:eyJzY29wZSI6ImhrdHJ5IiwiZGVhZGxpbmUiOjE0OTY3MTc2MzB9
        Response res = uploadManager.put("/Users/hktry/A3.png", filename, upToken);
        System.out.println(res.statusCode);
        System.out.println(res.toString());
    }

}
