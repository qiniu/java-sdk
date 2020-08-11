import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 自定义文件元信息demo(x-qn-meta-*)
 * 
 * 接口
 * POST /setmeta/<EncodedEntryURI>[/<x-qn-meta-MetaKey>/<EncodedMetaValue>][/cond/Encoded(condKey1=condVal1&condKey2=condVal2)]
 * Host: rs-<region>.qiniu.com
 * Content-Type: application/x-www-form-urlencoded
 * Authorization: Qbox 鉴权
 * 
 * 注意：
 * meta-key，key不能设置为中文,不允许为空;
 * 新的metas会完全替换掉以前的metas，注意, 是完全覆盖;
 * 如果请求url中没有 [<x-qn-meta-MetaKey>/<EncodedMetaValue>]，则表示要删除所有metas;
 *
 */
public class QNMetaDemo {
    //设置好账号的ACCESS_KEY和SECRET_KEY
    private static final String ACCESS_KEY = "填写你们自己的AK";
    private static final String SECRET_KEY = "填写你们自己的SK";
    private static final Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    private Client client = new Client();
    private String bucketName;
    private String rsHost;

    {
        bucketName = "填写你们自己的存储空间";
        //设置存储区域rs域名，华东z0 华北z1 华南z2
        rsHost = "rs-z2.qiniu.com";
    }

    public static void main(String[] args) throws Exception {
        QNMetaDemo qnMetaDemo = new QNMetaDemo();
        //需要设置自定义meta的空间文件名称
        String key = "1.mp4";
        //设置自定义的meta头部，注意，每次调用该接口，是直接覆盖meta，不是追加meta
        HashMap<String, String> metaKeyVal = new HashMap<>();
        metaKeyVal.put("eng1", "qiniu");
        metaKeyVal.put("eng2", "七牛");
        boolean result = qnMetaDemo.setMeta(qnMetaDemo.bucketName, key, metaKeyVal);
        if(result){
            System.out.println("done");
        }
    }

    /**
     * @param bucket  存储空间名称
     * @param key     存储空间的文件名称
     * @param headers 自定义的请求头 key - val map
     * @return true or false
     * @throws QiniuException
     */
    public boolean setMeta(String bucket, String key, Map<String, String> headers) throws QiniuException {
        String resource = UrlSafeBase64.encodeToString(bucket.concat(":").concat(key));
        String path = String.format("/setmeta/%s", resource);
        String k;
        String encodedMetaValue;
        for(Iterator var6 = headers.keySet().iterator(); var6.hasNext(); path = String.format("%s/x-qn-meta-%s/%s", path, k, encodedMetaValue)) {
            k = (String)var6.next();
            encodedMetaValue = UrlSafeBase64.encodeToString((String)headers.get(k));
        }
        //接口请求地址
        String url = String.format("https://%s%s", rsHost, path);
        System.out.println(url);
        Response res = this.post(url);
        if (res.statusCode != 200) {
            return false;
        } else {
            return true;
        }
    }

    private Response post(String url) throws QiniuException {
        StringMap headers = this.auth.authorization(url);
        return this.client.post(url, null, headers, "application/x-www-form-urlencoded");
    }
}
