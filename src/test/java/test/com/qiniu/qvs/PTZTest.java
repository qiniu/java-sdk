package test.com.qiniu.qvs;

import com.qiniu.http.Response;
import com.qiniu.qvs.PTZManager;
import com.qiniu.util.Auth;
import test.com.qiniu.TestConfig;
import org.junit.jupiter.api.BeforeEach;

public class PTZTest {
    Auth auth = TestConfig.testAuth;
    private PTZManager ptzManager;
    private Response res = null;
    private final String namespaceId = "3nm4x1e0xw855";
    private final String gbId = "31011500991320007536";
    private final String chId = "";
    private String name = "" + System.currentTimeMillis();

    @BeforeEach
    public void setUp() throws Exception {
        this.ptzManager = new PTZManager(auth);
    }

/* @Test
    @Tag("IntegrationTest")
    public void testPTZControl() {
        try {
            res = ptzManager.ptzControl(namespaceId, gbId, "up", 5, chId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
 */

 /* @Test
    @Tag("IntegrationTest")
    public void testFocusControl() {
        try {
            res = ptzManager.focusControl(namespaceId, gbId, "focusfar", 5, chId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
  */

//    @Test
//    @Tag("IntegrationTest")
//    public void testIrisControl() {
//        try {
//            res = ptzManager.irisControl(namespaceId, gbId, "irisin", 5, chId);
//            assertNotNull(res);
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }

  /* @Test
    @Tag("IntegrationTest")
    public void testPresetsControl() {
        name = "" + System.currentTimeMillis();
        try {
            res = ptzManager.presetsControl(namespaceId, gbId, "set", name, 0, chId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
   */

   /* @Test
    @Tag("IntegrationTest")
    public void testListPresets() {
        try {
            res = ptzManager.listPresets(namespaceId, gbId, chId);
            assertNotNull(res);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
    */
}
