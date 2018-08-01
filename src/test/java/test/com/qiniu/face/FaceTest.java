package test.com.qiniu.face;

import com.qiniu.common.QiniuException;
import com.qiniu.face.FaceCompareManager;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.util.Date;

/**
 * Created by jemy on 2018/6/6.
 */
public class FaceTest {
    private String ak = "fae7Nl_OO2V6jKwIhZd"; //AccessKey you get from qiniu
    private String sk = "-Dx41TwHW2lMJSO0cu2UHyr"; //SecretKey you get from qiniu
    private Auth auth = null;

    {
        try {
            auth = Auth.create(ak, sk);
        } catch (Exception e) {
            auth = TestConfig.testAuth;
        }
    }

    private FaceCompareManager face = new FaceCompareManager(auth);

    //创建人脸库
    @Test
    public void createFaceDb() {
        try {
            String data = "{\"data\": [{\"uri\": \"\", \"attribute\": {\"name\": \"zw001\"}}]}";
            Response response = face.createFaceDB("face01", data);
            System.out.print(response.getInfo());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //查询人脸库
    @Test
    public void listFaceDB() {
        Response response = null;
        try {
            response = face.listFaceDB();
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        System.out.print(getString(response));
    }

    //删除人脸库
    @Test
    public void deleteFaceDB() {
        try {
            Response response = face.deleteFaceDB("zwdb06");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //添加人脸
    @Test
    public void createFace() {
        try {
            Long start = new Date().getTime();
            String data = "{\"data\": [{\"uri\": \"http://1.bkt.clouddn.com/FgoeFtPzutwUClK4jaR28BXGMA_D\", \"attribute\": {\"name\": \"zw004\"}}]}";
            Response response = face.createFace("zwdb02", data);
            Long end = new Date().getTime() - start;
            System.out.println(end);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }


    //查询所有人脸
    @Test
    public void getFaceAll() {
        try {
            Response response = face.getFace("face01");
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    //删除人脸
    @Test
    public void deleteFace() {
        try {
            String str = "{\"faces\":[\"BwAAAJ5WNym22TUV\"]}";
            Response response = face.deleteFace("zwdb04", str);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }

    //人脸检索
    @Test
    public void compareFace() {
        try {
            Long start = new Date().getTime();
            String str = "{\"data\":{\"uri\":\"http://p6c0z9zl1.bkt.clouddn.com/Fo9_5Ejmu9Wnu4yxTkFRxTTZqUGM\"}}";
            Response response = face.compareFace("zwdb01", str);
            Long end = new Date().getTime() - start;
            System.out.println(end);
            System.out.print(getString(response));
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    private String getString(Response response) {
        String[] resJson = response.getInfo().split("\n");
        return response.statusCode + "\n" + resJson[2];
    }
}
