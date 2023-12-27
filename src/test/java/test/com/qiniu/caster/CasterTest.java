package test.com.qiniu.caster;

import com.qiniu.caster.CasterManager;
import com.qiniu.caster.model.CasterParams;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.*;
import test.com.qiniu.TestConfig;

import java.util.HashMap;

public class CasterTest {
    static String accessKey = TestConfig.testAccessKey;
    static String secretKey = TestConfig.testSecretKey;
    static CasterManager casterManager;

    @BeforeAll
    public static void setUp() {
        Auth auth = Auth.create(accessKey, secretKey);
        CasterTest.casterManager = new CasterManager(auth);
    }

    @Test
    public void testCreat() {
        CasterParams casterParams = new CasterParams();
        casterParams.setStaticKey("aaabbbccc");
        HashMap hashMap = new HashMap<String, CasterParams.Monitor>();
        CasterParams.Monitor monitor = new CasterParams.Monitor("www.bai.com", 2, false);
        CasterParams.Monitor monitor2 = new CasterParams.Monitor();
        hashMap.put("1", monitor);
        hashMap.put("0", monitor2);
        CasterParams.Canvas canvas = new CasterParams.Canvas("720P", 720, 480);
        casterParams.setCanvas(canvas);
        casterParams.setMonitors(hashMap);
        Response result =  casterManager.createCaster("abcde", casterParams);
        assert result.statusCode == 200;
    }

    @Test
    public void casterInfo() throws QiniuException {
        Response result =  casterManager.getCasterInfo("u1380432151abcde");
        assert result.statusCode == 200;
    }

    @Test
    @Disabled
    public void testStop() {
        Response result =  casterManager.stopCaster("u1380432151abcde");
        assert result.statusCode == 200;
    }

    @Test
    @Disabled
    public void testStart() {
        Response result =  casterManager.startCaster("u1380432151abcde", (int) (System.currentTimeMillis() / 1000) + 3600, 1);
        assert result.statusCode == 200;
    }

    @Test
    public void testChangeLayouts() {
        Response result =  casterManager.changeLayout("u1380432151abcde", 2, "aaabbbccc");
        assert result.statusCode == 200;
    }

    @Test
    public void testUpdateLayouts() {
        Response result =  casterManager.updateLayout("u1380432151abcde", 0, "liuliu", null, null, "aaabbbccc");
        assert result.statusCode == 200;
    }


    @Test
    @AfterAll
    public static void testDelete() {
        Response result =  casterManager.deleteCaster("u1380432151abcde");
        assert result.statusCode == 200;
    }

}
