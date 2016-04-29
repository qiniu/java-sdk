import java.io.IOException;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

public class UploadDemo {
  //设置好账号的ACCESS_KEY和SECRET_KEY
  String ACCESS_KEY = "Access_Key";
  String SECRET_KEY = "Secret_Key";
  //要上传的空间
  String bucketname = "Bucket_Name";
  //上传到七牛后保存的文件名
  String key = "my-java.png";
  //上传文件的路径
  String filePath = "/.../...";

  //密钥配置
  Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
  //创建上传对象
  UploadManager uploadManager = new UploadManager();

  // 覆盖上传
  public String getUpToken(){
    //<bucket>:<key>，表示只允许用户上传指定key的文件。在这种格式下文件默认允许“修改”，已存在同名资源则会被本次覆盖。
    //如果希望只能上传指定key的文件，并且不允许修改，那么可以将下面的 insertOnly 属性值设为 1。
    //第三个参数是token的过期时间
      return auth.uploadToken(bucketname, key, 3600, new StringMap().put("insertOnly", 1 ));
  }

  public void upload() throws IOException{
    try {
      //调用put方法上传，这里指定的key和上传策略中的key要一致
      Response res = uploadManager.put(filePath, key, getUpToken());
      //打印返回的信息
      System.out.println(res.bodyString()); 
      } catch (QiniuException e) {
          Response r = e.response;
          // 请求失败时打印的异常的信息
          System.out.println(r.toString());
          try {
              //响应的文本信息
            System.out.println(r.bodyString());
          } catch (QiniuException e1) {
              //ignore
          }
      }       
  }

  public static void main(String args[]) throws IOException{  
    new UploadDemo().upload();
  }

}