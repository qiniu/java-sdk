package test.com.qiniu.caster;

import com.qiniu.caster.CasterManager;
import com.qiniu.caster.model.CasterParams;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class CasterTest {
    String accessKey = ""; //config.getAccesskey();
    String secretKey = ""; //config.getSecretKey();
    CasterManager casterManager;

    @BeforeEach
    public void setUp() throws Exception {
        Auth auth = Auth.create(accessKey, secretKey);
        this.casterManager = new CasterManager(auth);
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
    public void testStop() {
        Response result =  casterManager.stopCaster("u1380432151abcde");
        assert result.statusCode == 200;
    }

    @Test
    public void testStart() {
        Response result =  casterManager.startCaster("u1380432151abcde", 2, 1);
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
    public void testDelete() {
        Response result =  casterManager.deleteCaster("u1380432151abcde");
        assert result.statusCode == 200;
    }

}
